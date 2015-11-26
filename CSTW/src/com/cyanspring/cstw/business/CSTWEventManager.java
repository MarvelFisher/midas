package com.cyanspring.cstw.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.05
 *
 */
public final class CSTWEventManager {

	private static final Logger log = LoggerFactory
			.getLogger(CSTWEventManager.class);

	public static void sendEvent(AsyncEvent event) {
		if (event instanceof RemoteAsyncEvent) {
			RemoteAsyncEvent remoteEvent = (RemoteAsyncEvent) event;
			try {
				Business.getInstance().getEventManager()
						.sendRemoteEvent(remoteEvent);
			} catch (Exception e) {
				log.error("Remote Event Send Error:" + e);
			}
		} else {
			Business.getInstance().getEventManager().sendEvent(event);
		}
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
