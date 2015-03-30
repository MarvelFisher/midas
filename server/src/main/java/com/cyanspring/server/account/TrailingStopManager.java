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
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.fx.FxUtils;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;

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
	
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	private Map<String, Double> highs = new HashMap<String, Double>();
	private Map<String, Double> lows = new HashMap<String, Double>();
	private Map<String, Map<String, PositionPeakPrice>> prices = new HashMap<String, Map<String, PositionPeakPrice>>(); // symbol/account
	private long timerInterval;
	private AsyncTimerEvent timerEvent;
	private int refPriceType = 0; // 0 mid price; 1 last price;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};

	@Override
	public void init() throws Exception {
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("TrailingStopManager");
		
		scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		eventProcessor.uninit();
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		workTrailingStop();
	}
	
	private void workTrailingStop() {
		List<AccountSetting> settings = accountKeeper.getTrailingStopSettings();
		
		for(AccountSetting setting: settings) {
			Account account = accountKeeper.getAccount(setting.getId());
			if(null == account)
				continue;

			double trailingStop = setting.getTrailingStop();
			if(!PriceUtils.isZero(trailingStop))
				continue;
			
			
			List<OpenPosition> list = positionKeeper.getOverallPosition(account);
			for(OpenPosition position: list) {
				Quote quote = quotes.get(position.getSymbol());
				if(null == quote || quote.isStale())
					continue;

				double refPrice = getRefPrice(quote);
				if(PriceUtils.isZero(refPrice))
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
				} else {
					
					if(ppp.getPosition() > 0 && position.getQty() < 0 || ppp.getPosition() < 0 && position.getQty() > 0) { //side changed
						ppp.setPrice(position.getPrice());
					}
					
					ppp.setPosition(position.getQty());
					
					double pnl = FxUtils.calculatePnL(refDataManager, position.getSymbol(), position.getQty(), 
							(ppp.getPrice()-refPrice));

					double value = FxUtils.calculatePnL(refDataManager, position.getSymbol(), position.getQty(), 
							position.getPrice());
					
					if(PriceUtils.EqualGreaterThan(pnl/value, trailingStop)) {
						if(positionKeeper.checkAccountPositionLock(position.getAccount(), position.getSymbol())) {
							log.info("Account locked for trailing stoping: " + refPrice + ", " +
									ppp.getPrice() + ", " + 
									position);
						}
						log.info("Trailing stoping: " + refPrice + ", " +
								ppp.getPrice() + ", " + 
								position);
						ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
								null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.StopLoss,
								IdGenerator.getInstance().getNextID());
						
						eventManager.sendEvent(event);
					}				
				}
			}
		}
	}

	private double getMidPrice(Quote quote) {
		if(PriceUtils.isZero(quote.getBid()))
			return quote.getAsk();
		
		if(PriceUtils.isZero(quote.getAsk()))
			return quote.getBid();
		
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	private double getLastPrice(Quote quote) {
		if(!PriceUtils.isZero(quote.getLast()))
			return quote.getLast();
		
		if(!PriceUtils.isZero(quote.getClose()))
			return quote.getClose();
		
		return getMidPrice(quote);
	}
	
	private double getRefPrice(Quote quote) {
		if(refPriceType == 0)
			return getMidPrice(quote);
		
		if(refPriceType == 1)
			return getLastPrice(quote);
		
		return getLastPrice(quote);
	}
	
	public void updatePrices(String symbol, double refPrice) {
		Map<String, PositionPeakPrice> accountMap = prices.get(symbol);
		if(null == accountMap) {
			accountMap = new HashMap<String, PositionPeakPrice>();
			prices.put(symbol, accountMap);
		}
		for(PositionPeakPrice ppp: accountMap.values()) {
			if(PriceUtils.GreaterThan(ppp.getPosition(), 0) && PriceUtils.GreaterThan(refPrice, ppp.getPrice()) ||
			   PriceUtils.LessThan(ppp.getPosition(), 0) && PriceUtils.LessThan(refPrice, ppp.getPrice()))
				ppp.setPrice(refPrice);
		}
		log.info("PositionPeakPrice updated: " + accountMap.size());
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		Quote quote = event.getQuote();
		quotes.put(quote.getSymbol(), quote);
		Double high = highs.get(quote.getSymbol());
		double refPrice = getRefPrice(quote);
		if(null == high || (!PriceUtils.isZero(refPrice) && PriceUtils.GreaterThan(refPrice, high))) {
			log.info("Updating new high: " + quote.getSymbol() + ", " + refPrice);
			highs.put(quote.getSymbol(), refPrice);
			updatePrices(quote.getSymbol(), refPrice);
		}
		
		Double low = lows.get(quote.getSymbol());
		refPrice = getRefPrice(quote);
		if(null == low || (!PriceUtils.isZero(refPrice) && PriceUtils.LessThan(refPrice, low))) {
			log.info("Updating new low: " + quote.getSymbol() + ", " + refPrice);
			lows.put(quote.getSymbol(), refPrice);
			updatePrices(quote.getSymbol(), refPrice);
		}
		
	}
}
