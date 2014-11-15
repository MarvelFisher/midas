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

import java.util.Collections;
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
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;

public class MarketDataManager implements IPlugin, IMarketDataListener, IMarketDataStateListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketDataManager.class);
	
	private Map<String, Quote> quotes = Collections.synchronizedMap(new HashMap<String, Quote>());
	@Autowired
	protected IRemoteEventManager eventManager;
	
	@Autowired
	protected ScheduleManager scheduleManager;
	
	private IQuoteChecker quoteChecker;
	private boolean invalidQuoteLogged;
	
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	protected Date lastQuoteSent = Clock.getInstance().now();
	protected long quoteThrottle = 100; // 0 = no throttle
	protected long timerInterval = 300;
	protected Map<String, QuoteEvent> quotesToBeSent = new HashMap<String, QuoteEvent>();
	private boolean preSubscribed = false;
	private List<String> preSubscriptionList;

	private IMarketDataAdaptor adaptor;
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
	
	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = event.getQuote();
		
		if(null != quoteChecker && !quoteChecker.check(quote)) {
			if(!invalidQuoteLogged) {
				log.warn("Invalid quote received: " + quote);
				invalidQuoteLogged = true;
			}
			return;
		} else {
			if(invalidQuoteLogged) {
				log.info("valid quote received: " + quote);
				invalidQuoteLogged = false;
			}
		}
		
		if(eventProcessor.isSync()) {
			sendQuoteEvent(event);
			return;
		}
			
		Quote prev = quotes.put(quote.getSymbol(), quote);
		// queue up quotes
		if (null != prev && quoteThrottle != 0 && TimeUtil.getTimePass(prev.getTimeSent()) < quoteThrottle) {
			quote.setTimeSent(prev.getTimeSent()); // important record the last time sent of this quote
			quotesToBeSent.put(quote.getSymbol(), event);
			return;
		}
		
		// send the quote now
		quote.setTimeSent(Clock.getInstance().now());
		quotesToBeSent.remove(quote.getSymbol()); //clear anything in queue because we are sending it now
		sendQuoteEvent(event);
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		//flush out all quotes throttled
		for(Entry<String, QuoteEvent> entry: quotesToBeSent.entrySet()){
			sendQuoteEvent(entry.getValue());
			//log.debug("Sending throttle quote: " + entry.getValue().getQuote());
		}
		quotesToBeSent.clear();
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

		if(!eventProcessor.isSync() && quoteThrottle != 0)
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
		
		adaptor.init();
		adaptor.subscribeMarketDataState(this);
		if(adaptor.getState())
			preSubscribe();
	}
	
	@Override
	public void uninit() {
		log.info("uninitialising");
		if(quoteThrottle != 0)
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
