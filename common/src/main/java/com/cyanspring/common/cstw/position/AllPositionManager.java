package com.cyanspring.common.cstw.position;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.OverAllPositionReplyEvent;
import com.cyanspring.common.event.account.OverAllPositionRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;

public class AllPositionManager implements IAsyncEventListener {

	private static final Logger log = LoggerFactory.getLogger(AllPositionManager.class);
	private IRemoteEventManager eventManager;
	private String server = "";
	private List<String> accountIdList = null;
	private UserGroup loginUser = null;
	private ConcurrentHashMap<String, List<OverallPosition>> allPositionMap = new ConcurrentHashMap<String, List<OverallPosition>>();
	private ConcurrentHashMap<String, List<OpenPosition>> openPositionMap = new ConcurrentHashMap<String, List<OpenPosition>>();
	private ConcurrentHashMap<String, List<ClosedPosition>> closedPositionMap = new ConcurrentHashMap<String, List<ClosedPosition>>();
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
		}
	}
	
	private void updateClosedPositionList(
			List<ClosedPosition> closedPositionList) {
		closedPositionMap.clear();
		for(ClosedPosition pos : closedPositionList){
			log.info(" update close pos:{},{},{},{},{}",new Object[]{pos.getSymbol(),pos.getQty(),pos.getPnL(),pos.getBuyPrice(),pos.getSellPrice()});
			updatePosition(pos,false);
		}		
	}

	private void updateOpenPositionList(List<OpenPosition> openPositionList) {
		openPositionMap.clear();
		for(OpenPosition pos : openPositionList){
			log.info(" update open pos:{},{},{},{}",new Object[]{pos.getSymbol(),pos.getQty(),pos.getPnL(),pos.getPrice()});
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
					tempList.add(position);
				}else{
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
			log.info("account:{}",key);
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
			log.info("account:{}",key);
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
				log.info(" -all:{}, {}, {}, {}, {}, {}, {}"
						,new Object[]{op.getAccount(),op.getSymbol(),op.getBuyPrice(),op.getBuyQty(),op.getSellPrice(),op.getSellQty(),op.getTotalQty()});
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

	public OverallPosition toOverallPosition(String accountId,String symbol,List<OpenPosition> opList, List<ClosedPosition> cpList){
		OverallPosition oap = new OverallPosition();
		oap.setAccount(accountId);
		oap.setSymbol(symbol);
		for(OpenPosition op : opList){
			if(!op.getSymbol().equals(symbol))
				continue;
			
			double qty = op.getQty();
			double price = op.getPrice();
			if(op.isBuy()){
				if(PriceUtils.isZero(oap.getBuyPrice()))
					oap.setBuyPrice(price);
				else
					oap.setBuyPrice((oap.getBuyPrice()+price)/2);
				
				oap.setBuyQty(oap.getBuyQty()+Math.abs(qty));
				
			}else{
				if(PriceUtils.isZero(oap.getSellPrice()))
					oap.setSellPrice(price);
				else
					oap.setSellPrice((oap.getSellPrice()+price)/2);
				
				oap.setSellQty(oap.getSellQty()+Math.abs(qty));
				
			}
			
			oap.setPnL(op.getPnL()+oap.getPnL());
			oap.setUrPnL(op.getPnL()+oap.getUrPnL());
		}
		
		for(ClosedPosition cp : cpList){
			if(!cp.getSymbol().equals(symbol))
				continue;
			
			double qty = cp.getQty();
			
			if(PriceUtils.isZero(oap.getBuyPrice()))
				oap.setBuyPrice(cp.getBuyPrice());
			else
				oap.setBuyPrice((oap.getBuyPrice()+cp.getBuyPrice())/2);
			
			if(PriceUtils.isZero(oap.getSellPrice()))
				oap.setSellPrice(cp.getSellPrice());
			else
				oap.setSellPrice((oap.getSellPrice()+cp.getSellPrice())/2);
			
			if(cp.isBuy())
				oap.setBuyQty(oap.getBuyQty()+Math.abs(qty));
			else
				oap.setSellQty(oap.getSellQty()+Math.abs(qty));

			oap.setPnL(cp.getPnL()+oap.getPnL());

		}
		oap.setLastUpdate(new Date());
		return oap;
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
			for(String symbol : symbolSet){
				log.info("symbol set:{},{}",id,symbol);
				OverallPosition oap = toOverallPosition(id,symbol,oList,cList);
				tempAllPositionSet.add(oap);
			}		
			
			log.info("refresh all position:{},size:{}",id,tempAllPositionSet.size());
			allList.addAll(tempAllPositionSet);
			allPositionMap.put(id, allList);
		}
		printOverallPositionMap();
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
			,String server,List<String> accountIdList,UserGroup loginUser){
		
		log.info("init AllPositionManager");
		
		if(null == eventManager || !StringUtils.hasText(server)){
			log.info("init error: eventManager is null or server is empty");
		}
		
		this.eventManager = eventManager;
		this.server = server;
		this.accountIdList = accountIdList;
		this.loginUser = loginUser;
		
		if( null == accountIdList){
			accountIdList = new ArrayList<String>();
		}
		
		subEvent(OverAllPositionReplyEvent.class);
		subEvent(OpenPositionUpdateEvent.class);
		subEvent(OpenPositionDynamicUpdateEvent.class);
		subEvent(ClosedPositionUpdateEvent.class);
		if(null != loginUser && loginUser.isAdmin()){
			requestOverAllPosition(null);
			return;
		}
		requestOverAllPosition(accountIdList);
	}
	
	public void unInit(){
		unSubEvent(OverAllPositionReplyEvent.class);
		unSubEvent(OpenPositionUpdateEvent.class);
		unSubEvent(OpenPositionDynamicUpdateEvent.class);
		unSubEvent(ClosedPositionUpdateEvent.class);
	}
	
	
	public void addIPositionChangeListener(IPositionChangeListener listener) {
		listenerList.add(listener);
	}

	public void removeIPositionChangeListener(IPositionChangeListener listener) {
		listenerList.remove(listener);
	}
	
	public void requestOverAllPosition(List<String> accountIdList) {
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
