package com.cyanspring.cstw.service.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.IUiListener;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/22
 *
 */
public abstract class BasicServiceImpl implements IBasicService {

	protected static final Logger log = LoggerFactory
			.getLogger(BasicServiceImpl.class);

	protected String id = IdGenerator.getInstance().getNextID();

	protected String txId = IdGenerator.getInstance().getNextID();

	protected Business business = Business.getInstance();

	protected String server = Business.getBusinessService().getFirstServer();

	protected IUiListener uiListener;

	private List<Class<? extends AsyncEvent>> replyEventList;

	private IAsyncEventListener eventListener;

	// Just for logger info
	private String nameForLogger = this.getClass().getSimpleName() + " ";

	public BasicServiceImpl() {
		replyEventList = getReplyEventList();
		initAsyncEventListener();
	}

	private void initAsyncEventListener() {
		eventListener = new IAsyncEventListener() {
			@Override
			public void onEvent(AsyncEvent event) {
				RefreshEventType type = handleEvent(event);
				uiListener.refreshByType(type);
			}
		};

		if (replyEventList == null) {
			return;
		}
		for (Class<? extends AsyncEvent> event : replyEventList) {
			Map<Class<? extends AsyncEvent>, String> map = getReplyEventKeyMap();
			if (map.get(event) != null) {
				Business.getInstance().getEventManager()
						.subscribe(event, map.get(event), eventListener);
			} else {
				Business.getInstance().getEventManager()
						.subscribe(event, eventListener);
			}

		}

	}

	public void registerEvent(Class<? extends AsyncEvent> event) {
		log.info(nameForLogger
				+ "single register reply event to default server:" + event);
		Business.getInstance().getEventManager()
				.subscribe(event, eventListener);
	}

	protected abstract List<Class<? extends AsyncEvent>> getReplyEventList();

	protected Map<Class<? extends AsyncEvent>, String> getReplyEventKeyMap() {
		return new HashMap<Class<? extends AsyncEvent>, String>();
	}

	protected abstract RefreshEventType handleEvent(AsyncEvent event);

	protected void sendEvent(AsyncEvent event) {
		if (event instanceof RemoteAsyncEvent) {
			RemoteAsyncEvent remoteEvent = (RemoteAsyncEvent) event;
			try {
				log.info(nameForLogger + " send event to default server:"
						+ event.getClass().getSimpleName());
				business.getEventManager().sendRemoteEvent(remoteEvent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			business.getEventManager().sendEvent(event);
		}

	}

	@Override
	public void setRefreshListener(IUiListener listener) {
		uiListener = listener;
	}

	@Override
	public void clear() {
		uiListener = null;
		if (replyEventList == null) {
			return;
		}
		for (Class<? extends AsyncEvent> event : replyEventList) {
			Business.getInstance().getEventManager()
					.unsubscribe(event, eventListener);
			log.info(nameForLogger + "unsubscribe event "
					+ "  to default server:" + event);
		}
	}

}
