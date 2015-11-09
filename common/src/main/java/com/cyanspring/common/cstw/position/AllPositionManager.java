package com.cyanspring.common.cstw.position;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.OverAllPositionReplyEvent;
import com.cyanspring.common.event.account.OverAllPositionRequestEvent;
import com.cyanspring.common.event.order.AllStrategySnapshotReplyEvent;
import com.cyanspring.common.event.order.AllStrategySnapshotRequestEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

/**
 * 
 * @author jimmy
 *
 */
public class AllPositionManager implements IAsyncEventListener {

	private static final Logger log = LoggerFactory.getLogger(AllPositionManager.class);
	private IRemoteEventManager eventManager;
	private String server = "";
	private List<String> accountIdList = new ArrayList<String>();
	private Map<String,Account> accountMap = new HashMap<String,Account>();
	private UserGroup loginUser = null;
	private ConcurrentHashMap<String, List<OverallPosition>> allPositionMap = new ConcurrentHashMap<String, List<OverallPosition>>();
	private ConcurrentHashMap<String, List<OpenPosition>> openPositionMap = new ConcurrentHashMap<String, List<OpenPosition>>();
	private ConcurrentHashMap<String, List<ClosedPosition>> closedPositionMap = new ConcurrentHashMap<String, List<ClosedPosition>>();
	private ConcurrentHashMap<String,Map<String,ParentOrder>> orderMap = new ConcurrentHashMap<String,Map<String,ParentOrder>>();

	private List<IPositionChangeListener> listenerList = new ArrayList<IPositionChangeListener>();

	public AllPositionManager() {
		
	}
	
	public AllPositionManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof OverAllPositionReplyEvent){
			OverAllPositionReplyEvent e = (OverAllPositionReplyEvent) event;
			log.info("receive OverAllPositionReplyEvent - isOk:{}",e.isOk());
			if(!e.isOk()){
				log.warn("OverAllPositionReplyEvent fail:{}",e.getMessage());
				return;
			}
			log.info("getOpenPositionList size:{}, getClosedPositionList:{}",e.getOpenPositionList().size(),e.getClosedPositionList().size());
			updateOpenPositionList(e.getOpenPositionList());
			updateClosedPositionList(e.getClosedPositionList());
			refreshOverallPosition(null);
		}else if(event instanceof OpenPositionUpdateEvent){
			OpenPositionUpdateEvent e = (OpenPositionUpdateEvent) event;
			updatePosition(e.getPosition(),true);			
		}else if(event instanceof OpenPositionDynamicUpdateEvent){
			OpenPositionDynamicUpdateEvent e = (OpenPositionDynamicUpdateEvent) event;
			updatePosition(e.getPosition(),true);			
		}else if(event instanceof ClosedPositionUpdateEvent){
			ClosedPositionUpdateEvent e = (ClosedPositionUpdateEvent) event;
			updatePosition(e.getPosition(),true);
		}else if(event instanceof AllStrategySnapshotReplyEvent){
			AllStrategySnapshotReplyEvent e = (AllStrategySnapshotReplyEvent) event;
			processAllStrategySnapshotReplyEvent(e);
			refreshOverallPosition(null);
		}else if(event instanceof ParentOrderUpdateEvent){
			ParentOrderUpdateEvent e = (ParentOrderUpdateEvent) event;
			updateOrder(e.getOrder());
		}
	}
	
	private void processAllStrategySnapshotReplyEvent(
			AllStrategySnapshotReplyEvent event) {
		if(!event.isOk()){
			log.warn("processAllStrategySnapshotReplyEvent is not ok:{}",event.getMessage());
			return;
		}
		updateOrder(event.getOrders());

		if(null != loginUser && loginUser.isAdmin()){
			eventManager.subscribe(ParentOrderUpdateEvent.class,this);
		}else {
			subGroupEvent(accountIdList);
		}			
	}
	
	synchronized private void updateOrder(List<ParentOrder> orders) {
		if(null == orders || orders.isEmpty())
			return ;
		
		
		for(ParentOrder order : orders){
			
//			log.info("order symbol :{},state:{}, status:{} , cumqty:{}, time:{}",new Object[]{
//					order.getSymbol()
//					,order.getState()
//					,order.getOrdStatus()
//					,order.getCumQty()
//					,order.getTimeModified()
//			});
			if(!TimeUtil.sameDate(TimeUtil.getOnlyDate(new Date()), order.getModified()))
				continue;
			
			if(!order.getState().equals(StrategyState.Terminated)
					|| !order.getOrdStatus().equals(OrdStatus.FILLED) 
					|| !PriceUtils.GreaterThan(order.getCumQty(), 0)
					){
				continue;
			}		
			String account = order.getAccount();
			log.info("receive order:{},{}",account,order.getId());
			Map<String,ParentOrder> tempMap = null;

			if(orderMap.containsKey(account)){
				tempMap = orderMap.get(account);
				if(null == tempMap)
					tempMap = new HashMap<String,ParentOrder>();
				else{
					if(tempMap.containsKey(order.getId()))
						continue;
				}
				
				tempMap.put(order.getId(),order);
				orderMap.put(account, tempMap);
			}else{
				tempMap = new HashMap<String,ParentOrder>();
				tempMap.put(order.getId(),order);
				orderMap.put(account, tempMap);
			}
		}
	}
	
	synchronized public void updateOrder(ParentOrder order){
		if( null == order)
			return;
		
		List <ParentOrder> tmpList = new ArrayList<ParentOrder>();
		tmpList.add(order);
		updateOrder(tmpList);
		refreshOverallPosition(order.getAccount());
	}

	private void subGroupEvent(List<String> accountList){
		for(String id : accountList){
			eventManager.unsubscribe(ParentOrderUpdateEvent.class, id, this);
			eventManager.subscribe(ParentOrderUpdateEvent.class, id, this);
			log.info("sub ParentOrderUpdateEvent:{}",id);
		}
	}
	
	private void updateClosedPositionList(
			List<ClosedPosition> closedPositionList) {
		closedPositionMap.clear();
		for(ClosedPosition pos : closedPositionList){
			log.info(" update close pos:{},{},{},{},{},{}"
					,new Object[]{pos.getSymbol(),pos.getQty(),pos.getPnL(),pos.getBuyPrice(),pos.getSellPrice(),pos.isBuy()});
			updatePosition(pos,false);
		}		
	}

	private void updateOpenPositionList(List<OpenPosition> openPositionList) {
		openPositionMap.clear();
		for(OpenPosition pos : openPositionList){
			log.info(" update open pos:{},{},{},{},{}"
					,new Object[]{pos.getSymbol(),pos.getQty(),pos.getPnL(),pos.getPrice(),pos.isBuy()});
			updatePosition(pos,false);
		}
	}
	
	private void updatePosition(OpenPosition position,boolean isDynamic){
		if( null == position || !inGroup(position.getAccount()))
			return;
		
		String account = position.getAccount();
		List<OpenPosition> oldList = openPositionMap.get(account);
		if(null == oldList){
			oldList = new ArrayList<OpenPosition>();
			oldList.add(position);
			openPositionMap.put(account, oldList);
		}else{
			List <OpenPosition>tempList = new ArrayList<OpenPosition>();
			boolean isNewSymbol = true;
			for(OpenPosition op:oldList){
				if(position.getSymbol().equals(op.getSymbol())){
					isNewSymbol = false;
					if(!PriceUtils.isZero(position.getAvailableQty()))
						tempList.add(position);
				}else{
					if(!PriceUtils.isZero(op.getAvailableQty()))
						tempList.add(op);
				}
			}
			if(isNewSymbol){
				tempList.add(position);
			}
			openPositionMap.put(account, tempList);
		}
//		 printOpenPositionMap();
		if(isDynamic){
			refreshOverallPosition(position.getAccount());
			notifyOpenPositionChange(position);
		}
	}

	
	private void printOpenPositionMap(){
		Iterator <String>keys = openPositionMap.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			log.info("(open pos)account:{}",key);
			List<OpenPosition> ops = openPositionMap.get(key);
			for(OpenPosition op : ops){
				log.info(" -op:{},{},{},{}",new Object[]{op.getSymbol(),op.getQty(),op.getPnL(),op.getPrice()});
			}
		}
	}
	
	private void printClosedPositionMap(){
		Iterator <String>keys = closedPositionMap.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			log.info("(close pos)account:{}",key);
			List<ClosedPosition> ops = closedPositionMap.get(key);
			for(ClosedPosition op : ops){
				log.info(" -cp:{},{},{},{},{}",new Object[]{op.getSymbol(),op.getQty(),op.getPnL(),op.getBuyPrice(),op.getSellPrice()});
			}
		}
	}
	
	private void printOverallPositionMap(){
		log.info("printOverallPositionMap");
		Iterator <String>keys = allPositionMap.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			log.info("account:{}",key);
			List<OverallPosition> ops = allPositionMap.get(key);
			for(OverallPosition op : ops){
				log.info(" -all:{}, {}, {}, {}, {}, {}, {}, {}, {}, {}"
						,new Object[]{op.getAccount()
								,op.getUser()
								,op.getSymbol()
								,op.getBuyPrice()
								,op.getBuyQty()
								,op.getSellPrice()
								,op.getSellQty()
								,op.getTotalQty()
								,op.getQty()
								,op.getExecCount()
						});
			}
		}
	}
	
	private void updatePosition(ClosedPosition position,boolean isDynamic){
		if( null == position || !inGroup(position.getAccount()))
			return;
				
		String account = position.getAccount();
		if(isDynamic){
			List<OpenPosition> tempList = new ArrayList<OpenPosition>();
			if(openPositionMap.containsKey(account)){
				List <OpenPosition>oldList = openPositionMap.get(account);
				for(OpenPosition op:oldList){
					if(!position.getSymbol().equals(op.getSymbol())){
						tempList.add(op);
					}
				}
				openPositionMap.put(account, tempList);
			}
		}
	
		List<ClosedPosition> oldList = closedPositionMap.get(account);
		if(null == oldList){
			oldList = new ArrayList<ClosedPosition>();
		}
		oldList.add(position);
		closedPositionMap.put(account, oldList);
//		printClosedPositionMap();
		if(isDynamic){
			refreshOverallPosition(position.getAccount());
			notifyClosedPositionChange(position);
		}
	}
	
	private void notifyOpenPositionChange(OpenPosition position) {
		for(IPositionChangeListener listener : listenerList){
			listener.OpenPositionChange(position);
		}
	}
	
	private void notifyClosedPositionChange(ClosedPosition position) {
		for(IPositionChangeListener listener : listenerList){
			listener.ClosedPositionChange(position);
		}		
	}
	
	private void notifyOverallPositionChange() {
		for(IPositionChangeListener listener : listenerList){
			listener.OverAllPositionChange(getOverAllPositionList());
		}	
	}

	public OverallPosition toOverallPosition(Account account,String symbol,List<OpenPosition> opList, List<ClosedPosition> cpList){
		OverallPosition oap = new OverallPosition(account.getUserId(),account.getId(),"",symbol);
		
		double totalBuyQty = 0;
		double totalSellQty = 0;
		double buySum = 0;
		double sellSum = 0;
		for(OpenPosition op : opList){
			if(!op.getSymbol().equals(symbol))
				continue;
			
			double qty = Math.abs(op.getQty());
			double price = op.getPrice();
			if(op.isBuy()){		
				oap.setBuyQty(oap.getBuyQty()+qty);
				totalBuyQty += qty;
				buySum += (qty*price);
			}else{
				oap.setSellQty(oap.getSellQty()+qty);
				totalSellQty += qty;
				sellSum += (qty*price);
			}
			
			oap.setPnL(op.getAcPnL()+oap.getPnL());
			oap.setUrPnL(op.getAcPnL()+oap.getUrPnL());
		}
		oap.setAvgPrice((buySum+sellSum)/(totalBuyQty+totalSellQty));
		
		for(ClosedPosition cp : cpList){
			if(!cp.getSymbol().equals(symbol))
				continue;
			
			double qty = Math.abs(cp.getQty());
			sellSum += (qty*cp.getBuyPrice());
			buySum += (qty*cp.getSellPrice());
			totalBuyQty += Math.abs(qty);
			totalSellQty += Math.abs(qty);
			
			oap.setBuyQty(oap.getBuyQty()+qty);
			oap.setSellQty(oap.getSellQty()+qty);
			oap.setPnL(cp.getAcPnL()+oap.getPnL());
			oap.setRealizedPnL(cp.getAcPnL()+oap.getRealizedPnL());
		}
//		log.info("buysum:{} , totalBuyQty:{} , sellsum:{}, totalSellQty:{}"
//				,new Object[]{buySum,totalBuyQty,sellSum,totalSellQty});
		
		if(PriceUtils.isZero(buySum) || PriceUtils.isZero(totalBuyQty))
			oap.setBuyPrice(0);
		else
			oap.setBuyPrice(buySum/totalBuyQty);
		
		if(PriceUtils.isZero(sellSum) || PriceUtils.isZero(totalSellQty))
			oap.setSellPrice(0);
		else
			oap.setSellPrice(sellSum/totalSellQty);
	
		oap.setLastUpdate(new Date());
		oap.setExecCount(getExeCount(account.getId(), symbol));
		return oap;
	}
	
	private double getExeCount(String id,String symbol) {
		if(!StringUtils.hasText(id))
			return 0;
				
		if(orderMap.containsKey(id)){
			double count = 0;
			Map<String,ParentOrder> tempMap = orderMap.get(id);
			Iterator<ParentOrder> orders = tempMap.values().iterator();
			while(orders.hasNext()){
				ParentOrder order = orders.next();
				if(order.getSymbol().equals(symbol)){
					count++;
				}
			}
			return count;
		}
		return 0;
	}

	synchronized private void refreshOverallPosition(String positionAccount) {
		Set<String> idSet = new HashSet<String>();
		
		if(null == positionAccount){
			if( null != accountIdList && !accountIdList.isEmpty())
				idSet.addAll(accountIdList);
			else{
				idSet.addAll(openPositionMap.keySet());
				idSet.addAll(closedPositionMap.keySet());
			}
		}else{
			idSet.add(positionAccount);
		}

		for(String id : idSet){
			List<OpenPosition> oList = openPositionMap.get(id);
			List<ClosedPosition> cList = closedPositionMap.get(id);
			if(null == oList)
				oList = new ArrayList<OpenPosition>();
			if(null == cList)
				cList = new ArrayList<ClosedPosition>();
			
			List<OverallPosition> allList = allPositionMap.get(id);
			if( null == allList)
				allList = new ArrayList<OverallPosition>();
			
			allList.clear();
			Set <String> symbolSet = collectSymbol(id,oList,cList);
			Set <OverallPosition> tempAllPositionSet = new HashSet<OverallPosition>();
			Account account = accountMap.get(id);
			if(null == account){
				log.warn("can't find account in accountMap:{}",id);
				continue;
			}
			for(String symbol : symbolSet){
				log.info("symbol set:{},{}",id,symbol);			
				OverallPosition oap = toOverallPosition(account,symbol,oList,cList);
				tempAllPositionSet.add(oap);
			}		
			
			log.info("refresh all position:{},size:{}",id,tempAllPositionSet.size());
			allList.addAll(tempAllPositionSet);
			allPositionMap.put(id, allList);
		}
//		printOverallPositionMap();
		notifyOverallPositionChange();
	}

	private Set<String> collectSymbol(String id,List<OpenPosition> oList,List<ClosedPosition> cList) {
		Set <String> symbolSet = new HashSet<String>();
		
		if(null != oList){
			for(OpenPosition op : oList){
				symbolSet.add(op.getSymbol());
			}
		}

		if(null != cList){
			for(ClosedPosition cp : cList){
				symbolSet.add(cp.getSymbol());
			}
		}
		
		return symbolSet;
	}

	private boolean inGroup(String account){
		
		if(null != loginUser && loginUser.isAdmin())
			return true;
		
		if(null != accountIdList && accountIdList.contains(account))
			return true;

		return false;
	}
	
	public void init(IRemoteEventManager eventManager
			,String server,List<Account> accountList,UserGroup loginUser){
		
		log.info("init AllPositionManager");
		
		if(null == eventManager || !StringUtils.hasText(server)){
			log.info("init error: eventManager is null or server is empty");
		}
		
		this.eventManager = eventManager;
		this.server = server;
		if(null != accountList){
			log.info("put accountList:{}",accountList.size());
			for(Account account: accountList){
//				log.info("put account Map:{}",account.getId());
				accountMap.put(account.getId(), account);
			}
		}
		
		accountIdList.addAll(accountMap.keySet());
		this.loginUser = loginUser;
		
		if( null == accountIdList){
			accountIdList = new ArrayList<String>();
		}
		
		subEvent(OverAllPositionReplyEvent.class);
		subEvent(OpenPositionUpdateEvent.class);
		subEvent(OpenPositionDynamicUpdateEvent.class);
		subEvent(ClosedPositionUpdateEvent.class);
		subEvent(AllStrategySnapshotReplyEvent.class);
		subEvent(ParentOrderUpdateEvent.class);

		try {
			if(null != loginUser && loginUser.isAdmin()){
				eventManager.sendRemoteEvent(new AllStrategySnapshotRequestEvent(IdGenerator.getInstance().getNextID(),server, null));	
				requestOverAllPosition(null);
				return;
			}else if(null != loginUser && loginUser.getRole().isManagerLevel()){
				eventManager.sendRemoteEvent(new AllStrategySnapshotRequestEvent(IdGenerator.getInstance().getNextID(), server, accountIdList));
			}	
			
			requestOverAllPosition(accountIdList);

		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public void unInit(){
		unSubEvent(OverAllPositionReplyEvent.class);
		unSubEvent(OpenPositionUpdateEvent.class);
		unSubEvent(OpenPositionDynamicUpdateEvent.class);
		unSubEvent(ClosedPositionUpdateEvent.class);
		unSubEvent(AllStrategySnapshotReplyEvent.class);
		unSubEvent(ParentOrderUpdateEvent.class);
	}
	
	
	public void addIPositionChangeListener(IPositionChangeListener listener) {
		listenerList.add(listener);
	}

	public void removeIPositionChangeListener(IPositionChangeListener listener) {
		listenerList.remove(listener);
	}
	
	private void requestOverAllPosition(List<String> accountIdList) {
		OverAllPositionRequestEvent event = new OverAllPositionRequestEvent(IdGenerator
				.getInstance().getNextID(), server, accountIdList);
		try {
			if(null != eventManager)
				eventManager.sendRemoteEvent(event);
			else
				log.info("eventManager is null");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.subscribe(clazz, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.unsubscribe(clazz, this);
	}
	
	public List<ClosedPosition> getAllClosedPositionList(){
		if( null == closedPositionMap)
			return null;
		
		List <ClosedPosition> list = new ArrayList<ClosedPosition>();
		Iterator <List<ClosedPosition>> positionIte = closedPositionMap.values().iterator();
		while(positionIte.hasNext()){
			list.addAll(positionIte.next());
		}
		return list;
	}
	
	public List<OverallPosition> getOverAllPositionList(){
		if( null == allPositionMap)
			return null;
		
		List <OverallPosition> list = new ArrayList<OverallPosition>();
		Iterator <List<OverallPosition>> positionIte = allPositionMap.values().iterator();
		while(positionIte.hasNext()){
			list.addAll(positionIte.next());
		}
		return list;
	}

}
