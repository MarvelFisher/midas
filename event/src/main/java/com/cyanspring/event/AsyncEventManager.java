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
package com.cyanspring.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.IAsyncEventManager;

@SuppressWarnings("rawtypes")
public class AsyncEventManager implements IAsyncEventManager {
private static final Logger log = LoggerFactory.getLogger(AsyncEventManager.class);

	private Map<Class<? extends AsyncEvent>, Map<String, List<IAsyncEventListener>>> subscriptions = 
		new ConcurrentHashMap<Class<? extends AsyncEvent>, 
				Map<String, List<IAsyncEventListener>>>();
	
	// catering single thread mode listener gets removed in the loop
	// of callListener
	private boolean sync; 
	
	@Override
	public void sendEvent(AsyncEvent event) {
		Map<String, List<IAsyncEventListener>> listenerMap = subscriptions.get(event.getClass());
		if (null == listenerMap )
			return;
		
		List<IAsyncEventListener> eventListeners = listenerMap.get(event.getKey());
		synchronizedCallListeners(eventListeners, event);
		
		eventListeners = listenerMap.get("*");
		synchronizedCallListeners(eventListeners, event);
	}
	
	private void synchronizedCallListeners(List<IAsyncEventListener> eventListeners, AsyncEvent event) {
		if(null != eventListeners) {
			synchronized(eventListeners) {
				if(sync) { // make a copy of the listener list
					eventListeners = new LinkedList<IAsyncEventListener>(eventListeners);
				}
				
				for(IAsyncEventListener listener: eventListeners) {
					callListener(listener, event);
				}
			}
		}
	}
	
	class EventStat {
		long total = 0;
		HashMap<Class, Long> stat = new HashMap<Class, Long>();
		synchronized void addStat(AsyncEvent event) {
			Long count = stat.get(event.getClass());
			if(null == count)
				count = new Long(0);
			stat.put(event.getClass(), count+1);
			total++;
		}
		synchronized void outPutStat() {
			if(total % 5000 == 10) {
				log.debug("Event Stats: \n"+stat);
			}
		}
	}
	
	private EventStat eventStat = new EventStat();
	private void callListener(IAsyncEventListener listener, AsyncEvent event) {
		try {
			eventStat.addStat(event);
			eventStat.outPutStat();
			if(listener instanceof IAsyncExecuteEventListener) {
				IAsyncExecuteEventListener al = (IAsyncExecuteEventListener)listener;
				al.getInbox().addEvent(event, listener);
			} else {
				listener.onEvent(event);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean subscribe(Class<? extends AsyncEvent> clazz, IAsyncEventListener listener) {
		return subscribe(clazz, "*", listener);
	}
	
	@Override
	public boolean subscribe(Class<? extends AsyncEvent> clazz, String key, IAsyncEventListener listener) {
		if (null == listener) {
			return false;
		}
		
		Map<String, List<IAsyncEventListener>> listenerMap = subscriptions.get(clazz);
		if (listenerMap == null) {
			listenerMap = Collections.synchronizedMap(new HashMap<String, List<IAsyncEventListener>>());
			subscriptions.put(clazz, listenerMap);
		}
		
		if (key == null)
			key = "*";
		
		List<IAsyncEventListener> eventListeners = listenerMap.get(key);
		if (null == eventListeners)	{
			eventListeners = Collections.synchronizedList(new LinkedList<IAsyncEventListener>());
			listenerMap.put(key, eventListeners);
		}
		
		if (!eventListeners.contains(listener))	{
			synchronized(eventListeners) {
				eventListeners.add(listener);
			}
		} else
			return false;
		
		return true;
	}
	
	@Override
	public void unsubscribe(Class<? extends AsyncEvent> clazz, IAsyncEventListener listener)
	{
		unsubscribe(clazz, "*", listener);
	}

	@Override
	public void unsubscribe(Class<? extends AsyncEvent> clazz, String key, IAsyncEventListener listener)
	{
		
		Map<String, List<IAsyncEventListener>> listenerMap = subscriptions.get(clazz);
		if (null == listenerMap ) {
			return;
		}
		
		List<IAsyncEventListener> eventListeners = listenerMap.get(key);
		if (null == eventListeners)	{
			return;
		}
		
		if (!eventListeners.contains(listener))	{
			return;
		}
		
		synchronized(eventListeners) {
			eventListeners.remove(listener);
		}
	}
	
	@Override
	public void clearAllSubscriptions() {
		subscriptions.clear();
	}

	@Override
	public boolean isSync() {
		return sync;
	}

	@Override
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	
	
}
