package com.cyanspring.cstw.cachingmanager;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/16
 *
 */
public abstract class BasicCachingManager {

	protected static final Logger log = LoggerFactory
			.getLogger(BasicCachingManager.class);

	protected String id;

	protected String txId;

	protected String server;

	protected Business business;

	private IAsyncEventListener eventListener;

	private List<Class<? extends AsyncEvent>> replyEventList;

	private Map<String, String> eventKeyMap;

	public BasicCachingManager() {
		business = Business.getInstance();
	}

	public void init() {
		replyEventList = getReplyEventList();
		eventKeyMap = getEventKeyMap();
		id = IdGenerator.getInstance().getNextID();
		txId = IdGenerator.getInstance().getNextID();
		server = business.getFirstServer();
		initAsyncEventListener();
	}

	protected Map<String, String> getEventKeyMap() {
		return null;
	}

	private void initAsyncEventListener() {
		eventListener = new IAsyncEventListener() {
			@Override
			public void onEvent(AsyncEvent event) {
				processAsyncEvent(event);
			}
		};

		if (replyEventList == null) {
			return;
		}

		for (Class<? extends AsyncEvent> event : replyEventList) {
			if (eventKeyMap != null && eventKeyMap.get(event.getName()) != null) {
				String key = eventKeyMap.get(event.getName());
				Business.getInstance().getEventManager()
						.subscribe(event, key, eventListener);
			} else {
				Business.getInstance().getEventManager()
						.subscribe(event, eventListener);
			}

		}
	}

	protected abstract List<Class<? extends AsyncEvent>> getReplyEventList();

	protected abstract void processAsyncEvent(AsyncEvent event);

}
