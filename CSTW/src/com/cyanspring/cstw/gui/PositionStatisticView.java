
package com.cyanspring.cstw.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotRequestEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.statistic.AccountNumberReplyEvent;
import com.cyanspring.common.event.statistic.AccountNumberRequestEvent;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.bean.PositionStatisticBean;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class PositionStatisticView extends ViewPart implements IAsyncEventListener{

	private static final Logger log = LoggerFactory.getLogger(PositionStatisticView.class);
	public static final String ID = "com.cyanspring.cstw.gui.PositionStatisticView";
	private DynamicTableViewer openPositionViewer;			
	private int limitAccount = 2000;
	private Action aggregateAction;
	private Action refreshAction;
	private ImageRegistry imageRegistry;
	private final String COLUMN_CLASS="CLASS";
	private ReentrantLock lock = new ReentrantLock();
	private Map <String,PositionStatisticBean> symbolOpMap = new HashMap<String, PositionStatisticBean>();
	private ConcurrentHashMap <String,List<OpenPosition>> accountOpMap = new ConcurrentHashMap<String,List<OpenPosition>>();	
	private AsyncTimerEvent refreshEvent = new AsyncTimerEvent();
	private long maxRefreshInterval = 1000;
	
	@Override
	public void onEvent(AsyncEvent event) {
		
		if(event instanceof AsyncTimerEvent){
			
			displayOpenPosition();		
			
		}else if( event instanceof AllPositionSnapshotReplyEvent){

			AllPositionSnapshotReplyEvent allPosition = (AllPositionSnapshotReplyEvent) event;
			List <OpenPosition>ops = allPosition.getOpenPositionList();
			collectAllPosition(ops);
			displayOpenPosition();
			
		}else if(event instanceof OpenPositionUpdateEvent){
			
			OpenPosition position = ((OpenPositionUpdateEvent) event).getPosition();
			updatePosition(position);
			
		}else if(event instanceof OpenPositionDynamicUpdateEvent){

			OpenPosition position = ((OpenPositionDynamicUpdateEvent) event).getPosition();
			updatePosition(position);
			
		}else if(event instanceof ClosedPositionUpdateEvent){

			ClosedPosition position = ((ClosedPositionUpdateEvent) event).getPosition();
			closePosition(position);	
		
		}else if(event instanceof AccountNumberReplyEvent){

			AccountNumberReplyEvent accountNum = (AccountNumberReplyEvent) event;
			if(accountNum.isOk()){
				log.info("Account Number:{}",accountNum.getAccountNumber());
				if( limitAccount > accountNum.getAccountNumber()){
					
					sendAllPositionRequestEvent();
				}else{
					log.error("Total account :"+accountNum.getAccountNumber()+" exceed account limit:"+limitAccount);
					showDialog("Total account :"+accountNum.getAccountNumber()+" exceed account limit:"+limitAccount);
					refreshAction.setEnabled(false);
				}
			}else{
				log.error(accountNum.getErrorMessage());
				showDialog(accountNum.getErrorMessage());
				refreshAction.setEnabled(false);
			}
		}
	}

	private void closePosition(ClosedPosition position) {
		String account = position.getAccount();
		List<OpenPosition> tempList = new ArrayList<OpenPosition>();
		if(accountOpMap.containsKey(account)){
			List <OpenPosition>oldList = accountOpMap.get(account);
			for(OpenPosition op:oldList){
				if(!position.getSymbol().equals(op.getSymbol())){
					tempList.add(op);
				}
			}
			accountOpMap.put(account, tempList);
		}		
		
	}

	private void updatePosition(OpenPosition position) {
		
		String account = position.getAccount();
		List<OpenPosition> tempList = new ArrayList<OpenPosition>();
		boolean isNewSymbol = true;
		if(accountOpMap.containsKey(account)){
			List <OpenPosition>oldList = accountOpMap.get(account);
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
			accountOpMap.put(account, tempList);
		}else{
			tempList = new ArrayList<OpenPosition>();
			tempList.add(position);
			accountOpMap.put(account, tempList);
		}
	}

	private void collectAllPosition(List<OpenPosition> ops) {
		
		log.info("Collect All position:{}",ops.size());
		for(OpenPosition op:ops){			
			List <OpenPosition>opList = null;
			String account = op.getAccount();
			if(accountOpMap.containsKey(account)){
				opList = accountOpMap.get(account);
				boolean isSymbolExist = false;
				for(OpenPosition oldOp : opList){
					if(oldOp.getSymbol().equals(op.getSymbol())){
						isSymbolExist = true;
						oldOp.addQty(op.getQty());
						oldOp.setPnL(op.getPnL()+oldOp.getPnL());
						oldOp.setAcPnL(op.getAcPnL()+oldOp.getAcPnL());
						oldOp.setMargin(op.getMargin()+oldOp.getMargin());
						oldOp.setPrice((op.getPrice()+oldOp.getPrice())/2);
					}
				}
				if(!isSymbolExist)
					opList.add(op);
			}else{
				opList = new ArrayList<OpenPosition>();
				opList.add(op);
			}	
			accountOpMap.put(account, opList);
		}
	}

	private void caculate(OpenPosition op) {
		PositionStatisticBean bean = null;
		String symbol = op.getSymbol();
//		log.info("Account:{}, symbol:{} ,Qty:{},Avaliable Qty:{}",new Object[]{op.getAccount(),symbol,op.getQty(),op.getAvailableQty()});
		if(symbolOpMap.containsKey(symbol)){
			bean = symbolOpMap.get(op.getSymbol());
		}else{
			bean = new PositionStatisticBean();
			bean.setSymbol(op.getSymbol());
		}
		
		bean.setQty(op.getQty()+bean.getQty());
		bean.setPnL(op.getPnL()+bean.getPnL());
		bean.setAcPnL(op.getAcPnL()+bean.getAcPnL());
		bean.setMargin(op.getMargin()+bean.getMargin());

		if(PriceUtils.isZero(bean.getQty()))
			return;
		
		if(null != bean)
			symbolOpMap.put(symbol, bean);
	}
	
	@Override
	public void createPartControl(Composite parent) {
	
		subEvent(AllPositionSnapshotReplyEvent.class);
		subEvent(AccountNumberReplyEvent.class);
		subEvent(OpenPositionUpdateEvent.class,null);
		subEvent(OpenPositionDynamicUpdateEvent.class,null);		
		subEvent(ClosedPositionUpdateEvent.class,null);
		subEvent(ExecutionUpdateEvent.class,null);
		subEvent(ClosePositionReplyEvent.class,null);
		
		
		imageRegistry = Activator.getDefault().getImageRegistry();
		final Composite  mainComposite = new Composite(parent,SWT.BORDER);
		mainComposite.setLayout (new FillLayout());
	
		createOpenPositionViewer(mainComposite);
		createAggregateAction(parent);
		createRefreshAction(parent);
		
		AccountNumberRequestEvent accountNumberEvent = new AccountNumberRequestEvent(ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(accountNumberEvent);
		try{
			scheduleJob(refreshEvent, maxRefreshInterval);
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	
	private void sendAllPositionRequestEvent() {
		accountOpMap.clear();
		symbolOpMap.clear();	
		log.info("send AllPositionSnapshotRequestEvent");
		AllPositionSnapshotRequestEvent request = new AllPositionSnapshotRequestEvent(ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(request);		
	}

	public void displayOpenPosition(){
		
		try{
			lock.lock();
			if(!aggregateAction.isChecked()){
				showAllOpenPosition();
			}else{
				showAggregateOpenPosition();
			}
		}finally{
			lock.unlock();
		}
	}
	
	private void showAggregateOpenPosition() {
		
		symbolOpMap = new HashMap<String, PositionStatisticBean>();
		final List <OpenPosition>opList = getAllOpenPositionList();

		for(OpenPosition op : opList){
			caculate(op);
		}		
		
		final List <PositionStatisticBean>psbList = new ArrayList<PositionStatisticBean>();
		psbList.addAll(symbolOpMap.values());
		
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					
					if(openPositionViewer.isViewClosing())
						return;
					
					if(null == psbList || psbList.isEmpty()){
						openPositionViewer.setInput(psbList);			
						openPositionViewer.refresh();
						return;
					}
					List<ColumnProperty> properties = openPositionViewer
							.setObjectColumnProperties(psbList.get(0));				
					properties = filterColumn(properties);

					if(properties.size() < openPositionViewer.getComparator().getColumn())
						openPositionViewer.getComparator().setColumn(0);
					
					openPositionViewer.setSmartColumnProperties(psbList.get(0).getClass().getName(),properties);
					openPositionViewer.setInput(psbList);			
					openPositionViewer.refresh();
				}
			}
		});
		
	}
	private void showAllOpenPosition() {
		
		final List <OpenPosition>psbList = getAllOpenPositionList();
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					
					if(openPositionViewer.isViewClosing())
						return;
					
					if(null == psbList || psbList.isEmpty()){
						openPositionViewer.setInput(psbList);			
						openPositionViewer.refresh();
						return;
					}
					
					List<ColumnProperty> properties = openPositionViewer
							.setObjectColumnProperties(psbList.get(0));
					openPositionViewer.setSmartColumnProperties(psbList.get(0).getClass().getName(),properties);
					openPositionViewer.setInput(psbList);
					openPositionViewer.refresh();
				}
			}
		});
	}
	private List<ColumnProperty> filterColumn(List<ColumnProperty> properties){
		
		List<ColumnProperty>  propList= new ArrayList<ColumnProperty>();
		for(ColumnProperty cp : properties){
			if(!cp.getTitle().toUpperCase().equals(COLUMN_CLASS)){
				propList.add(cp);
			}
		}		
		return propList;
	}
	
	private List<OpenPosition> getAllOpenPositionList(){
		
		final List <OpenPosition>psbList = new ArrayList<OpenPosition>();
		Iterator<List<OpenPosition>> opIterator = accountOpMap.values().iterator();
		while(opIterator.hasNext()){	
			List <OpenPosition> ops = opIterator.next();
			for(OpenPosition op : ops){
				if(!PriceUtils.isZero(op.getQty()))
					psbList.add(op);
			}
		}
		return psbList;
	}
	private void createRefreshAction(final Composite parent){
		
		refreshAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				if(refreshAction.isChecked()){
					scheduleJob(refreshEvent, maxRefreshInterval);
				}else{
					cancelScheduleJob(refreshEvent);
				}
			}
		};
		
		refreshAction.setChecked(true);		
		refreshAction.setText("Auto Refresh");
		refreshAction.setToolTipText("Auto Refresh");	
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.REFRESH_ICON.toString());	
		refreshAction.setImageDescriptor(imageDesc);	
		getViewSite().getActionBars().getToolBarManager().add(refreshAction);
	}
	private void createAggregateAction(final Composite parent){
		
		aggregateAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				if(!aggregateAction.isChecked()) {
					showAllOpenPosition();
				} else { 
					showAggregateOpenPosition();
				}
			}
		};	
		
		aggregateAction.setChecked(false);		
		aggregateAction.setText("Aggregate");
		aggregateAction.setToolTipText("Aggregate");	
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FILTER_ICON.toString());	
		aggregateAction.setImageDescriptor(imageDesc);		
		getViewSite().getActionBars().getToolBarManager().add(aggregateAction);
	}
	
	private void createOpenPositionViewer(Composite parent) {
	    String strFile = Business.getInstance().getConfigPath() + "OpenPositionTable.xml";
		openPositionViewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		openPositionViewer.init();
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	@Override
	public void dispose() {
		Business.getInstance().getScheduleManager().cancelTimerEvent(refreshEvent);		
		unSubEvent(AllPositionSnapshotReplyEvent.class);
		unSubEvent(AccountNumberReplyEvent.class);
		unSubEvent(OpenPositionUpdateEvent.class,null);
		unSubEvent(OpenPositionDynamicUpdateEvent.class,null);		
		unSubEvent(ClosedPositionUpdateEvent.class,null);
		unSubEvent(ExecutionUpdateEvent.class,null);
		unSubEvent(ClosePositionReplyEvent.class,null);
		super.dispose();
	}
	@Override
	public void setFocus() {
		
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz,String ID){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz,String ID){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
	}

	private void scheduleJob(AsyncTimerEvent timerEvent,
			long maxRefreshInterval) {
		
		Business.getInstance().getScheduleManager().scheduleRepeatTimerEvent(maxRefreshInterval,
				PositionStatisticView.this, timerEvent);
	}

	private void cancelScheduleJob(AsyncTimerEvent timerEvent) {
		Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);		
	}

	public void showDialog(final String msg){
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(openPositionViewer.getControl().getShell(), "Open Position Statistic", msg);
			}
		
		});
	}
}
