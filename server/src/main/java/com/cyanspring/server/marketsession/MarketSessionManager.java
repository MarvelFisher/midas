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
package com.cyanspring.server.marketsession;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionState;
import com.cyanspring.common.marketsession.MarketSessionStateDay;
import com.cyanspring.common.marketsession.MarketSessionStateTime;
import com.cyanspring.common.marketsession.MarketSessionStateWeekDay;
import com.cyanspring.common.marketsession.MarketSessionTime;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.event.AsyncEventProcessor;

public class MarketSessionManager implements IPlugin, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionManager.class);
	
	@Autowired
	private ScheduleManager scheduleManager;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	protected long timerInterval = 5*1000;
	
	private MarketSessionType currentSessionType;
	
	private MarketSessionState sessionState;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(MarketSessionRequestEvent.class, null);
			subscribeToEvent(TradeDateRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public void processTradeDateRequestEvent(TradeDateRequestEvent event){
		try{
			TradeDateEvent tdEvent = new TradeDateEvent(null, null, sessionState.getTradeDate());
			eventManager.sendEvent(tdEvent);
		}catch(Exception e){
			log.error(e.getMessage(), e);
		} 
	}
	
	public void processMarketSessionRequestEvent(MarketSessionRequestEvent event){
		Date date = Clock.getInstance().now();
		try {
			MarketSessionEvent msEvent = sessionState.getCurrentMarketSessionEvent(date);
			msEvent.setKey(null);
			msEvent.setReceiver(null);
			eventManager.sendRemoteEvent(msEvent);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		Date date = Clock.getInstance().now();
		try {			
			if(sessionState.isStateChanged(date)){				
				MarketSessionEvent msEvent = sessionState.getCurrentMarketSessionEvent(date);
				msEvent.setKey(null);
				msEvent.setReceiver(null);
				log.info("Send MarketSessionEvent: " + msEvent);
				eventManager.sendGlobalEvent(msEvent);	
			}
			if(sessionState.isTradeDateChange()){
				TradeDateEvent tdEvent = new TradeDateEvent(null, null, sessionState.getTradeDate());
				log.info("Send TradeDateEvent: " + tdEvent.getTradeDate());
				eventManager.sendEvent(tdEvent);
				sessionState.setTradeDateUpdated();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		sessionState.init();
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("MarketSessionManager");
		
		if(!eventProcessor.isSync())
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);	
	}	

	@Override
	public void uninit() {
	}

	public void onEvent(AsyncEvent event) {
		if (event instanceof MarketSessionEvent) {
			currentSessionType = ((MarketSessionEvent)event).getSession();
			eventManager.sendEvent(event);
		} else {
			log.error("unhandled event: " + event);
		}
	}

	public MarketSessionType getCurrentSessionType() {
		return currentSessionType;
	}

	public void setSessionState(MarketSessionState sessionState) {
		this.sessionState = sessionState;
	}
}
