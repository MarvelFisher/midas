package com.cyanspring.info.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.info.PriceHighLowEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.AsyncEventProcessor;

public class MarketInfoManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(MarketInfoManager.class);

	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	private IRemoteEventManager eventManager;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}

	};
	
	@Override
	public void init() throws Exception {
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("MarketInfoManager");
		
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		log.info("Quote: " + event.getQuote());
	}
	
	public void sendPriceHighLowEvent(PriceHighLowEvent event) {
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
