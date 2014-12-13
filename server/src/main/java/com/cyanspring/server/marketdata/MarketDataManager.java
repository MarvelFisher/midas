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
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketdata.TradeEvent;
import com.cyanspring.common.event.marketdata.TradeSubEvent;
import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MarketDataManager implements IPlugin, IMarketDataListener, IMarketDataStateListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketDataManager.class);
	
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
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
	private long lastQuoteSaveInterval = 20000;
	private Date lastQuoteSaveTime = new Date();
	private XStream xstream = new XStream(new DomDriver());
	private boolean staleQuotesSent;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteSubEvent.class, null);
			subscribeToEvent(TradeSubEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};	
	
	public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
		log.debug("QuoteSubEvent: " + event.getSymbol() + ", " + event.getReceiver());
		Quote quote = quotes.get(event.getSymbol());
		if (quote == null) {
			adaptor.subscribeMarketData(event.getSymbol(), MarketDataManager.this);
		} else {
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

		if(!eventProcessor.isSync())
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
		
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
		
		loadLastQuotes();

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
		
		if(adaptor.getState())
			preSubscribe();
	}
	
	private void saveLastQuotes() {
		if(TimeUtil.getTimePass(lastQuoteSaveTime) < lastQuoteSaveInterval) 
			return;
		
		if(quotes.size() <= 0)
			return;
		
		lastQuoteSaveTime = Clock.getInstance().now();
		String fileName = tickDir + "/" + lastQuoteFile;
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
		
		if(TimeUtil.getTimePass(lastQuoteSaveTime) < 2 * lastQuoteSaveInterval) 
			return;
		
		staleQuotesSent = true;
		for(Quote quote: quotes.values()) {
			if(quote.isStale())
				this.clearAndSendQuoteEvent(new QuoteEvent(quote.getSymbol(), null, quote));
		}
	}
		
	@SuppressWarnings("unchecked")
	private void loadLastQuotes() {
		String fileName = tickDir + "/" + lastQuoteFile;
		File file = new File(fileName);
		if(file.exists() && quotes.size() <= 0) {
			try {
				ClassLoader save = xstream.getClassLoader();
				ClassLoader cl = HashMap.class.getClassLoader();
				if (cl != null)
					xstream.setClassLoader(cl);
	
				quotes = (HashMap<String, Quote>)xstream.fromXML(file);
				if(!(quotes instanceof HashMap))
					throw new Exception("Can't xstream load last quote: " + fileName);
				xstream.setClassLoader(save);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			for(Quote quote: quotes.values()) {
				quote.setStale(true);
			}
			log.info("Last quotes loaded: " + fileName);
		}
	}

	@Override
	public void uninit() {
		log.info("uninitialising");
		if(!eventProcessor.isSync())
			scheduleManager.cancelTimerEvent(timerEvent);

		eventProcessor.uninit();
	}
	
	
	@Override
	public void onQuote(Quote quote) {
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
			preSubscribe();
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
	
}
