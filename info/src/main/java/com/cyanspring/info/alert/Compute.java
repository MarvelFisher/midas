package com.cyanspring.info.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ResetAccountRequestEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertRequestEvent;
import com.cyanspring.common.event.alert.QueryPriceAlertRequestEvent;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.event.AsyncEventProcessor;

public abstract class Compute {
	private List<Class<? extends AsyncEvent>> lst;
//	private ConcurrentLinkedQueue<AsyncEvent> sendRemoteEventQueue;
//	private ConcurrentLinkedQueue<AsyncEvent> sendEventQueue;
//	private IRemoteEventManager eventManager;
//	private IRemoteEventManager eventManagerMD;	
	private AsyncEventProcessor eventProcessor ;
	private AsyncEventProcessor eventProcessorMD ;
	public List<Class<? extends AsyncEvent>> getSubscirbetoEventList() {
		setLst(new ArrayList<Class<? extends AsyncEvent>>());
		SubscirbetoEvents();
		return getLst();
	}

	public List<Class<? extends AsyncEvent>> getSubscirbetoEventMDList() {
		setLst(new ArrayList<Class<? extends AsyncEvent>>());
		SubscribetoEventsMD();
		return getLst();
	}

	public abstract void SubscirbetoEvents();

	public abstract void SubscribetoEventsMD();
	
	public abstract void init();
	
	public void SubscirbetoEvent(Class<? extends AsyncEvent> clazz) {
		getLst().add(clazz);
	}

	public void SubscirbetoEventMD(Class<? extends AsyncEvent> clazz) {
		getLst().add(clazz);
	}
	
//	public void initial(ConcurrentLinkedQueue<AsyncEvent> sendRemoteEventQueue ,ConcurrentLinkedQueue<AsyncEvent> sendEventQueue)
//	{
//		this.sendRemoteEventQueue = sendRemoteEventQueue;
//		this.sendEventQueue = sendEventQueue;
//		init() ;
//	}
	
	public void initial(AsyncEventProcessor EventProcessor , AsyncEventProcessor EventProcessorMD)
	{
		this.setEventProcessor(EventProcessor);		
		this.setEventProcessorMD(EventProcessorMD) ;
		init() ;
	}
	
	synchronized public void SendRemoteEvent(RemoteAsyncEvent clazz)
	{
		try {
			((IRemoteEventManager)getEventProcessor().getEventManager()).sendRemoteEvent(clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	synchronized public void SendEvent(AsyncEvent clazz)
	{
		getEventProcessorMD().getEventManager().sendEvent(clazz);
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event,
			List<Compute> computes) {
	}

	public void processQuoteEvent(QuoteEvent event, List<Compute> computes) {
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event,
			List<Compute> computes) {
	}

	public void processQueryPriceAlertRequestEvent(
			QueryPriceAlertRequestEvent event, List<Compute> computes) {
	}

	public void processSetPriceAlertRequestEvent(
			SetPriceAlertRequestEvent event, List<Compute> computes) {
	}

	public void processQueryOrderAlertRequestEvent(
			QueryOrderAlertRequestEvent event, List<Compute> computes) {
	}

	public void processResetAccountRequestEvent(ResetAccountRequestEvent event,
			List<Compute> computes) {
	}

	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event,
			List<Compute> computes) {
	}

	public List<Class<? extends AsyncEvent>> getLst() {
		return lst;
	}

	public void setLst(List<Class<? extends AsyncEvent>> lst) {
		this.lst = lst;
	}

	public AsyncEventProcessor getEventProcessor() {
		return eventProcessor;
	}

	public void setEventProcessor(AsyncEventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public AsyncEventProcessor getEventProcessorMD() {
		return eventProcessorMD;
	}

	public void setEventProcessorMD(AsyncEventProcessor eventProcessorMD) {
		this.eventProcessorMD = eventProcessorMD;
	}
}
