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
package com.cyanspring.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncExecuteEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.strategy.RemoveStrategyEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.server.event.ServerShutdownEvent;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyContainer;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.event.strategy.AddStrategyEvent;
import com.cyanspring.event.AsyncExecuteEventThread;

public class AsyncStrategyContainer implements IStrategyContainer, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(AsyncStrategyContainer.class);
	
	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private ScheduleManager scheduleManager;
	
	private boolean sync;

	private static AtomicInteger nextId = new AtomicInteger(0);
	protected Map<String, IStrategy> strategies = new HashMap<String, IStrategy>();
	protected String id;

	public String getId() {
		return id;
	}

	public AsyncStrategyContainer() {
		id = "SC-" + nextId.getAndIncrement();
	}

	@Override
	public void init() {
		eventManager.subscribe(AddStrategyEvent.class, id, this);
		eventManager.subscribe(RemoveStrategyEvent.class, id, this);
		eventManager.subscribe(ServerShutdownEvent.class, this);
		thread.setName("Thread-" + id);
		thread.start();
	}

	@Override
	public void uninit() {
		eventManager.unsubscribe(AddStrategyEvent.class, id, this);
		eventManager.unsubscribe(RemoveStrategyEvent.class, id, this);
		eventManager.unsubscribe(ServerShutdownEvent.class, this);
	}
	
	private void processAddStrategyEvent(AddStrategyEvent event) {
		IStrategy strategy = event.getStrategy();
		strategies.put(strategy.getId(), strategy);
		strategy.setContainer(this);
		try {
			strategy.init();
		} catch (StrategyException e) {
			log.error(e.getMessage(), e);
		}
		if(event.isAutoStart())
			eventManager.sendEvent(new StartStrategyEvent(strategy.getId(), null));
	}

	private void processRemoveStrategyEvent(RemoveStrategyEvent event) {
		String id = event.getStrategyId();
		strategies.remove(id);
	}

	private AsyncExecuteEventThread thread = new AsyncExecuteEventThread() {

		@Override
		public void onNormalEvent(AsyncEvent t) {
			if (t instanceof AddStrategyEvent) {
				processAddStrategyEvent((AddStrategyEvent) t);
			} else if (t instanceof RemoveStrategyEvent) {
				processRemoveStrategyEvent((RemoveStrategyEvent) t);
			} else if (t instanceof ServerShutdownEvent) {
				processServerShutdownEvent((ServerShutdownEvent) t);
			}
		}

	};
	
	private void processServerShutdownEvent(ServerShutdownEvent event) {
		for(Entry<String, IStrategy> entry: strategies.entrySet()) {
			entry.getValue().stop();
			entry.getValue().execute(ExecuteTiming.NOW);
		}
	}
	
	IAsyncEventInbox syncInbox = new IAsyncEventInbox() {

		@Override
		public void addEvent(AsyncEvent event, IAsyncEventListener listener) {
			thread.onEvent(new AsyncExecuteEvent(listener, event));
		}
	};
	
	@Override
	public IAsyncEventInbox getInbox() {
		if(sync)
			return syncInbox;
		else
			return thread;
	}

	@Override
	public boolean subscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener) {
		eventManager.subscribe(clazz, key, listener);
		return true;
	}

	@Override
	public boolean unsubscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener) {
		eventManager.unsubscribe(clazz, key, listener);
		return true;
	}

	@Override
	public void scheduleTimerEvent(long time, IAsyncExecuteEventListener listener,
			AsyncEvent event) {
		scheduleManager.scheduleTimerEvent(time, listener, event);
	}

	@Override
	public void scheduleTimerEvent(Date time, IAsyncExecuteEventListener listener,
			AsyncEvent event) {
		scheduleManager.scheduleTimerEvent(time, listener, event);
	}

	@Override
	public boolean scheduleRepeatTimerEvent(long time,
			IAsyncExecuteEventListener listener, AsyncEvent event) {
		return scheduleManager.scheduleRepeatTimerEvent(time, listener,
				event);
	}

	@Override
	public boolean cancelTimerEvent(AsyncEvent event) {
		return scheduleManager.cancelTimerEvent(event);
	}

	@Override
	public void sendEvent(AsyncEvent event) {
		eventManager.sendEvent(event);
	}
	
	@Override
	public void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendLocalOrRemoteEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendLocalOrRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(sync)
			thread.onEvent(event);
		else
			thread.addEvent(event);
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	@Override
	public int getStrategyCount() {
		return strategies.size();
	}

}
