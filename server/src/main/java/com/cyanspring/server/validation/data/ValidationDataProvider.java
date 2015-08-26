package com.cyanspring.server.validation.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.event.marketdata.AllQuoteExtSubEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketdata.MultiQuoteExtendEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.QuoteExtSubEvent;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;

public class ValidationDataProvider implements IPlugin, IQuoteExtProvider {
	private static final Logger log = LoggerFactory
			.getLogger(ValidationDataProvider.class);
	private static final String ID = "VDP-"
			+ IdGenerator.getInstance().getNextID();
	private static final String SENDER = ValidationDataProvider.class
			.getSimpleName();
	private ConcurrentHashMap<String, DataObject> quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
	private ConcurrentHashMap<String, MarketSessionData> symbolSessionMap = new ConcurrentHashMap<String, MarketSessionData>();
	private Date tradeDate = null;
	private String tradeDateFormat = "yyyy-MM-dd";

	@Autowired
	protected IRemoteEventManager eventManager;

	@Autowired
	protected MarketSessionUtil marketSessionUtil;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteExtEvent.class, null);
			subscribeToEvent(TradeDateEvent.class, null);
			subscribeToEvent(MultiQuoteExtendEvent.class, null);
			subscribeToEvent(IndexSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processIndexSessionEvent(IndexSessionEvent event){

		ConcurrentHashMap<String, MarketSessionData> updateMap = getSymbolMapFromIndexSessionMap(event.getDataMap(),null);
		if( null == updateMap)
			return;
		Set <Entry<String,MarketSessionData>>entrys = updateMap.entrySet();
		List<String>symbolList = new ArrayList<String>();
		for(Entry<String,MarketSessionData> entry : entrys){
			String symbol = entry.getKey();
			MarketSessionData session = entry.getValue();
			symbolSessionMap.put(symbol, session);
			if(MarketSessionType.PREOPEN.equals(session.getSessionType())){
				symbolList.add(symbol);
				if(quoteExtendsMap.containsKey(symbol)){
					quoteExtendsMap.remove(symbol);
				}
			}
		}
		if(!symbolList.isEmpty()){
			QuoteExtSubEvent requestQuoteExtEvent = new QuoteExtSubEvent(ValidationDataProvider.ID, ValidationDataProvider.SENDER, symbolList);
			eventManager.sendEvent(requestQuoteExtEvent);
		}
	}

	private List<String> getSymbolList(String key) {
		return marketSessionUtil.getSymbolListByIndexSessionKey(key);
	}

	private ConcurrentHashMap<String, MarketSessionData> getSymbolMapFromIndexSessionMap(
			Map<String, MarketSessionData> map, MarketSessionType type) {
		// type == null --> get all
		Set<Entry<String, MarketSessionData>> entrys = map.entrySet();
		ConcurrentHashMap<String, MarketSessionData> symbolMap = new ConcurrentHashMap<String, MarketSessionData>();

		for (Entry<String, MarketSessionData> entry : entrys) {
			String key = entry.getKey();
			MarketSessionData session = entry.getValue();
			MarketSessionType sessionType = session.getSessionType();
			List<String> symbols = getSymbolList(key);
			if (null != symbols && !symbols.isEmpty())
				continue;

			if (null == type || type.equals(sessionType)) {
				for (String symbol : symbols) {
					symbolMap.put(symbol, session);
				}
			}
		}

		return symbolMap;
	}

	@Override
	public void init() throws Exception {

		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName("ValidationDataProvider");
		}
		requestTradeDate();
	}

	@Override
	public void uninit() {
		quoteExtendsMap = null;
		eventProcessor.uninit();
	}

	@Override
	public ConcurrentHashMap<String, DataObject> getQuoteExtMap() {
		return quoteExtendsMap;
	}

	private void requestIndexSession() {
		IndexSessionRequestEvent event = new IndexSessionRequestEvent(
				ValidationDataProvider.ID, ValidationDataProvider.SENDER, null);
		eventManager.sendEvent(event);
	}

	private void requestTradeDate() {
		TradeDateRequestEvent request = new TradeDateRequestEvent(
				ValidationDataProvider.ID, ValidationDataProvider.SENDER);
		eventManager.sendEvent(request);
	}

	public void processMultiQuoteExtendEvent(MultiQuoteExtendEvent event) {
		Map<String, DataObject> receiveDataMap = event.getMutilQuoteExtend();

		if (null == receiveDataMap || 0 == receiveDataMap.size()) {
			log.warn(" MultiQuoteExtendEvent reply doesn't contains any data ");
			return;
		} else {
			log.info("receiveData size:" + receiveDataMap.size());
		}

		if (null == quoteExtendsMap) {
			quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		}

		Set<Entry<String, DataObject>> entrys = receiveDataMap.entrySet();
		for (Entry<String, DataObject> entry : entrys) {
			String key = entry.getKey();
			DataObject data = entry.getValue();
			String symbol = data.get(String.class,
					QuoteExtDataField.SYMBOL.value());
			Date lastTradeDate = data.get(Date.class,
					QuoteExtDataField.TIMESTAMP.value());
			if (!StringUtils.hasText(symbol))
				continue;

			if (null == lastTradeDate) {
				log.info("this symbol doesn't have trade date:{}", symbol);
				continue;
			}

			String symbolTradeDate = TimeUtil.formatDate(lastTradeDate,"yyyy-MM-dd");
			try {
				if (symbolSessionMap.contains(symbol)) {
					MarketSessionData session = symbolSessionMap.get(symbol);
					if (!session.getTradeDateByString().equals(symbolTradeDate)) {
						continue;
					}
					quoteExtendsMap.put(key, data);
				} else {
					log.info("symbolsessionMap doesn't have:{}", symbol);

					if (null != tradeDate && !isSameTradeDate(symbolTradeDate)) {
						log.info(
								"quote trade date:{} not match validation trade date:{}",
								symbolTradeDate, tradeDate);
						continue;
					}
					quoteExtendsMap.put(key, data);
				}
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	public void processQuoteExtEvent(QuoteExtEvent event) {
		try {
			DataObject updateObj = event.getQuoteExt();
			String symbol = updateObj.get(String.class,
					QuoteExtDataField.SYMBOL.value());
			if (null == quoteExtendsMap) {
				quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
			}
			if (null != symbol) {

				Map<String, Object> paramMap = updateObj.getFields();
				if (null == paramMap || paramMap.isEmpty()) {
					return;
				}
				Iterator<Entry<String, Object>> paramIterator = paramMap
						.entrySet().iterator();
				DataObject obj = null;
				if (quoteExtendsMap.containsKey(symbol)) {
					obj = quoteExtendsMap.get(symbol);
				} else {
					obj = updateObj;
				}

				while (paramIterator.hasNext()) {
					Entry<String, Object> entry = paramIterator.next();
					String key = entry.getKey();
					Object value = entry.getValue();
					if (null == key || null == value) {
						continue;
					}
					obj.put(entry.getKey(), entry.getValue());
				}
				quoteExtendsMap.put(symbol, obj);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public void sendQuoteExtSubEvent() {
		quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		AllQuoteExtSubEvent event = new AllQuoteExtSubEvent(
				ValidationDataProvider.ID, ValidationDataProvider.SENDER);
		log.info("send QuoteExtSub event");
		eventManager.sendEvent(event);
	}

	public void processTradeDateEvent(TradeDateEvent event) {
		try {
			String eventTradeDate = event.getTradeDate();
			if (null == eventTradeDate)
				return;

			if (null == tradeDate || !isSameTradeDate(eventTradeDate)) {
				setTradeDate(eventTradeDate);
				requestIndexSession();
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	private boolean isSameTradeDate(String date) throws ParseException {
		Date dateC = TimeUtil.parseDate(date, tradeDateFormat);
		return TimeUtil.sameDate(tradeDate, dateC);
	}

	private void setTradeDate(String td) throws ParseException {
		if (null != td && !"".equals(td)) {
			tradeDate = TimeUtil.parseDate(td, tradeDateFormat);
		}
	}
}
