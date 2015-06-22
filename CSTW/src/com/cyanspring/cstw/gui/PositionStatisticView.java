package com.cyanspring.cstw.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotReplyEvent;
import com.cyanspring.common.event.account.AllPositionSnapshotRequestEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.ExecutionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.bean.PositionStatisticBean;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.common.StyledAction;

public class PositionStatisticView extends ViewPart implements IAsyncEventListener{

	private static final Logger log = LoggerFactory.getLogger(PositionStatisticView.class);
	public static final String ID = "com.cyanspring.cstw.gui.PositionStatisticView";
//	private Composite topComposite;
	private DynamicTableViewer openPositionViewer;			

	private Action aggregateAction;
	private Action refreshAction;
	private ImageRegistry imageRegistry;
	private final String COLUMN_CLASS="CLASS";
	private Map <String,PositionStatisticBean> symbolOpMap = new HashMap<String, PositionStatisticBean>();
	private ConcurrentHashMap <String,List<OpenPosition>> accountOpMap = new ConcurrentHashMap<String,List<OpenPosition>>();
	@Override
	public void onEvent(AsyncEvent event) {
		
		if( event instanceof AllPositionSnapshotReplyEvent){
			log.info("get reply AllPositionSnapshotReplyEvent");
			AllPositionSnapshotReplyEvent allPosition = (AllPositionSnapshotReplyEvent) event;
			List <OpenPosition>ops = allPosition.getOpenPositionList();
			collectAllPosition(ops);
			displayOpenPosition();
		}else if(event instanceof OpenPositionUpdateEvent){
			log.info("get reply OpenPositionUpdateEvent");
			OpenPosition position = ((OpenPositionUpdateEvent) event).getPosition();
			caculate(position);
			
		}else if(event instanceof OpenPositionDynamicUpdateEvent){
			log.info("get reply OpenPositionDynamicUpdateEvent");
			OpenPosition position = ((OpenPositionDynamicUpdateEvent) event).getPosition();
			caculate(position);
		}else if(event instanceof ClosedPositionUpdateEvent){
			log.info("get reply Clo"
					+ "sedPositionUpdateEvent");

		
		}else if(event instanceof ExecutionUpdateEvent){
			log.info("get reply ExecutionUpdateEvent");

		
		}else if(event instanceof OpenPositionDynamicUpdateEvent){
			log.info("get reply OpenPositionDynamicUpdateEvent");


		}else if(event instanceof ClosePositionReplyEvent){
			log.info("get reply ClosePositionReplyEvent");

		
		}
	}

	private void collectAllPosition(List<OpenPosition> ops) {
		
		for(OpenPosition op:ops){			
			List <OpenPosition>opList = null;
			String account = op.getAccount();
			log.info("account:{},position:{}",account,op.getSymbol());
			if(accountOpMap.containsKey(account)){
				opList = accountOpMap.get(account);
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
		log.info("Account:{}, symbol:{} ,Qty:{},Avaliable Qty:{}",new Object[]{op.getAccount(),symbol,op.getQty(),op.getAvailableQty()});
		if(symbolOpMap.containsKey(symbol)){
			bean = symbolOpMap.get(op.getSymbol());
			bean.setQty(op.getQty()+bean.getQty());
			bean.setPnL(op.getPnL()+bean.getPnL());
		}else{
			bean = new PositionStatisticBean();
			bean.setSymbol(op.getSymbol());
			bean.setQty(op.getQty()+bean.getQty());
			bean.setPnL(op.getPnL()+bean.getPnL());
		}
		
		if(null != bean)
			symbolOpMap.put(symbol, bean);
	}

	@Override
	public void createPartControl(Composite parent) {
		
		subEvent(AllPositionSnapshotReplyEvent.class);
		subEvent(OpenPositionUpdateEvent.class);
		subEvent(OpenPositionDynamicUpdateEvent.class);
		
		subEvent(ClosedPositionUpdateEvent.class);
		subEvent(ExecutionUpdateEvent.class);
		subEvent(OpenPositionDynamicUpdateEvent.class);
//		subEvent(ClosePositionReplyEvent.class,Business.getInstance().getFirstServer());
		
//		Business.getInstance().getEventManager().subscribe(ClosePositionReplyEvent.class, 
//				"test9-FX",(IAsyncExecuteEventListener)PositionView.class );
		
		imageRegistry = Activator.getDefault().getImageRegistry();


		
		final Composite mainComposite = new Composite(parent,SWT.BORDER);
		mainComposite.setLayout (new FillLayout());

		
		createOpenPositionViewer(mainComposite);
		createAggregateAction(parent);
		createRefreshAction(parent);
		
		log.info("send AllPositionSnapshotRequestEvent");
		sendAllPositionRequestEvent();
	
	}
	private void sendAllPositionRequestEvent() {
		
		accountOpMap.clear();
		symbolOpMap.clear();
		
		AllPositionSnapshotRequestEvent request = new AllPositionSnapshotRequestEvent(
		ID, Business.getInstance().getFirstServer());
		sendRemoteEvent(request);		
	}

	public void displayOpenPosition(){
		if(!aggregateAction.isChecked()){
			log.info("showAllOpenPosition");
			showAllOpenPosition();
		}else{
			log.info("showAggregateOpenPosition");
			showAggregateOpenPosition();
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
					
					List<ColumnProperty> properties = openPositionViewer
							.setObjectColumnProperties(psbList.get(0));
					
					properties = filterColumn(properties);

					openPositionViewer.setSmartColumnProperties(psbList.get(0).getClass().getName(),
							properties);
					openPositionViewer.setInput(psbList);
				
					openPositionViewer.refresh();
				}
			}
		});
		
	}

	private void showAllOpenPosition() {
		log.info("accountOpMap:{}",accountOpMap.size());
		final List <OpenPosition>psbList = getAllOpenPositionList();

		log.info("psbList:{}",psbList.size());
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					
					if(openPositionViewer.isViewClosing())
						return;
					
					List<ColumnProperty> properties = openPositionViewer
							.setObjectColumnProperties(psbList.get(0));
					openPositionViewer.setSmartColumnProperties(psbList.get(0).getClass().getName(),
							properties);
					openPositionViewer.setInput(psbList);
					
//					
//					if(null == PositionStatisticView.this.symbolOpMap || openPositionViewer.isViewClosing())
//						return;
//					
//					if(PositionStatisticView.this.symbolOpMap.size() > 0)
//						createOpenPositionColumns(psbList);
//					
//					openPositionViewer.setInput(psbList);

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
			psbList.addAll(opIterator.next());
		}
		return psbList;
	}
	private void createRefreshAction(final Composite parent){
		
		refreshAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_PUSH_BUTTON) {
			public void run() {
				sendAllPositionRequestEvent();
			}
		};
		
		refreshAction.setChecked(false);		
		refreshAction.setText("Refreash");
		refreshAction.setToolTipText("Refreash");	
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
	public void setFocus() {
		
	}
	
	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
	}
	private void subEvent(Class<? extends AsyncEvent> clazz,String ID){
		Business.getInstance().getEventManager().subscribe(clazz,ID, this);		
	}
	
	private void displayObject() {
		
		final List <PositionStatisticBean>psbList = new ArrayList<PositionStatisticBean>();
		psbList.addAll(symbolOpMap.values());		
		
		log.info("psbList:{}",psbList.size());
		openPositionViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized(openPositionViewer) {
					if(null == PositionStatisticView.this.symbolOpMap || openPositionViewer.isViewClosing())
						return;
					
					if(PositionStatisticView.this.symbolOpMap.size() > 0)
						createOpenPositionColumns(psbList);
					
					openPositionViewer.setInput(PositionStatisticView.this.symbolOpMap);

					openPositionViewer.refresh();
				}
			}
		});
	}
	
	private boolean openPositionColumnsCreated = false;
	private void createOpenPositionColumns(List  positions) {
		if(!openPositionColumnsCreated) {
			Object obj = positions.get(0);
			List<ColumnProperty> properties = openPositionViewer.setObjectColumnProperties(obj);
			
			String[] from = {"PnL"};
			String[] to = {"UrP&L"};
			openPositionViewer.setTitleMap(from, to);
			openPositionViewer.setSmartColumnProperties(obj.getClass().getName(), properties);
			openPositionViewer.setInput(positions);
			openPositionColumnsCreated = true;
		}
	}
}
