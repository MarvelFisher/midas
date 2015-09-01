package com.cyanspring.server.validation.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketdata.AllQuoteExtSubEvent;
import com.cyanspring.common.event.marketdata.MultiQuoteExtendEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.QuoteExtSubEvent;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;

public class IndexValidationDataProvider implements IPlugin, IQuoteExtProvider {
	
	private static final Logger log = LoggerFactory.getLogger(IndexValidationDataProvider.class);
	private static final String ID = "VDP-"+ IdGenerator.getInstance().getNextID();
	private static final String SENDER = IndexValidationDataProvider.class.getSimpleName();
	private ConcurrentHashMap<String, DataObject> quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
	private ConcurrentHashMap<String, MarketSessionData> symbolSessionMap = new ConcurrentHashMap<String, MarketSessionData>();
	
	@Autowired
	protected IRemoteEventManager eventManager;

	@Autowired
	protected MarketSessionUtil marketSessionUtil;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteExtEvent.class, null);
			subscribeToEvent(MultiQuoteExtendEvent.class, null);
			subscribeToEvent(IndexSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processIndexSessionEvent(IndexSessionEvent event){
		if(!event.isOk()){
			log.warn("index session event request is not ok");
			return;
		}
		
		ConcurrentHashMap<String, MarketSessionData> updateMap = getSymbolMapFromIndexSessionMap(event.getDataMap(),null);
		
		if( null == updateMap)
			return;
		
		if( null == quoteExtendsMap)
			quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		
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
				}else{
					log.info("doesn't exist quoteExtendMap:{}",symbol);
				}
				continue;
			}
			
			if(!checkTradeDate(symbol,session) && quoteExtendsMap.containsKey(symbol)){
				quoteExtendsMap.remove(symbol);
				symbolList.add(symbol);
			}
			
			if(!quoteExtendsMap.containsKey(symbol)){
				symbolList.add(symbol);
				continue;
			}
		}
		
		if( null == quoteExtendsMap || quoteExtendsMap.size()<=0){			
			sendQuoteExtSubEvent();
		}else if(!symbolList.isEmpty()){
			log.info("Request QuoteExtSubEvent symbols:{}",symbolList.size());
			QuoteExtSubEvent requestQuoteExtEvent = new QuoteExtSubEvent(IndexValidationDataProvider.ID, IndexValidationDataProvider.SENDER, symbolList);
			eventManager.sendEvent(requestQuoteExtEvent);
		}
	}

	private boolean checkTradeDate(String symbol, MarketSessionData session) {
		if(!quoteExtendsMap.containsKey(symbol))
			return false;
		
		String indexTradeDate = session.getTradeDateByString();
		DataObject dataObject = quoteExtendsMap.get(symbol);
		if(null != dataObject){
			Date lastTradeDate = dataObject.get(Date.class,QuoteExtDataField.TIMESTAMP.value());
			String symbolTradeDate = TimeUtil.formatDate(lastTradeDate,"yyyy-MM-dd");
			if(indexTradeDate.equals(symbolTradeDate)){
				return true;
			}
		}

		return false;
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
			log.info("get Index Session:{},{},{}",new Object[]{key,sessionType,session.getTradeDateByString()});
			List<String> symbols = getSymbolList(key);
			
			if (null == symbols || symbols.isEmpty())
				continue;

			log.info("{} get symbols:{}",key,symbols.size());
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
		requestIndexSession();
		sendQuoteExtSubEvent();
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
		log.info("Send request Index Session Event");
		IndexSessionRequestEvent event = new IndexSessionRequestEvent(
				IndexValidationDataProvider.ID, IndexValidationDataProvider.SENDER, null);
		eventManager.sendEvent(event);
	}

	public void processMultiQuoteExtendEvent(MultiQuoteExtendEvent event) {
		
		Map<String, DataObject> receiveDataMap = event.getMutilQuoteExtend();
		if (null == receiveDataMap || 0 == receiveDataMap.size()) {
			log.warn(" MultiQuoteExtendEvent reply doesn't contains any data ");
			return;
		} else {
			log.info("Process MultiQuoteExtend receiveData size:" + receiveDataMap.size());
		}

		if (null == quoteExtendsMap) {
			quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		}

		Set<Entry<String, DataObject>> entrys = receiveDataMap.entrySet();
		for (Entry<String, DataObject> entry : entrys) {
			String key = entry.getKey();
			DataObject data = entry.getValue();
			String symbol = data.get(String.class,QuoteExtDataField.SYMBOL.value());
			Date lastTradeDate = data.get(Date.class,QuoteExtDataField.TIMESTAMP.value());
//			log.info("Multi Quote:{},{}",symbol,lastTradeDate);
			if (!StringUtils.hasText(symbol))
				continue;

			if (null == lastTradeDate) {
				log.info("this symbol doesn't have trade date:{}", symbol);
				continue;
			}
			
			String symbolTradeDate = TimeUtil.formatDate(lastTradeDate,"yyyy-MM-dd");
			try {
				if(null == symbolSessionMap || symbolSessionMap.isEmpty()){
					log.info("IndexSessionMap is empty, waiting AllIndexSession initializing");
					break;
				}
				
				if (symbolSessionMap.containsKey(symbol)) {
					MarketSessionData session = symbolSessionMap.get(symbol);
					if (!session.getTradeDateByString().equals(symbolTradeDate)) {
						log.info("not same trade date compare session:{},index:{},quote:{}",new Object[]{symbol,session.getTradeDateByString(),symbolTradeDate});
						continue;
					}
					quoteExtendsMap.put(key, data);
				}else{
					log.info("waiting index session :{}",symbol);
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
		log.info("Send All QuoteExtSub event");
		quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		AllQuoteExtSubEvent event = new AllQuoteExtSubEvent(
				IndexValidationDataProvider.ID, IndexValidationDataProvider.SENDER);
		eventManager.sendEvent(event);
	}

}
