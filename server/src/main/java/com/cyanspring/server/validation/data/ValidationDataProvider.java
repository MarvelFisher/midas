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

public class ValidationDataProvider implements IPlugin, IQuoteExtProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ValidationDataProvider.class);
	private static final String ID = "VDP-"+ IdGenerator.getInstance().getNextID();
	private static final String SENDER = ValidationDataProvider.class.getSimpleName();
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
		log.info("get processIndexSessionEvent");
		log.info("event.getDataMap():{}",event.getDataMap().size());
		if(!event.isOk()){
			log.warn("index session event request fail");
			return;
		}
		
		ConcurrentHashMap<String, MarketSessionData> updateMap = getSymbolMapFromIndexSessionMap(event.getDataMap(),null);
		
		if( null == updateMap)
			return;
		
		if( null == quoteExtendsMap)
			quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
		
		log.info("updateMap:{}",updateMap.size());
		Set <Entry<String,MarketSessionData>>entrys = updateMap.entrySet();
		List<String>symbolList = new ArrayList<String>();
		for(Entry<String,MarketSessionData> entry : entrys){
			String symbol = entry.getKey();
			
			MarketSessionData session = entry.getValue();
			symbolSessionMap.put(symbol, session);
			if(MarketSessionType.PREOPEN.equals(session.getSessionType())){
				log.info("get PREOPEN type:{}",symbol);
				symbolList.add(symbol);
				if(quoteExtendsMap.containsKey(symbol)){
					quoteExtendsMap.remove(symbol);
				}else{
					log.info("doesn't exist quoteExtendMap:{}",symbol);
				}
				continue;
			}
			
			if(!quoteExtendsMap.containsKey(symbol)){
				symbolList.add(symbol);
				continue;
			}
		}
		
		log.info("symbol list:{}",symbolList.size());
		if( null == quoteExtendsMap || quoteExtendsMap.size()<=0){			
			sendQuoteExtSubEvent();
		}else if(!symbolList.isEmpty()){		
			QuoteExtSubEvent requestQuoteExtEvent = new QuoteExtSubEvent(ValidationDataProvider.ID, ValidationDataProvider.SENDER, symbolList);
			eventManager.sendEvent(requestQuoteExtEvent);
		}
		
//		Set<Entry<String, MarketSessionData>> entrySet =  symbolSessionMap.entrySet();
//		for(Entry <String,MarketSessionData> data : entrySet){
//			log.info("print key:{} value:{} trade date:{}",new Object[]{data.getKey(),data.getValue().getSessionType(),data.getValue().getTradeDateByString()});
//		}
		
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
			if (null == symbols || symbols.isEmpty())
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
		log.info("request requestIndexSession");
		requestIndexSession();
		log.info("request sendQuoteExtSubEvent");
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
		log.info("send requestIndexSession ");
		IndexSessionRequestEvent event = new IndexSessionRequestEvent(
				ValidationDataProvider.ID, ValidationDataProvider.SENDER, null);
		eventManager.sendEvent(event);
	}

	public void processMultiQuoteExtendEvent(MultiQuoteExtendEvent event) {
		
		Map<String, DataObject> receiveDataMap = event.getMutilQuoteExtend();
		log.info("get processMultiQuoteExtendEvent:{}",receiveDataMap.size());
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
		boolean missingIndexSession = false;
		for (Entry<String, DataObject> entry : entrys) {
			String key = entry.getKey();
			DataObject data = entry.getValue();
			String symbol = data.get(String.class,
					QuoteExtDataField.SYMBOL.value());
			Date lastTradeDate = data.get(Date.class,
					QuoteExtDataField.TIMESTAMP.value());
			log.info("MuliteQuote:{},{}",symbol,lastTradeDate);
			if (!StringUtils.hasText(symbol))
				continue;

			if (null == lastTradeDate) {
				log.info("this symbol doesn't have trade date:{}", symbol);
				continue;
			}
			
			String symbolTradeDate = TimeUtil.formatDate(lastTradeDate,"yyyy-MM-dd");
			try {
				
				if (symbolSessionMap.containsKey(symbol)) {
					MarketSessionData session = symbolSessionMap.get(symbol);
					if (!session.getTradeDateByString().equals(symbolTradeDate)) {
						log.info("not same trade date compare session:{}",symbol);
						continue;
					}
					quoteExtendsMap.put(key, data);
				}else{
					log.info("missing index session :{}",symbol);
					missingIndexSession = true;
				}
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		if(missingIndexSession)		
			requestIndexSession();	
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

}
