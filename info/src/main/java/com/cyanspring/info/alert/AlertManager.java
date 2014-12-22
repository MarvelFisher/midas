package com.cyanspring.info.alert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.alert.PriceAlert;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.alert.CreatePriceAlertRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.event.AsyncEventProcessor;

public class AlertManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(AlertManager.class);

	@Autowired
	ScheduleManager scheduleManager;

	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	private boolean tradeAlert;
	private boolean priceAlert;
	private IPriceAlertSender priceAlertSender;
	private ITradeAlertSender tradeAlertSender;
	
	private Map<String, List<PriceAlert>> symbolPriceAlerts = new HashMap<String, List<PriceAlert>>();
	private Map<String, List<PriceAlert>> accountPriceAlerts = new HashMap<String, List<PriceAlert>>();
	private int maxNoOfAlerts = 20;
	private Map<String, Quote> quotes = new HashMap<String, Quote>();
	
	private boolean addPriceAlert(PriceAlert priceAlert) {
		List<PriceAlert> list;
		list = accountPriceAlerts.get(priceAlert.getAccount());
		if(null == list) {
			list = new LinkedList<PriceAlert>();
			accountPriceAlerts.put(priceAlert.getAccount(), list);
		} else if(list.size() >= maxNoOfAlerts)
			return false;
		list.add(priceAlert);
		
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null == list) {
			list = new LinkedList<PriceAlert>();
			symbolPriceAlerts.put(priceAlert.getSymbol(), list);
		}
		list.add(priceAlert);
		return true;
	}
	
	private void removePriceAlert(PriceAlert priceAlert) {
		List<PriceAlert> list;
		list = symbolPriceAlerts.get(priceAlert.getSymbol());
		if(null != list)
			list.remove(priceAlert);
		
		list = accountPriceAlerts.get(priceAlert.getAccount());
		if(null != list)
			list.remove(priceAlert);
	}
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateChildOrderEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(CreatePriceAlertRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}

	};
	
	public void processUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		if(null == event.getExecution())
			return;
		
		if(null != tradeAlertSender)
			tradeAlertSender.sendTradeAlert(event.getExecution());
	}
	
	public void processQuoteEvent(QuoteEvent event) {
		log.debug("Quote: " + event.getQuote());
		Quote quote = event.getQuote();
		quotes.put(quote.getSymbol(), quote);
		List<PriceAlert> list = symbolPriceAlerts.get(quote.getSymbol());
		if(null == list)
			return;
		for(PriceAlert alert: list) {
			firePriceAlert(alert, quote);
		}
	}
	
	private double getAlertPrice(Quote quote) {
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	private boolean firePriceAlert(PriceAlert alert, Quote quote) {
		double currentPrice = getAlertPrice(quote);
		if(PriceUtils.isZero(alert.getStartPrice())) {
			alert.setStartPrice(currentPrice);
		} else if (PriceUtils.Equal(alert.getPrice(), currentPrice) || // condition 1
			PriceUtils.EqualGreaterThan(alert.getPrice(), alert.getStartPrice()) &&
			PriceUtils.EqualGreaterThan(currentPrice, alert.getPrice()) ||  // condition 2
			PriceUtils.EqualLessThan(alert.getPrice(), alert.getStartPrice()) &&
			PriceUtils.EqualLessThan(currentPrice, alert.getPrice())) { // condition 3
			if(null != priceAlertSender)
				priceAlertSender.sendPriceAlert(alert);
			return true;
		}
		return false;
	}

	public void processCreatePriceAlertRequestEvent(CreatePriceAlertRequestEvent event) {
		boolean ok = addPriceAlert(event.getPriceAlert());
		String message = null;
		if(!ok)
			message = "Price alert has over per account limit";
			
	}

	@Override
	public void init() throws Exception {
		log.info("Initialising...");
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("AlertManager");
	}

	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessor.uninit();
	}
	
	// getters and setters
	public boolean isTradeAlert() {
		return tradeAlert;
	}

	public void setTradeAlert(boolean tradeAlert) {
		this.tradeAlert = tradeAlert;
	}

	public boolean isPriceAlert() {
		return priceAlert;
	}

	public void setPriceAlert(boolean priceAlert) {
		this.priceAlert = priceAlert;
	}

	public IPriceAlertSender getPriceAlertSender() {
		return priceAlertSender;
	}

	public void setPriceAlertSender(IPriceAlertSender priceAlertSender) {
		this.priceAlertSender = priceAlertSender;
	}

	public ITradeAlertSender getTradeAlertSender() {
		return tradeAlertSender;
	}

	public void setTradeAlertSender(ITradeAlertSender tradeAlertSender) {
		this.tradeAlertSender = tradeAlertSender;
	}

	
}
