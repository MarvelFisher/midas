/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server.marketdata;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketdata.LastTradeDateQuotesEvent;
import com.cyanspring.common.event.marketdata.LastTradeDateQuotesRequestEvent;
import com.cyanspring.common.event.marketdata.PresubscribeEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketdata.TradeDateUpdateEvent;
import com.cyanspring.common.event.marketdata.TradeEvent;
import com.cyanspring.common.event.marketdata.TradeSubEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MarketDataManager implements IPlugin, IMarketDataListener, IMarketDataStateListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketDataManager.class);
	
	private HashMap<String, Quote> quotes = new HashMap<String, Quote>();
	private Map<String, Quote> lastTradeDateQuotes = new HashMap<String, Quote>();
	
	@Autowired
	protected IRemoteEventManager eventManager;
	
	@Autowired
	protected ScheduleManager scheduleManager;
	
	private IQuoteChecker quoteChecker = new PriceQuoteChecker();
	
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	protected Date lastQuoteSent = Clock.getInstance().now();
	protected long quoteThrottle = 100; // 0 = no throttle
	protected long timerInterval = 300;
	protected Map<String, QuoteEvent> quotesToBeSent = new HashMap<String, QuoteEvent>();
	private boolean preSubscribed = false;
	private List<String> preSubscriptionList;
	private IMarketDataAdaptor adaptor;
	private String tickDir = "ticks";
	private String lastQuoteFile = "last.xml";
	private String lastTradeDateQuoteFile = "last_tdq.xml";
	private long lastQuoteSaveInterval = 20000;
	private Date lastQuoteSaveTime = Clock.getInstance().now();
	private XStream xstream = new XStream(new DomDriver());
	private boolean staleQuotesSent;
	private Date initTime = Clock.getInstance().now();
	private String tradeDate;
	boolean isUninit = false;
	private Map<MarketSessionType, Long> sessionMonitor;
	private Date chkDate;
	private long chkTime;	

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteSubEvent.class, null);
			subscribeToEvent(TradeSubEvent.class, null);
			subscribeToEvent(PresubscribeEvent.class, null);
			subscribeToEvent(LastTradeDateQuotesRequestEvent.class, null);
			subscribeToEvent(TradeDateEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
		
	public void processMarketSessionEvent(MarketSessionEvent event){
		chkTime = sessionMonitor.get(event.getSession());
		log.info("Get MarketSessionEvent: " + event.getSession() + ", map size: " + sessionMonitor.size() + ", checkTime: " + chkTime);
	}
	
	public void processLastTradeDateQuotesRequestEvent(LastTradeDateQuotesRequestEvent event) {		
		try {
			if(tradeDate == null){
				TradeDateRequestEvent tdrEvent = new TradeDateRequestEvent(null, null);
				eventManager.sendEvent(tdrEvent);
			}else{
				List<Quote> lst = new ArrayList<Quote>(lastTradeDateQuotes.values());
				log.info("LastTradeDateQuotesRequestEvent sending lastTradeDateQuotes: " + lst );
				LastTradeDateQuotesEvent lastTDQEvent = new LastTradeDateQuotesEvent(null, null, tradeDate, lst);
				eventManager.sendRemoteEvent(lastTDQEvent);				
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processTradeDateEvent(TradeDateEvent event){
		String newTradeDate = event.getTradeDate();
		if(!newTradeDate.equals(tradeDate) || tradeDate == null){
			tradeDate = newTradeDate;
			try {
				eventManager.sendGlobalEvent(new TradeDateUpdateEvent(null,null,tradeDate));
				saveLastTradeDateQuotes();
				List<Quote> lst = new ArrayList<Quote>(lastTradeDateQuotes.values());
				log.info("LastTradeDatesQuotes: " + lst + ", tradeDate:" + tradeDate);
				eventManager.sendRemoteEvent(new LastTradeDateQuotesEvent(null, null, tradeDate, lst));				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void processPresubscribeEvent(PresubscribeEvent event) {
		preSubscribe();
	}
	
	public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
		log.debug("QuoteSubEvent: " + event.getSymbol() + ", " + event.getReceiver());
		Quote quote = quotes.get(event.getSymbol());
		if (quote == null) {
			adaptor.subscribeMarketData(event.getSymbol(), MarketDataManager.this);
		} else {
			if(quote.isStale())
				adaptor.subscribeMarketData(event.getSymbol(), MarketDataManager.this);
			eventManager.sendLocalOrRemoteEvent(new QuoteEvent(event.getKey(), event.getSender(), quote));
		}
	}
	
	public void processTradeSubEvent(TradeSubEvent event) throws MarketDataException {
		TradeSubEvent tradeSubEvent = (TradeSubEvent)event;
		Quote quote = quotes.get(tradeSubEvent.getSymbol());
		if (quote == null) {
			adaptor.subscribeMarketData(tradeSubEvent.getSymbol(), MarketDataManager.this);
		}
	}

	private void sendQuoteEvent(QuoteEvent event) {
		try {
			eventManager.sendGlobalEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void clearAndSendQuoteEvent(QuoteEvent event) {
		event.getQuote().setTimeSent(Clock.getInstance().now());
		quotesToBeSent.remove(event.getQuote().getSymbol()); //clear anything in queue because we are sending it now
		sendQuoteEvent(event);
	}

	private void logStaleInfo(Quote prev, Quote quote, boolean stale) {
		log.info("Quote stale: " + quote.getSymbol() + ", " + stale + ", Prev: " + prev + ", New: " + quote);
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = event.getQuote();
		
		Quote prev = quotes.get(quote.getSymbol());
		
		if(null == prev) {
			logStaleInfo(prev, quote, quote.isStale());
			quotes.put(quote.getSymbol(), quote);
			clearAndSendQuoteEvent(event);
			return;
		} else if (null != quoteChecker && !quoteChecker.check(quote)) {
			boolean prevStale = prev.isStale();
			prev.setStale(true); //just set the existing stale
			if(!prevStale) {
				logStaleInfo(prev, quote, true);
				clearAndSendQuoteEvent(new QuoteEvent(event.getKey(), null, prev));
			}
			return;
		} else {
			quotes.put(quote.getSymbol(), quote);
			if(prev.isStale() != quote.isStale()) {
				logStaleInfo(prev, quote, quote.isStale());
			}
		}
		
		if(eventProcessor.isSync()) {
			sendQuoteEvent(event);
			return;
		}
			
		// queue up quotes
		if (null != prev && quoteThrottle != 0 && TimeUtil.getTimePass(prev.getTimeSent()) < quoteThrottle) {
			quote.setTimeSent(prev.getTimeSent()); // important record the last time sent of this quote
			quotesToBeSent.put(quote.getSymbol(), event);
			return;
		}
		
		// send the quote now
		clearAndSendQuoteEvent(event);
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		//flush out all quotes throttled
		for(Entry<String, QuoteEvent> entry: quotesToBeSent.entrySet()){
			sendQuoteEvent(entry.getValue());
			//log.debug("Sending throttle quote: " + entry.getValue().getQuote());
		}
		quotesToBeSent.clear();
		saveLastQuotes();
		broadCastStaleQuotes();
	}

	public void processTradeEvent(TradeEvent event) {
		eventManager.sendEvent(event);
	}
	
	public MarketDataManager(IMarketDataAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	
	@Override
	public void init() throws Exception {
		log.info("initialising");
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("MarketDataManager");				

		// create tick directory
		File file = new File(tickDir);
		if(!file.isDirectory()) {
			log.info("Creating tick directory: " + tickDir);
			if(!file.mkdir()) {
				throw new TickDataException("Unable to create tick data directory: " + tickDir);
			}
		} else {
			log.info("Existing tick directory: " + tickDir);
		}

		quotes = loadQuotes(tickDir + "/" + lastQuoteFile);
log.info("Quotes Loaded Counts [" + quotes.size() + "] " );
for(Entry<String, Quote> entry : quotes.entrySet())
{
	log.info("Quotes Loaded Results [" + entry.getKey() + "] " + entry.getValue().toString());
}
		lastTradeDateQuotes = loadQuotes(tickDir + "/" + lastTradeDateQuoteFile);
		if(lastTradeDateQuotes == null || lastTradeDateQuotes.size() <= 0){
			log.warn("No lastTradeDateQuotes values while initialing.");			
			lastTradeDateQuotes = (Map<String, Quote>) quotes.clone();
		}

		log.info("LastTradeDateQuotes Loaded Counts [" + lastTradeDateQuotes.size() + "] " );
for(Entry<String, Quote> entry : lastTradeDateQuotes.entrySet())
{
	log.info("LastTradeDateQuotes Loaded Results [" + entry.getKey() + "] " + entry.getValue().toString());
}
		
		chkDate = Clock.getInstance().now();
		
		adaptor.subscribeMarketDataState(this);
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					adaptor.init();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
		});
		thread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			  public void run() {
				  uninit();
			  }
			});
		
		
		if(adaptor.getState())
			eventProcessor.onEvent(new PresubscribeEvent(null));
		
		if(!eventProcessor.isSync())
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);			
	}
	
	private void saveLastQuotes() {
		if(TimeUtil.getTimePass(lastQuoteSaveTime) < lastQuoteSaveInterval) 
			return;
		
		if(quotes.size() <= 0)
			return;
		
		lastQuoteSaveTime = Clock.getInstance().now();
		String fileName = tickDir + "/" + lastQuoteFile;
		saveQuotesToFile(fileName);
	}
	
	private void saveLastTradeDateQuotes(){
		if (lastTradeDateQuotes.size() <= 0 && quotes.size() <= 0)
			return;		
		lastTradeDateQuotes = quotes;			
		String fileName = tickDir + "/" + lastTradeDateQuoteFile;
		saveQuotesToFile(fileName); 		
	}

	private void saveQuotesToFile(String fileName) {
		File file = new File(fileName);
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(quotes, os);
			os.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void broadCastStaleQuotes() {
		if(staleQuotesSent)
			return;
		
		if(TimeUtil.getTimePass(initTime) < lastQuoteSaveInterval) 
			return;
		
		staleQuotesSent = true;
		for(Quote quote: quotes.values()) {
			if(quote.isStale())
				this.clearAndSendQuoteEvent(new QuoteEvent(quote.getSymbol(), null, quote));
		}
	}		
	
	private HashMap<String, Quote> loadQuotes(String fileName){
		File file = new File(fileName);
		HashMap<String, Quote> quotes = new HashMap<>();
		if(file.exists() && quotes.size() <= 0){
			try{
				ClassLoader save = xstream.getClassLoader();
				ClassLoader cl = HashMap.class.getClassLoader();
				if (cl != null) 
					xstream.setClassLoader(cl);
				quotes = (HashMap<String, Quote>)xstream.fromXML(file);
				if(!(quotes instanceof HashMap)) 
					throw new Exception("Can't xstream load last quote: " + fileName);
				xstream.setClassLoader(save);
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
			for(Quote quote: quotes.values()) {
				quote.setStale(true);
			}
			log.info("Quotes loaded: " + fileName);
		}
		return quotes;
	}
	
	public void reset() {
		quotes.clear();
	}
	
	@Override
	public void uninit() {
		if (isUninit)
			return;
		
		isUninit = true;

		log.info("uninitialising");
		if(!eventProcessor.isSync())
			scheduleManager.cancelTimerEvent(timerEvent);

		adaptor.uninit();

		eventProcessor.uninit();
	}
	
	@Override
	public void onQuote(Quote quote) {
		if(TimeUtil.getTimePass(chkDate) > chkTime && chkTime != 0){
			log.error("Quotes receive time large than excepted.");
		}
		chkDate = Clock.getInstance().now();
		QuoteEvent event = new QuoteEvent(quote.getSymbol(), null, quote);
		eventProcessor.onEvent(event);
	}
	
	@Override
	public void onTrade(Trade trade) {
		TradeEvent event = new TradeEvent(trade.getSymbol(), null, trade);
		eventProcessor.onEvent(event);
	}
	
	@Override
	public void onState(boolean on) {
		if (!on) {
			log.warn("MarketData feed is down");
		} else {
			log.info("MarketData feed is up");
			eventProcessor.onEvent(new PresubscribeEvent(null));
		}
		eventManager.sendEvent(new MarketDataReadyEvent(null, on));
	
	}
	
	private void preSubscribe() {
		if(null == preSubscriptionList || preSubscribed)
			return;
		
		preSubscribed = true;
		log.debug("Market data presubscribe: " + preSubscriptionList);
		try {
			for (String symbol : preSubscriptionList)
				adaptor.subscribeMarketData(symbol, this);
		} catch (MarketDataException e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean isSync() {
		return eventProcessor.isSync();
	}

	public void setSync(boolean sync) {
		eventProcessor.setSync(sync);
	}

	public long getQuoteThrottle() {
		return quoteThrottle;
	}

	public void setQuoteThrottle(long quoteThrottle) {
		this.quoteThrottle = quoteThrottle;
	}

	public List<String> getPreSubscriptionList() {
		return preSubscriptionList;
	}

	public void setPreSubscriptionList(List<String> preSubscriptionList) {
		this.preSubscriptionList = preSubscriptionList;
	}

	public IQuoteChecker getQuoteChecker() {
		return quoteChecker;
	}

	public void setQuoteChecker(IQuoteChecker quoteChecker) {
		this.quoteChecker = quoteChecker;
	}	
	
	public void setSessionMonitor(Map<MarketSessionType, Long> sessionMonitor) {
		this.sessionMonitor = sessionMonitor;
	}
	
	public long getTimerInterval() {
		return timerInterval;
	}

	public void setTimerInterval(long timerInterval) {
		this.timerInterval = timerInterval;
	}
	
}
