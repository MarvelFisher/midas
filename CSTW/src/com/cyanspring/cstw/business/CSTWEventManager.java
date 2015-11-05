package com.cyanspring.cstw.business;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public final class CSTWEventManager {

	public static void sendEvent(AsyncEvent event) {
		Business.getInstance().getEventManager().sendEvent(event);
	}

	public static void subscribe(Class<? extends AsyncEvent> event,
			IAsyncEventListener listener) {
		Business.getInstance().getEventManager().subscribe(event, listener);
	}

	public static void subscribe(Class<? extends AsyncEvent> event, String key,
			IAsyncEventListener listener) {
		Business.getInstance().getEventManager()
				.subscribe(event, key, listener);
	}

	public static void unsubscribe(Class<? extends AsyncEvent> event,
			IAsyncEventListener listener) {
		Business.getInstance().getEventManager().unsubscribe(event, listener);
	}

	public static void unsubscribe(Class<? extends AsyncEvent> event,
			String key, IAsyncEventListener listener) {
		Business.getInstance().getEventManager()
				.unsubscribe(event, key, listener);
	}

}
