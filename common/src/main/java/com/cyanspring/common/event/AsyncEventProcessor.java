package com.cyanspring.common.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AsyncEventProcessor implements IAsyncEventListener {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory
			.getLogger(AsyncEventProcessor.class);
	
	private Object handler;
	private Exception lastException;
	private List<AsyncEventSub> subs = new ArrayList<AsyncEventSub>();
	protected boolean sync;
	private Map<Class<? extends AsyncEvent>, Method> methodMap = new HashMap<Class<? extends AsyncEvent>, Method>();
	private AsyncPriorityEventThread thread;
	
	class AsyncEventSub {
		Class<? extends AsyncEvent> eventClass;
		String key;
		public AsyncEventSub(Class<? extends AsyncEvent> eventClass, String key) {
			super();
			this.eventClass = eventClass;
			this.key = key;
		}
		public Class<?  extends AsyncEvent> getEventClass() {
			return eventClass;
		}
		public String getKey() {
			return key;
		}
		
	}
	
	public abstract void subscribeToEvents();
	public abstract IAsyncEventManager getEventManager();
	
	private String getMethodName(Class<? extends AsyncEvent> clazz) {
		return "process" + clazz.getSimpleName();
	}
	public void subscribeToEvent(Class<? extends AsyncEvent> clazz, String key) {
		subs.add(new AsyncEventSub(clazz, key));
	}
	
	public void subscribeToEventNow(Class<? extends AsyncEvent> clazz, String key) {
		getEventManager().subscribe(clazz, key, this);
	}
	
	public void unsubscribeToEvent(Class<? extends AsyncEvent> clazz, String key) {
		getEventManager().unsubscribe(clazz, key, this);
	}
	
	public void init() throws Exception {
		if(!sync && thread == null)
			createThread();
		
		subscribeToEvents();
		doSubscription();
	}
	
	protected void doSubscription() throws Exception {
		if(null == subs || subs.size() == 0) {
			log.warn("Event subscription list is null or empty, nothing to work on");
			return;
		}
		IAsyncEventManager eventManager = getEventManager();
		for(AsyncEventSub sub: subs) {
			String methodName = getMethodName(sub.getEventClass());
			try {
				Method method = getHandler().getClass().getMethod(methodName, sub.getEventClass());
				methodMap.put(sub.getEventClass(), method);
			} catch (Exception e) {
				if(e instanceof NoSuchMethodException)
					throw new Exception("Event type " + sub.getEventClass().getName() + 
						" is not handled. Do you have a public method with name " + methodName + "?");
				else
					log.error(e.getMessage(), e);
				
				this.lastException = e;
				continue;
			}
			eventManager.subscribe(sub.getEventClass(), sub.getKey(), this);
		}
		
	}
	
	public void uninit() {
		IAsyncEventManager eventManager = getEventManager();
		for(AsyncEventSub sub: subs) {
			eventManager.unsubscribe(sub.getEventClass(), sub.getKey(), this);
		}
		lastException = null;
		subs.clear();
		methodMap.clear();
		if(null != thread) {
			killThread();
		}
	}
	
	protected void onAsyncEvent(AsyncEvent event) {
		try {
			Method method = methodMap.get(event.getClass());
			if(method == null) { 
				// if handling method not found try to get outer class handler
				String methodName = getMethodName(event.getClass());
				try {
					method = getHandler().getClass().getMethod(methodName, event.getClass());
					methodMap.put(event.getClass(), method);
					log.info(methodName + " added in method map");
				} catch (NoSuchMethodException e) {
					log.error("Event type " + event.getClass() + " handling method is not found " + getHandler() );
					return;
				}
			}
			method.invoke(getHandler(), event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			lastException = e;
		}
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(sync) {
			onAsyncEvent(event);
		} else {
			thread.addEvent(event);
		}
	}
	
	public Object getHandler() {
		return handler == null? this:handler;
	}
	
	public void setHandler(Object handler) {
		this.handler = handler;
	}
	public Exception getLastException() {
		return lastException;
	}
	
	public boolean isSync() {
		return sync;
	}
	
	protected void createThread() {
		thread = new AsyncPriorityEventThread() {

			@Override
			public void onEvent(AsyncEvent event) {
				AsyncEventProcessor.this.onAsyncEvent(event);
			}
			
		};
		thread.start();
	}
	
	private void killThread() {
		thread.exit();
		thread = null;
	}
	
	public void setSync(boolean sync) {
		if(this.sync && !sync) {// set to async
			createThread();
		}
		if(!this.sync && sync && null != thread) {
			killThread();
		}
		this.sync = sync;
	}
	
	public AsyncPriorityEventThread getThread() {
		return thread;
	}
	
	
}
