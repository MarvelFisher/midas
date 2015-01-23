package com.cyanspring.info.alert;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.alert.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.alert.AlertType;
import com.cyanspring.common.event.alert.PriceAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertReplyEvent;
import com.cyanspring.common.event.alert.QueryOrderAlertRequestEvent;
import com.cyanspring.common.event.alert.QueryPriceAlertRequestEvent;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.event.AsyncEventProcessor;

public class AlertManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);

	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	SessionFactory sessionFactory;
	
	private int timeoutSecond ;
	private int createThreadCount ;
	private int maxRetrytimes ;
	private long killTimeoutSecond ;
	
	private boolean tradeAlert;
	private boolean priceAlert;
	private static final String MSG_TYPE_PRICE = "1";
	private static final String MSG_TYPE_ORDER = "2";
	
	private String parseApplicationId;
	private String parseRestApiId;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private int CheckThreadStatusInterval = 60000; // 60 seconds
	
	public ConcurrentLinkedQueue<ParseData> ParseDataQueue ;
	private ArrayList<ParseThread> ParseThreadList ;
	
	private Map<String, ArrayList<BasePriceAlert>> symbolPriceAlerts = new HashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<BasePriceAlert>> userPriceAlerts = new HashMap<String, ArrayList<BasePriceAlert>>();
	private Map<String, ArrayList<TradeAlert>> userTradeAlerts = new HashMap<String, ArrayList<TradeAlert>>();
	private int maxNoOfAlerts = 20;
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(ChildOrderUpdateEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(SetPriceAlertRequestEvent.class, null);
			subscribeToEvent(QueryPriceAlertRequestEvent.class, null);
			subscribeToEvent(QueryOrderAlertRequestEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}
	};
	
	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		Execution execution = event.getExecution();
		if(null == execution)
			return;
		
		log.debug("[processUpdateChildOrderEvent] "+ execution.toString());		
		if(null != ParseDataQueue)
		{
			ParseDataQueue.add(PackTradeAlert(execution));
			receiveChildOrderUpdateEvent(execution) ;
		}
		else
		{
			log.error("ParseDataQueue not ready!!");
			return ;
		}
	}
	
	private void receiveChildOrderUpdateEvent(Execution execution)
	{
		try
		{
			DecimalFormat qtyFormat = new DecimalFormat("#0");		
			String strQty = qtyFormat.format(execution.getQuantity());		
			DecimalFormat priceFormat = new DecimalFormat("#0.#####");
			String strPrice = priceFormat.format(execution.getPrice());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String Datetime = dateFormat.format(execution.get(Date.class, "Created"));
			String tradeMessage = "Trade " + execution.getSymbol() + " " + 
								execution.getSide().toString() + " " + strQty + "@" + strPrice;
			TradeAlert TA = new TradeAlert(execution.getUser(), execution.getSymbol(), null ,execution.getQuantity(), execution.getPrice(),Datetime, tradeMessage);
			//save to Array
			ArrayList<TradeAlert> list ;
			int search;
			list = userTradeAlerts.get(execution.getUser());
			if (null == list)
			{
				list = new ArrayList<TradeAlert>();
				list.add(TA);
				userTradeAlerts.put(execution.getUser(),list) ;
			}
			else
			{
				if (list.contains(TA))
				{
					log.warn("[UpdateChildOrderEvent][WARNING] : ChildOrderEvent already exists.");
					return ;
				}
				else
				{
					if (list.size() >= 20)
					{
						list.remove(19);
						list.add(0,TA);
					}
					else
					{
						list.add(0,TA);
					}
				}			
			}
			//save to SQL
			SQLSave(TA);
		}
		catch (Exception e)
		{
			log.warn("[receiveChildOrderUpdateEvent] : " + e.getMessage());
		}
	}
	
	public void processQueryOrderAlertRequestEvent(QueryOrderAlertRequestEvent event)
	{
		AlertType type = event.getType();
		QueryOrderAlertReplyEvent queryorderalertreplyevent = null;
		if (type == AlertType.TRADE_QUERY_PAST)
		{
			ArrayList<TradeAlert> list = userTradeAlerts.get(event.getuserId());
			if (null == list)
			{
				log.info("[processQueryOrderAlertRequestEvent] : user OrderAlert list isn't exists.") ;
				//Send orderalert event reply
				queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),null,event.getTxId(),false,"userOrderAlert list isn't exists");
			}
			else
			{
				//Send orderalert event reply
				queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),list,event.getTxId(),true,"");
			}
			try {
				eventManagerMD.sendRemoteEvent(queryorderalertreplyevent);
			} catch (Exception e) {
				log.warn("[processQueryOrderAlertRequestEvent] : " + e.getMessage());
			}
		}
		else
		{
			log.warn("[receiveQueryOrderAlertRequestEvent] AlertType error." + type.toString());
		}
	}
	
	public void processQuoteEvent(QuoteEvent event) {		
		Quote quote = event.getQuote();
		if (quotes.get(quote.getSymbol()) == null)
		{
			quotes.put(quote.getSymbol(), quote);//���洵銝����
			return ;
		}
		//頝istPrice��撠�
		log.debug("Quote: " + quote);
		ArrayList<BasePriceAlert> list = symbolPriceAlerts.get(quote.getSymbol());
		if(null == list)
			return;
		else
		{
			for(BasePriceAlert alert: list) {
				if (ComparePriceQuoto(alert, quotes.get(quote.getSymbol()), quote))
				{
					//Add Alert to ParseQueue
					ParseDataQueue.add(PackPriceAlert(alert));					
					//Add Alert to PastSQL
					PastPriceAlert pastPriceAlert = new PastPriceAlert(alert.getUserId(),alert.getSymbol(),alert.getPrice(),alert.getDateTime(),alert.getContent());
					pastPriceAlert.setId(alert.getId());
					SQLSave(pastPriceAlert);
					//Delete Alert from CurSQL
					CurPriceAlert curPriceAlert = new CurPriceAlert(alert.getUserId(),alert.getSymbol(),alert.getPrice(),alert.getDateTime(),alert.getContent());
					curPriceAlert.setId(alert.getId());
					SQLDelete(curPriceAlert);
					//Delete Alert from List
					list.remove(alert);
				}
				else
				{
					return ;
				}
			}
		}
		quotes.put(quote.getSymbol(), quote);
		
	}
	
	private boolean ComparePriceQuoto(BasePriceAlert alert, Quote Previousquoto, Quote quote)
	{
		double alertPrice = alert.getPrice();
		double PreviousPrice = getAlertPrice(Previousquoto);
		double currentPrice = getAlertPrice(quote);
		if (PriceUtils.GreaterThan(alertPrice, PreviousPrice))
		{
			if (PriceUtils.GreaterThan(alertPrice, currentPrice))
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		else if (PriceUtils.EqualLessThan(alertPrice, PreviousPrice))
		{
			if (PriceUtils.GreaterThan(alertPrice, currentPrice))
			{
				return true ;
			}
			else
			{
				return false ;
			}
		}
		return false;
	}

	public void processSetPriceAlertRequestEvent(SetPriceAlertRequestEvent event) {
		AlertType type = event.getType();
		if (type == AlertType.PRICE_SET_NEW)
		{
			receiveAddPriceAlert(event);
		}
		else if (type == AlertType.PRICE_SET_MODIFY)
		{
			receiveModifyPriceAlert(event);
		}
		else if (type == AlertType.PRICE_SET_CANCEL)
		{
			receiveCancelPriceAlert(event);
		}
	}
	
	public void processQueryPriceAlertRequestEvent(QueryPriceAlertRequestEvent event) {
		AlertType type = event.getType();
		if (type == AlertType.PRICE_QUERY_CUR)
		{
			receiveQueryCurPriceAlert(event);
		}
		else if (type == AlertType.PRICE_QUERY_PAST)
		{
			receiveQueryPastPriceAlert(event);
		}
	}
	
	private boolean receiveAddPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		BasePriceAlert priceAlert = event.getPriceAlert();
		int search;
		PriceAlertReplyEvent pricealertreplyevent = null;
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			list = new ArrayList<BasePriceAlert>();
			//do new PriceAlert
			list.add(priceAlert);
			symbolPriceAlerts.put(priceAlert.getSymbol(), list);
			//save to SQL
			CurPriceAlert curPriceAlert = new CurPriceAlert(priceAlert.getUserId(),priceAlert.getSymbol(),priceAlert.getPrice(),priceAlert.getDateTime(),priceAlert.getContent());
			SQLSave(curPriceAlert);
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
		}
		else
		{
			search = Collections.binarySearch(list, priceAlert);
			if (search > 0)
			{
				log.warn("[recevieAddPriceAlert] : id already exists. -> reject");
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id already exists.");
			}
			else
			{
				//do new PriceAlert
				list.add(~search, priceAlert);
				//save to SQL
				CurPriceAlert curPriceAlert = new CurPriceAlert(priceAlert.getUserId(),priceAlert.getSymbol(),priceAlert.getPrice(),priceAlert.getDateTime(),priceAlert.getContent());
				SQLSave(curPriceAlert);
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);				
			}
		}
		try {
			eventManagerMD.sendRemoteEvent(pricealertreplyevent);
		} catch (Exception e) {					
			log.warn("[recevieAddPriceAlert] : " + e.getMessage());
		}
		return true;
	}
	
	private boolean receiveModifyPriceAlert(SetPriceAlertRequestEvent event) {			
		ArrayList<BasePriceAlert> list;
		int search;
		BasePriceAlert priceAlert = event.getPriceAlert();
		PriceAlertReplyEvent pricealertreplyevent = null;
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			log.warn("[receiveModifyPriceAlert] : id isn't exists. -> reject");			
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
		}
		else
		{
			search = Collections.binarySearch(list, priceAlert);
			if (search > 0)
			{
				//do modify PriceAlert
				list.get(search).modifyPriceAlert(priceAlert);
				//update to SQL
				SQLUpdate(priceAlert);
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
			}
			else
			{				
				log.warn("[receiveModifyPriceAlert] : id isn't exists. -> reject");				
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
			}
		}
		try {
			eventManagerMD.sendRemoteEvent(pricealertreplyevent);
		} catch (Exception e) {					
			log.warn("[recevieAddPriceAlert] : " + e.getMessage());
		}
		return true;
	}
	
	private boolean receiveCancelPriceAlert(SetPriceAlertRequestEvent event) {
		ArrayList<BasePriceAlert> list;
		int search;
		BasePriceAlert priceAlert = event.getPriceAlert();
		PriceAlertReplyEvent pricealertreplyevent = null;
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			log.warn("[receiveCancelPriceAlert] : id isn't exists. ->reject");
			//SendPriceAlertreplyEvent
			pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
		}
		else
		{
			search = Collections.binarySearch(list, priceAlert);
			if (search > 0)
			{
				list.remove(priceAlert);
				//update to SQL
				SQLDelete(priceAlert);
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),null,event.getTxId(),priceAlert.getUserId(),event.getType(),list,true,null);
			}
			else
			{
				log.warn("[receiveCancelPriceAlert] : id isn't exists. ->reject");
				//SendPriceAlertreplyEvent
				pricealertreplyevent = new PriceAlertReplyEvent(null, event.getSender(),priceAlert.getId(),event.getTxId(),priceAlert.getUserId(),event.getType(),null,false,"id isn't exists.");
			}
		}
		return true;
	}

	private boolean receiveQueryCurPriceAlert(QueryPriceAlertRequestEvent event)
	{
		ArrayList<BasePriceAlert> list = userPriceAlerts.get(event.getUserId());
		if (null == list)
		{
			log.info("[processQueryOrderAlertRequestEvent] : user OrderAlert list isn't exists.") ;
			//Send orderalert event reply
//			queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),null,event.getTxId(),false,"userOrderAlert list isn't exists");
		}
		else
		{
			//Send orderalert event reply
//			queryorderalertreplyevent = new QueryOrderAlertReplyEvent(null, event.getSender(),list,event.getTxId(),true,"");
		}
		try {
//			eventManagerMD.sendRemoteEvent(queryorderalertreplyevent);
		} catch (Exception e) {
			log.warn("[processQueryOrderAlertRequestEvent] : " + e.getMessage());
		}
		return true ;
	}
	
	private boolean receiveQueryPastPriceAlert(QueryPriceAlertRequestEvent event)
	{
		return true ;
	}
	
	public <T> void SQLSave(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.save(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLSave] : " + e.getMessage());
		}
	}
	
	private <T> void SQLUpdate(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.update(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLUpdate] : " + e.getMessage());
		}
	}
	
	private <T> void SQLDelete(T object)
	{
		try
		{
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			session.delete(object);
			tx.commit();
			session.close();
		}
		catch (Exception e)
		{
			log.warn("[SQLDelete] : " + e.getMessage());
		}
	}
	
	private double getAlertPrice(Quote quote) {
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	public ParseData PackTradeAlert(Execution execution) {
		DecimalFormat qtyFormat = new DecimalFormat("#0");
		String strQty = qtyFormat.format(execution.getQuantity());
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(execution.getPrice());
		String tradeMessage = "Trade " + execution.getSymbol() + " " + 
				execution.getSide().toString() + " " + strQty + "@" + strPrice;
		String user = execution.getUser();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());
		String keyValue = execution.getSymbol() + "," + strPrice + "," + strQty + "," + (execution.getSide().isBuy()?"BOUGHT":"SOLD");
		return new ParseData(user, tradeMessage, user + IdGenerator.getInstance().getNextID(),
				MSG_TYPE_ORDER, strDate, keyValue);
	}

	public ParseData PackPriceAlert(BasePriceAlert priceAlert) {
		DecimalFormat priceFormat = new DecimalFormat("#0.#####");
		String strPrice = priceFormat.format(priceAlert.getPrice());		
		String PriceAlertMessage = priceAlert.getSymbol() + " just reached $" + strPrice;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(Clock.getInstance().now());	
		String keyValue = priceAlert.getSymbol() + "," + strPrice;
		priceAlert.setContent(PriceAlertMessage);
		return new ParseData(priceAlert.getUserId(), PriceAlertMessage, priceAlert.getId(),
				MSG_TYPE_PRICE, strDate, keyValue);
	}

	
	@SuppressWarnings("deprecation")
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if(event == timerEvent) {
			try
			{
				log.info("ParseDataQueue Size : " + ParseDataQueue.size());
//				ParseDataQueue.add(PackTradeAlert(new Execution("USDJPY", OrderSide.Buy, 10000.0,
//						123.4, "123-456-789", "111-111-111",
//						"1", "222-222-222",
//						"rickdev", "rickdev-FX", "abcdefg"),timeoutSecond));
//				
//				TradeAlert TA = new TradeAlert("david", "USDJPY", null ,(long)1000000, (double)120,"2015-01-22 20:45:11", "TEST");
//				SQLSave(TA);
				ThreadStatus TS ;
				for (ParseThread PT : ParseThreadList)
				{
					TS = PT.getThreadStatus() ;		
					if (TS.getThreadState() == ThreadState.IDLE.getState())
					{
						continue ;
					}
					else
					{
						long CurTime = System.currentTimeMillis();
						String Threadid = PT.getThreadId();
						if ((CurTime - TS.getTime()) > killTimeoutSecond)
						{
							log.warn(Threadid + " Timeout , ReOpen Thread.");
							ParseThreadList.remove(PT);
							try
							{
								PT.stop();
							}
							catch (Exception e)
							{
								log.warn("[processAsyncTimerEvent] Exception : " + e.getMessage()) ;
							}
							finally
							{
								PT = new ParseThread(Threadid, ParseDataQueue, timeoutSecond, maxRetrytimes, parseApplicationId,parseRestApiId);
								ParseThreadList.add(PT);
								PT.start();
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				log.warn("[processAsyncTimerEvent] Exception : " + e.getMessage()) ;
			}
		}
	}

	@Override
	public void init() throws Exception {
		String strThreadId = "";
		try
		{
			log.info("Initialising...");

			ParseDataQueue = new ConcurrentLinkedQueue<ParseData>() ;
			ParseThreadList = new ArrayList<ParseThread>() ;
			
			// subscribe to events
			eventProcessor.setHandler(this);
			eventProcessor.init();
			if(eventProcessor.getThread() != null)
				eventProcessor.getThread().setName("AlertManager");
			scheduleManager.scheduleRepeatTimerEvent(CheckThreadStatusInterval , eventProcessor, timerEvent);
			
			
			if (getCreateThreadCount() > 0)
			{
					for (int i = 0; i < getCreateThreadCount() ; i ++)
					{
						strThreadId = "Thread" + String.valueOf(i);
						ParseThread PT = new ParseThread(strThreadId,ParseDataQueue, timeoutSecond, maxRetrytimes, parseApplicationId,parseRestApiId);
						log.info("[" + strThreadId + "] New.") ;
						ParseThreadList.add(PT);
						PT.start();
					}
			}
			else
			{
				log.warn("createThreadCount Setting error : " + String.valueOf(getCreateThreadCount()));
			}
		}
		catch(Exception e)
		{
			log.warn("[" + strThreadId + "] Exception : " + e.getMessage()) ;
		}
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
		for (ParseThread PT : ParseThreadList)
		{
			PT.setstartThread(false) ;
		}
	}
	
	// getters and setters
	public boolean isTradeAlert() {
		return tradeAlert;
	}

	public void setTradeAlert(boolean tradeAlert) {
		this.tradeAlert = tradeAlert;
	}

	public boolean isPriceAlert() {
		return priceAlert;
	}

	public void setPriceAlert(boolean priceAlert) {
		this.priceAlert = priceAlert;
	}

	public int getTimeoutSecond() {
		return timeoutSecond;
	}

	public void setTimeoutSecond(int timeoutSecond) {
		this.timeoutSecond = timeoutSecond;
	}

	public int getMaxRetrytimes() {
		return maxRetrytimes;
	}

	public void setMaxRetrytimes(int maxRetrytimes) {
		this.maxRetrytimes = maxRetrytimes;
	}

	public long getKillTimeoutSecond() {
		return killTimeoutSecond;
	}

	public void setKillTimeoutSecond(long killTimeoutSecond) {
		this.killTimeoutSecond = killTimeoutSecond;
	}
	
	public String getParseApplicationId() {
		return parseApplicationId;
	}

	public void setParseApplicationId(String parseApplicationId) {
		this.parseApplicationId = parseApplicationId;
	}

	public String getParseRestApiId() {
		return parseRestApiId;
	}

	public void setParseRestApiId(String parseRestApiId) {
		this.parseRestApiId = parseRestApiId;
	}

	public int getCreateThreadCount() {
		return createThreadCount;
	}

	public void setCreateThreadCount(int createThreadCount) {
		this.createThreadCount = createThreadCount;
	}
}
