package com.cyanspring.common.fx;

import com.cyanspring.common.marketdata.Quote;

public interface IFxConverter {
	Double getFxRate(String symbol);
	double getFxRate(String fromSymbol, String toSymbol) throws FxException;
	double getFxQty(String fromSymbol, String toSymbol, double quantity) throws FxException;
	public void updateRate(String fromSymbol, String toSymbol, double bid, double ask);
	public void updateRate(Quote quote);
}
