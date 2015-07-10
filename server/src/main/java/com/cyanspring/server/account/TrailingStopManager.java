package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.PositionPeakPrice;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.PmPositionPeakPriceDeleteEvent;
import com.cyanspring.common.event.account.PmPositionPeakPriceUpdateEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PerfDurationCounter;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.common.event.AsyncEventProcessor;

public class TrailingStopManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(TrailingStopManager.class);

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private AccountKeeper accountKeeper;
	
	@Autowired
	private PositionKeeper positionKeeper;
	
	@Autowired
	IRefDataManager refDataManager;

	private ScheduleManager scheduleManager = new ScheduleManager();
	private PerfDurationCounter perfCounter;
	private long perfUpdateInterval = 20000;
	
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	private Map<String, Map<String, PositionPeakPrice>> prices = new HashMap<String, Map<String, PositionPeakPrice>>(); // symbol/account
	private Map<String, PositionPeakPrice> pendingUpdate = new HashMap<String, PositionPeakPrice>();
	private TimeThrottler throttler;
	private long persistInterval = 5000;
	private long timerInterval = 1000;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private int refPriceType = 0; // 0 marketable price; 1 last price; 2 mid price;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(OpenPositionUpdateEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};

	@Override
	public void init() throws Exception {
		perfCounter = new PerfDurationCounter("trailing stop processing", perfUpdateInterval);
		throttler = new TimeThrottler(persistInterval);
		
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("TrailingStopManager");
		
		scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
	}

	@Override
	public void uninit() {
		scheduleManager.uninit();
		eventProcessor.uninit();
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		perfCounter.start();
		workTrailingStop();
		if(throttler.check()) {
			persistPositionPeakPrices();
		}
		perfCounter.end();
	}
	
	private void persistPositionPeakPrices() {
		Map<String, PositionPeakPrice> sendingUpdate = pendingUpdate;
		pendingUpdate = new HashMap<String, PositionPeakPrice>();
		eventManager.sendEvent(new PmPositionPeakPriceUpdateEvent(sendingUpdate.values()));
	}
	
	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		OpenPosition position = event.getPosition();
		if(PriceUtils.isZero(position.getQty())) {
			Map<String, PositionPeakPrice> accountMap = prices.get(position.getSymbol());
			if(null == accountMap) {
				return;
			}
			PositionPeakPrice ppp = accountMap.remove(position.getAccount());
			if(null != ppp) {
				eventManager.sendEvent(new PmPositionPeakPriceDeleteEvent(ppp));
				log.debug("Reseting trailing stop record: " + position.getAccount() + position.getSymbol());
			}
		}
	}
	
	private void workTrailingStop() {
		List<AccountSetting> settings = accountKeeper.getTrailingStopSettings();
		
		for(AccountSetting setting: settings) {
			Account account = accountKeeper.getAccount(setting.getId());
			if(null == account)
				continue;

			double trailingStop = setting.getTrailingStop();
			if(PriceUtils.isZero(trailingStop))
				continue;
			
			List<OpenPosition> list = positionKeeper.getOverallPosition(account);
			for(OpenPosition position: list) {
				Quote quote = quotes.get(position.getSymbol());
				if(null == quote || quote.isStale())
					continue;

				
				if(PriceUtils.isZero(position.getQty()))
					continue;
				
				Map<String, PositionPeakPrice> accountMap = prices.get(position.getSymbol());
				if(null == accountMap) {
					accountMap = new HashMap<String, PositionPeakPrice>();
					prices.put(position.getSymbol(), accountMap);
				}
				PositionPeakPrice ppp = accountMap.get(account.getId());
				if(null == ppp) {
					ppp = new PositionPeakPrice(position.getAccount(), position.getSymbol(), position.getQty(), position.getPrice());
					accountMap.put(position.getAccount(), ppp);
				} 
					
				if(ppp.getPosition() > 0 && position.getQty() < 0 || ppp.getPosition() < 0 && position.getQty() > 0) { //side changed
					ppp.setPrice(position.getPrice());
				}
				
				ppp.setPosition(position.getQty());
				
				double refPrice = getRefPrice(quote, ppp.getPosition());
				if(PriceUtils.isZero(refPrice))
					continue;

				double delta = position.getQty() > 0?(ppp.getPrice() - refPrice) : (refPrice - ppp.getPrice());
				if(PriceUtils.EqualGreaterThan(delta/position.getPrice(), trailingStop)) {
					if(positionKeeper.checkAccountPositionLock(position.getAccount(), position.getSymbol())) {
						log.info("Account locked for trailing stoping: " + refPrice + ", " +
								ppp.getPrice() + ", " + 
								position);
						continue;
					}
					log.info("Trailing stoping: " + refPrice + ", " +
							ppp.getPrice() + ", " + 
							position + ", " +
							trailingStop);
					ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
							null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.TrailingStop,
							IdGenerator.getInstance().getNextID());
					
					eventManager.sendEvent(event);
				}				

			}
		}
	}

	
	private double getRefPrice(Quote quote, double qty) {
		switch(refPriceType) {
			case 0:
				return QuoteUtils.getMarketablePrice(quote, qty);
			case 1:
				return QuoteUtils.getLastPrice(quote);
			case 2:
				return QuoteUtils.getMidPrice(quote);
			default:
				return QuoteUtils.getMarketablePrice(quote, qty);
		}
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = event.getQuote();
		quotes.put(quote.getSymbol(), quote);
		Map<String, PositionPeakPrice> accountMap = prices.get(quote.getSymbol());
		if(null == accountMap) {
			accountMap = new HashMap<String, PositionPeakPrice>();
			prices.put(quote.getSymbol(), accountMap);
		}
		for(PositionPeakPrice ppp: accountMap.values()) {
			double refPrice = getRefPrice(quote, ppp.getPosition());
			if(PriceUtils.GreaterThan(ppp.getPosition(), 0) && PriceUtils.GreaterThan(refPrice, ppp.getPrice()) ||
			   PriceUtils.LessThan(ppp.getPosition(), 0) && PriceUtils.LessThan(refPrice, ppp.getPrice())) {
				ppp.setPrice(refPrice);
				pendingUpdate.put(ppp.getAccount() + "-" + ppp.getSymbol(), ppp);
				log.debug("PositionPeakPrice updated: " + ppp.getAccount() + ", " + ppp.getSymbol() + ", " + refPrice + "," + accountMap.size());
			}
		}
	
	}
	
	public void injectPositionPeakPrices(List<PositionPeakPrice> list) {
		for(PositionPeakPrice ppp: list) {
			Map<String, PositionPeakPrice> accountMap = prices.get(ppp.getSymbol());
			if(null == accountMap) {
				accountMap = new HashMap<String, PositionPeakPrice>();
				prices.put(ppp.getSymbol(), accountMap);
			}
			accountMap.put(ppp.getAccount(), ppp);
		}
	}

	// getters and setters
	public long getTimerInterval() {
		return timerInterval;
	}

	public void setTimerInterval(long timerInterval) {
		this.timerInterval = timerInterval;
	}

	public int getRefPriceType() {
		return refPriceType;
	}

	public void setRefPriceType(int refPriceType) {
		this.refPriceType = refPriceType;
	}

	public long getPerfUpdateInterval() {
		return perfUpdateInterval;
	}

	public void setPerfUpdateInterval(long perfUpdateInterval) {
		this.perfUpdateInterval = perfUpdateInterval;
	}

	public long getPersistInterval() {
		return persistInterval;
	}

	public void setPersistInterval(long persistInterval) {
		this.persistInterval = persistInterval;
	}
	
}
