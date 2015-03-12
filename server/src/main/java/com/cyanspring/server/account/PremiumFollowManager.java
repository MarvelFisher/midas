package com.cyanspring.server.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.PremiumFollowRequestEvent;
import com.cyanspring.event.AsyncEventProcessor;

public class PremiumFollowManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(PremiumFollowManager.class);

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private IRemoteEventManager globalEventManager;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PremiumFollowRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	
	private AsyncEventProcessor globalEventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PremiumFollowRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		
	}

}
