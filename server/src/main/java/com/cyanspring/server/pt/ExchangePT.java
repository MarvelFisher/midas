package com.cyanspring.server.pt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.server.bt.ExchangeBT;

public class ExchangePT extends ExchangeBT implements IMarketDataListener, IMarketDataStateListener {
	private static final Logger log = LoggerFactory
			.getLogger(ExchangePT.class);
	private boolean mdState = true;
	IMarketDataAdaptor mdAdaptor;
	
	@Override
	public void init() {
		log.info("initialising...");
		super.init();
		mdAdaptor.subscribeMarketDataState(this);
	}
	
	@Override
	public boolean getState() {
		return mdState;
	}

	@Override
	public void onState(boolean on) {
		mdState = on;
		if(null != stateListener)
			stateListener.onState(on);
	}

	@Override
	public void onQuote(Quote quote, int sourceId) {
		super.processQuote(quote);
	}

	@Override
	public void onTrade(Trade trade) {
		//ignore trade, assuming exchangeBT will work on trades
	}

	@Override
	public void subscribeMarketData(String instrument,
			IMarketDataListener listener) throws MarketDataException {
		this.mdListener = listener; // supports only one listener
		Quote quote = currentQuotes.get(instrument);
		if(quote == null)
			mdAdaptor.subscribeMarketData(instrument, this);
		if(null != quote && null!= mdListener)
			mdListener.onQuote(quote, 1);
	}
	
	public IMarketDataAdaptor getMdAdaptor() {
		return mdAdaptor;
	}

	public void setMdAdaptor(IMarketDataAdaptor mdAdaptor) {
		this.mdAdaptor = mdAdaptor;
	}

	
}
