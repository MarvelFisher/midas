package com.cyanspring.common.event;

public class AsyncEventHandler {
	private Class<? extends AsyncEvent> eventClass;
	private String key;
	private IAsyncEventListener listener;
	public AsyncEventHandler(Class<? extends AsyncEvent> eventClass,
			String key, IAsyncEventListener listener) {
		super();
		this.eventClass = eventClass;
		this.key = key;
		this.listener = listener;
	}
	public Class<? extends AsyncEvent> getEventClass() {
		return eventClass;
	}
	public String getKey() {
		return key;
	}
	public IAsyncEventListener getListener() {
		return listener;
	}
	
}
