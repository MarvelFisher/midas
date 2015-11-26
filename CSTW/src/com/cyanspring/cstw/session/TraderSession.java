package com.cyanspring.cstw.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.order.InitClientEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleOrderStrategyFieldDefUpdateEvent;
import com.cyanspring.cstw.business.CSTWEventManager;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.09
 *
 */
public final class TraderSession {

	private static final Logger log = LoggerFactory
			.getLogger(TraderSession.class);

	private static TraderSession instance;

	private EventListenerImpl listener;

	private List<String> singleOrderDisplayFieldList;
	private List<String> singleInstrumentDisplayFieldList;
	private List<String> multiInstrumentDisplayFieldList;

	private Map<String, Map<String, FieldDef>> singleOrderFieldDefMap;
	private Map<String, Map<String, FieldDef>> singleInstrumentFieldDefMap;
	private Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentFieldDefMap;

	public static TraderSession getInstance() {
		if (instance == null) {
			instance = new TraderSession();
		}
		return instance;
	}

	private TraderSession() {
		listener = new EventListenerImpl();
	}

	public void initByEvnet(InitClientEvent initClientEvent) {
		singleOrderFieldDefMap = initClientEvent.getSingleOrderFieldDefs();
		singleOrderDisplayFieldList = initClientEvent
				.getSingleOrderDisplayFields();
		singleInstrumentFieldDefMap = initClientEvent
				.getSingleInstrumentFieldDefs();
		singleInstrumentDisplayFieldList = initClientEvent
				.getSingleInstrumentDisplayFields();
		multiInstrumentDisplayFieldList = initClientEvent
				.getMultiInstrumentDisplayFields();
		multiInstrumentFieldDefMap = initClientEvent
				.getMultiInstrumentStrategyFieldDefs();

		CSTWEventManager.subscribe(
				SingleOrderStrategyFieldDefUpdateEvent.class, listener);
		CSTWEventManager.subscribe(
				SingleInstrumentStrategyFieldDefUpdateEvent.class, listener);
		CSTWEventManager.subscribe(
				MultiInstrumentStrategyFieldDefUpdateEvent.class, listener);
	}

	public List<String> getSingleOrderDisplayFieldList() {
		return singleOrderDisplayFieldList;
	}

	public void setSingleOrderDisplayFieldList(
			List<String> singleOrderDisplayFieldList) {
		this.singleOrderDisplayFieldList = singleOrderDisplayFieldList;
	}

	public List<String> getSingleInstrumentDisplayFieldList() {
		return singleInstrumentDisplayFieldList;
	}

	public void setSingleInstrumentDisplayFieldList(
			List<String> singleInstrumentDisplayFieldList) {
		this.singleInstrumentDisplayFieldList = singleInstrumentDisplayFieldList;
	}

	public List<String> getMultiInstrumentDisplayFieldList() {
		return multiInstrumentDisplayFieldList;
	}

	public void setMultiInstrumentDisplayFieldList(
			List<String> multiInstrumentDisplayFieldList) {
		this.multiInstrumentDisplayFieldList = multiInstrumentDisplayFieldList;
	}

	public Map<String, Map<String, FieldDef>> getSingleOrderFieldDefMap() {
		return singleOrderFieldDefMap;
	}

	public void setSingleOrderFieldDefMap(
			Map<String, Map<String, FieldDef>> singleOrderFieldDefMap) {
		this.singleOrderFieldDefMap = singleOrderFieldDefMap;
	}

	public Map<String, Map<String, FieldDef>> getSingleInstrumentFieldDefMap() {
		return singleInstrumentFieldDefMap;
	}

	public void setSingleInstrumentFieldDefMap(
			Map<String, Map<String, FieldDef>> singleInstrumentFieldDefMap) {
		this.singleInstrumentFieldDefMap = singleInstrumentFieldDefMap;
	}

	public Map<String, MultiInstrumentStrategyDisplayConfig> getMultiInstrumentFieldDefMap() {
		return multiInstrumentFieldDefMap;
	}

	public void setMultiInstrumentFieldDefMap(
			Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentFieldDefMap) {
		this.multiInstrumentFieldDefMap = multiInstrumentFieldDefMap;
	}

	public List<String> getSingleInstrumentAmendableFields(String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleInstrumentFieldDefMap.get(key);
		if (null != fieldDefs) {
			for (FieldDef fieldDef : fieldDefs.values()) {
				if (fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
	}

	public List<String> getSingleOrderAmendableFields(String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleOrderFieldDefMap.get(key);
		if (null != fieldDefs) {
			for (FieldDef fieldDef : fieldDefs.values()) {
				if (fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
	}

	class EventListenerImpl implements IAsyncEventListener {
		@Override
		public void onEvent(AsyncEvent event) {
			if (event instanceof SingleOrderStrategyFieldDefUpdateEvent) {
				processingSingleOrderStrategyFieldDefUpdateEvent((SingleOrderStrategyFieldDefUpdateEvent) event);
			} else if (event instanceof SingleInstrumentStrategyFieldDefUpdateEvent) {
				processingSingleInstrumentStrategyFieldDefUpdateEvent((SingleInstrumentStrategyFieldDefUpdateEvent) event);
			} else if (event instanceof MultiInstrumentStrategyFieldDefUpdateEvent) {
				processingMultiInstrumentStrategyFieldDefUpdateEvent((MultiInstrumentStrategyFieldDefUpdateEvent) event);
			}
		}
	}

	private synchronized void processingSingleOrderStrategyFieldDefUpdateEvent(
			SingleOrderStrategyFieldDefUpdateEvent event) {
		singleOrderFieldDefMap.put(event.getName(), event.getFieldDefs());
		log.info("Single-order strategy field def update: " + event.getName());
	}

	private synchronized void processingSingleInstrumentStrategyFieldDefUpdateEvent(
			SingleInstrumentStrategyFieldDefUpdateEvent event) {
		singleInstrumentFieldDefMap.put(event.getName(), event.getFieldDefs());
		log.info("Single-instrument strategy field def update: "
				+ event.getName());
	}

	private synchronized void processingMultiInstrumentStrategyFieldDefUpdateEvent(
			MultiInstrumentStrategyFieldDefUpdateEvent event) {
		multiInstrumentFieldDefMap.put(event.getConfig().getStrategy(),
				event.getConfig());
		log.info("Multi-Instrument strategy field def update: "
				+ event.getConfig().getStrategy());
	}

}
