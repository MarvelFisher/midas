package com.cyanspring.common.cstw.tick;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.statistic.TickTableReplyEvent;
import com.cyanspring.common.event.statistic.TickTableRequestEvent;
import com.cyanspring.common.staticdata.AbstractTickTable;
import com.cyanspring.common.util.IdGenerator;

public class TickManager implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(TickManager.class);
	private IRemoteEventManager eventManager;
	private Map<AbstractTickTable, List<String>> tickMap = new HashMap<AbstractTickTable, List<String>>();
	private String firstServer = "";

	public TickManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof TickTableReplyEvent) {
			log.info("receive TickTableReplyEvent");
			TickTableReplyEvent evt = (TickTableReplyEvent) event;
			processTickTableReplyEvent(evt);
		}
	}

	private void processTickTableReplyEvent(TickTableReplyEvent evt) {
		Iterator<List<String>> i = evt.getMap().values().iterator();
		Iterator<AbstractTickTable> ii = evt.getMap().keySet().iterator();
//		while (i.hasNext()) {
//			for (String s : i.next()) {
//				log.info("s:{}", s);
//			}
//		}
//		while (ii.hasNext()) {
//			AbstractTickTable a = ii.next();
//			log.info("tick table:{}", a.toString());
//		}

		if (null != evt.getMap())
			tickMap = evt.getMap();
	}

	public AbstractTickTable getTickTable(String symbol){
		Iterator<AbstractTickTable> table = tickMap.keySet().iterator();
		while(table.hasNext()){
			AbstractTickTable tempTable = table.next();
			List<String> symbolList = tickMap.get(tempTable);
			if(symbolList.contains(symbol))
				return tempTable;
		}
		return null;
	}
	
	
	
	private void requestTickTableInfo() {
		TickTableRequestEvent event = new TickTableRequestEvent(IdGenerator
				.getInstance().getNextID(), firstServer, null);
		try {
			eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public void init(String server) {
		subEvent(TickTableReplyEvent.class);
		firstServer = server;
		log.info("firstServer:{}", firstServer);
		log.info("send tick table request");
		requestTickTableInfo();
	}

	public void unInit() {
		unSubEvent(TickTableReplyEvent.class);
	}

	private void subEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.subscribe(clazz, this);
	}

	private void unSubEvent(Class<? extends AsyncEvent> clazz) {
		eventManager.unsubscribe(clazz, this);
	}
}
