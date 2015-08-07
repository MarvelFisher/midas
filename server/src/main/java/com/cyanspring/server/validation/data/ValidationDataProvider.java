package com.cyanspring.server.validation.data;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketdata.MultiQuoteExtendEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.QuoteExtSubEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.system.SuspendServerEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;

public class ValidationDataProvider implements IPlugin, IQuoteExtProvider {
	private static final Logger log = LoggerFactory.getLogger(ValidationDataProvider.class);
	private static final String ID = "VDP-" + IdGenerator.getInstance().getNextID();
	private static final String SENDER = ValidationDataProvider.class.getSimpleName();
	private ConcurrentHashMap<String, DataObject> quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
	private Date tradeDate = null;
	private String tradeDateFormat = "yyyy-MM-dd";

	@Autowired
	protected IRemoteEventManager eventManager;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteExtEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(TradeDateEvent.class, null);
			subscribeToEvent(MultiQuoteExtendEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	@Override
	public void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName("ValidationDataProvider");
		}
		requestMarketSession();
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

	public void requestMarketSession() {
		eventManager.sendEvent(
				new MarketSessionRequestEvent(ValidationDataProvider.ID, ValidationDataProvider.SENDER, true));
	}

	public void processMarketSessionEvent(MarketSessionEvent event) {
		try {
			Date oldTradeDate = tradeDate;
			String td = event.getTradeDate();

			if (null == oldTradeDate || MarketSessionType.PREOPEN == event.getSession()) {
				setTradeDate(td);
				sendQuoteExtSubEvent();
			}
		} catch (ParseException e) {
			log.warn("Trade date parse error:" + event.getTradeDate(), e);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
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

		quoteExtendsMap.putAll(receiveDataMap);
	}

	public void processQuoteExtEvent(QuoteExtEvent event) {
		try {
			DataObject updateObj = event.getQuoteExt();
			String symbol = updateObj.get(String.class, QuoteExtDataField.SYMBOL.value());
			if (null == quoteExtendsMap) {
				quoteExtendsMap = new ConcurrentHashMap<String, DataObject>();
			}
			if (null != symbol) {

				Map<String, Object> paramMap = updateObj.getFields();
				if (null == paramMap || paramMap.isEmpty()) {
					return;
				}
				Iterator<Entry<String, Object>> paramIterator = paramMap.entrySet().iterator();
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
		QuoteExtSubEvent event = new QuoteExtSubEvent(ValidationDataProvider.ID, ValidationDataProvider.SENDER);
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
				sendQuoteExtSubEvent();
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
