package com.cyanspring.analytical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.event.AsyncEventProcessor;

public class AnalyticalManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AnalyticalManager.class);

	private ScheduleManager scheduleManager = new ScheduleManager();
	private long timerInterval = 200;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private List<String> symbolList = new ArrayList<String>();
	private Map<String, Quote> quotes = new ConcurrentHashMap<String, Quote>();
	private List<IQuoteAnalyzer> analyzers;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	@Autowired
	private IRemoteEventManager eventManager;
	

	@Override
	public void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("SignalAnalyzer");
		
		scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
		for(String symbol: symbolList) {
			eventProcessor.subscribeToEventNow(QuoteEvent.class, symbol);
		}
		
		for(IQuoteAnalyzer analyzer: analyzers)
			analyzer.init();
	}

	@Override
	public void uninit() {
		for(IQuoteAnalyzer analyzer: analyzers)
			analyzer.uninit();
		scheduleManager.uninit();
		eventProcessor.uninit();
	}

	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = (Quote) event.getQuote().clone();
		quote.setTimeStamp(Clock.getInstance().now());
		quotes.put(quote.getSymbol(), quote);
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		for(IQuoteAnalyzer analyzer: analyzers) {
			for(Quote quote: quotes.values())
				analyzer.analyze(quote);
		}
	}

	public void setSymbolList(List<String> symbolList) {
		this.symbolList = symbolList;
	}

	public void setAnalyzers(List<IQuoteAnalyzer> analyzers) {
		this.analyzers = analyzers;
	}
	
	
}
