package com.cyanspring.info.alert;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.alert.*;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.alert.AlertType;
import com.cyanspring.common.event.alert.SetPriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;

public class AlertManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);

	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManager;
	
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
	
	private Map<String, ArrayList<PriceAlert>> symbolPriceAlerts = new HashMap<String, ArrayList<PriceAlert>>();
	private Map<String, ArrayList<TradeAlert>> symbolTradeAlerts = new HashMap<String, ArrayList<TradeAlert>>();
	private int maxNoOfAlerts = 20;
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	
	private boolean receiveQueryTradeAlert()
	{
		return true ;
	}
	
	private boolean receiveAddPriceAlert(PriceAlert priceAlert) {
		
		ArrayList<PriceAlert> list;
		int search;
		
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			list = new ArrayList<PriceAlert>();
			//do new PriceAlert
			list.add(priceAlert);
			symbolPriceAlerts.put(priceAlert.getSymbol(), list);
		}
		else
		{
			search = Collections.binarySearch(list, priceAlert);
			if (search > 0)
			{
				log.warn("[recevieAddPriceAlert] : id already exists. -> do modify PriceAlert.");
				domodifyPriceAlert(priceAlert) ;
			}
			else
			{
				
			}
		}
//		list.add(priceAlert);
		return true;
	}
	
	private boolean receiveModifyPriceAlert(PriceAlert priceAlert) {			
			return true;
		}
	
	private boolean receiveCancelPriceAlert(PriceAlert priceAlert) {
		return true;
	}
	
	private void domodifyPriceAlert(PriceAlert priceAlert)
	{
		
	}
	private void removePriceAlert(PriceAlert priceAlert) {
//		List<PriceAlert> list;
//		list = symbolPriceAlerts.get(priceAlert.getSymbol());
//		if(null != list)
//			list.remove(priceAlert);
//		
//		list = accountPriceAlerts.get(priceAlert.getAccount());
//		if(null != list)
//			list.remove(priceAlert);
	}
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateChildOrderEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(SetPriceAlertRequestEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		if(null == event.getExecution())
			return;
		
//		log.debug("[processUpdateChildOrderEvent] "+event.getExecution().toString());
		
		if(null != ParseDataQueue)
			ParseDataQueue.add(PackTradeAlert(event.getExecution(),timeoutSecond));
		else			
			log.error("ParseDataQueue not ready!!");
		//to TradeAlert
		//save to SQL
		//save to Array
		
//		if(null != tradeAlertSender)
//			tradeAlertSender.sendTradeAlert(event.getExecution(), timeoutSecond);
	}
	
	public void processQuoteEvent(QuoteEvent event) {
//		Quote quote = event.getQuote();
//		quotes.put(quote.getSymbol(), quote);
//		List<PriceAlert> list = symbolPriceAlerts.get(quote.getSymbol());
//		if(null == list)
//			return;
//		for(PriceAlert alert: list) {
//			firePriceAlert(alert, quote);
//		}
	}
	
	public void processSetPriceAlertRequestEvent(SetPriceAlertRequestEvent event) {
		AlertType type = event.getType();
		if (type == AlertType.PRICE_SET_NEW)
		{
			receiveAddPriceAlert(event.getPriceAlert());
		}
		else if (type == AlertType.PRICE_SET_MODIFY)
		{
			receiveModifyPriceAlert(event.getPriceAlert());
		}
		else if (type == AlertType.PRICE_SET_CANCEL)
		{
			receiveCancelPriceAlert(event.getPriceAlert());
		}
	}	
	
	private double getAlertPrice(Quote quote) {
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	private boolean firePriceAlert(PriceAlert alert, Quote quote) {
//		double currentPrice = getAlertPrice(quote);
//		if(PriceUtils.isZero(alert.getStartPrice())) {
//			alert.setStartPrice(currentPrice);
//		} else if (PriceUtils.Equal(alert.getPrice(), currentPrice) || // condition 1
//			PriceUtils.EqualGreaterThan(alert.getPrice(), alert.getStartPrice()) &&
//			PriceUtils.EqualGreaterThan(currentPrice, alert.getPrice()) ||  // condition 2
//			PriceUtils.EqualLessThan(alert.getPrice(), alert.getStartPrice()) &&
//			PriceUtils.EqualLessThan(currentPrice, alert.getPrice())) { // condition 3
//			if(null != priceAlertSender)
//				priceAlertSender.sendPriceAlert(alert, timeoutSecond);
//			return true;
//		}
		return false;
	}
	
	public ParseData PackTradeAlert(Execution execution, int timeoutSecond) {
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

	public ParseData PackPriceAlert(PriceAlert priceAlert, int timeoutSecond) {
		return null;	
	}

	
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
							PT.destroy();
							PT = new ParseThread(Threadid, ParseDataQueue, timeoutSecond, maxRetrytimes, parseApplicationId,parseRestApiId);
							ParseThreadList.add(PT);
							PT.start();
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
