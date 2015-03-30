package com.cyanspring.server.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import webcurve.util.PriceUtils;

import com.cyanspring.common.fx.FxException;
import com.cyanspring.common.fx.FxUtils;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.message.ErrorMessage;
public class FxConverter implements IFxConverter {
	private Map<String, Double> fxRates = new HashMap<String, Double>();

	@Override
	public Double getFxRate(String symbol) {
		String fromSymbol = FxUtils.getFromCurrency(symbol);
		String toSymbol = FxUtils.getToCurrency(symbol);
		return fxRates.get(fromSymbol + toSymbol);
	}
	
	@Override
	public double getFxRate(String fromSymbol, String toSymbol) throws FxException {
		if(fromSymbol.equals(toSymbol))
			return 1.0;
		
		Double result = fxRates.get(fromSymbol + toSymbol);
		if(null == result)
			throw new FxException("FxConverter can not find symbol: " + fromSymbol + toSymbol,ErrorMessage.FX_CONVERTER_CANT_FIND_SYMBOL);
		
		if(PriceUtils.isZero(result))
			throw new FxException("FxConverter rate is 0: " + fromSymbol + toSymbol,ErrorMessage.FX_CONVERTER_RATE_IS_ZERO);
			
		return result;
	}

	@Override
	public double getFxQty(String fromSymbol, String toSymbol, double quantity) throws FxException {
		double rate = 0.0;
		if(PriceUtils.GreaterThan(quantity, 0)) {
			rate = getFxRate(fromSymbol, toSymbol);
		} else {
			rate = 1/getFxRate(toSymbol, fromSymbol);
		}
		
		return rate * quantity;
	}

	public void updateRate(String fromSymbol, String toSymbol, double bid, double ask) {
		fxRates.put(fromSymbol + toSymbol, bid);
		fxRates.put(toSymbol + fromSymbol, 1/ask);
	}

	public void updateRate(Quote quote) {
		String fromSymbol = FxUtils.getFromCurrency(quote.getSymbol());
		String toSymbol = FxUtils.getToCurrency(quote.getSymbol());
		updateRate(fromSymbol, toSymbol, quote.getBid(), quote.getAsk());
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[");
		boolean first = true;
		for(Entry<String, Double> entry: fxRates.entrySet()) {
			if(first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(entry.getKey() + ":" + entry.getValue());
		}
		result.append("]");
		return result.toString();
	}
}
