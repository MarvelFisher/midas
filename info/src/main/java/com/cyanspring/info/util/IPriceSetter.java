package com.cyanspring.info.util;

import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.info.cdp.SymbolData;

public interface IPriceSetter {
	public boolean setPrice(HistoricalPrice price, Quote quote, double LastVolume);
	public boolean setDataPrice(SymbolData symboldata, Quote quote);

}
