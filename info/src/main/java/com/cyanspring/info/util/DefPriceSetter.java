package com.cyanspring.info.util;

import java.util.Calendar;
import java.util.TimeZone;

import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.info.cdp.SymbolData;

public class DefPriceSetter implements IPriceSetter
{
	@Override
	public boolean setPrice(HistoricalPrice price, Quote quote, double LastVolume, String commodity) 
	{
		if (PriceUtils.EqualLessThan(quote.getLast(), 0)
				|| (commodity.equals("I") == false && PriceUtils.EqualLessThan(quote.getLastVol(), 0)))
		{
			return false;
		}
		double dPrice = quote.getLast();
		boolean pricechanged = price.setPrice(dPrice);
		price.setDatatime(quote.getTimeStamp()) ;
		if (PriceUtils.Equal(quote.getTotalVolume(), LastVolume) == false)
		{
			double lastVolume = quote.getTotalVolume() - LastVolume;
			if (PriceUtils.GreaterThan(lastVolume, 0))
				price.setVolume((long) (price.getVolume() + lastVolume));
		}
		price.setTotalVolume(quote.getTotalVolume());
		price.setTurnover(quote.getTurnover());
		return pricechanged;
	}

	@Override
	public boolean setDataPrice(SymbolData symboldata, Quote quote) 
	{
		if (PriceUtils.EqualLessThan(quote.getLast(), 0)
				|| PriceUtils.EqualLessThan(quote.getLastVol(), 0))
		{
			return false;
		}
		double dPrice = quote.getLast();
		if (PriceUtils.LessThan(symboldata.getD52WHigh(), dPrice))
		{
			symboldata.setD52WHigh(dPrice) ;
		}
		if (PriceUtils.isZero(symboldata.getD52WLow()) 
				|| PriceUtils.GreaterThan(symboldata.getD52WLow(), dPrice))
		{
			symboldata.setD52WLow(dPrice);
		}
		if (PriceUtils.LessThan(symboldata.getdCurHigh(), dPrice))
		{
			symboldata.setdCurHigh(dPrice);
		}
		if (PriceUtils.isZero(symboldata.getdCurLow()) 
				|| PriceUtils.GreaterThan(symboldata.getdCurLow(), dPrice))
		{
			symboldata.setdCurLow(dPrice);
		}
		if (PriceUtils.isZero(symboldata.getdOpen()))
		{
			symboldata.setdOpen(dPrice);
		}
//		if(quote.getOpen() > 0 && PriceUtils.Equal(symboldata.getdOpen(),quote.getOpen()) == false) 
//		{
//			symboldata.setdOpen(quote.getOpen());
//		} // Ib & Id may have different first price, use first recieved price as open
		if (!PriceUtils.Equal(symboldata.getdClose(), dPrice))
		{
			symboldata.setdClose(dPrice);
		}
		symboldata.setdCurTotalVolume(quote.getTotalVolume());
		symboldata.setdCurTurnover(quote.getTurnover());
		return true;
	}
}
