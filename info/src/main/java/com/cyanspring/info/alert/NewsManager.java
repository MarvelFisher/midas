package com.cyanspring.info.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.event.AsyncEventProcessor;

public class NewsManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(NewsManager.class);
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		log.info("Initialising...");
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("NewsManager");
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		log.info("Uninitialising...");
		eventProcessor.uninit();
	}

}
