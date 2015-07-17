package com.cyanspring.info.util;

import java.util.Calendar;
import java.util.TimeZone;

import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.info.SymbolData;

public class DefPriceSetter implements IPriceSetter
{
	@Override
	public boolean setPrice(HistoricalPrice price, Quote quote, double LastVolume) 
	{
		if (PriceUtils.EqualLessThan(quote.getLast(), 0))
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
		if (quote.getLast() < 0)
		{
			return false;
		}
		double dPrice = quote.getLast();
		if (PriceUtils.isZero(dPrice))
		{
			return false;
		}
		if (symboldata.getD52WHigh() < dPrice)
		{
			symboldata.setD52WHigh(dPrice) ;
		}
		if (PriceUtils.isZero(symboldata.getD52WLow()) || symboldata.getD52WLow() > dPrice)
		{
			symboldata.setD52WLow(dPrice);
		}
		if (symboldata.getdCurHigh() < dPrice)
		{
			symboldata.setdCurHigh(dPrice);
		}
		if (PriceUtils.isZero(symboldata.getdCurLow()) || symboldata.getdCurLow() > dPrice)
		{
			symboldata.setdCurLow(dPrice);
		}
		if (PriceUtils.isZero(symboldata.getdOpen()))
		{
			symboldata.setdOpen(dPrice);
		}
		if(quote.getOpen() > 0 && PriceUtils.Equal(symboldata.getdOpen(),quote.getOpen()) == false) 
		{
			symboldata.setdOpen(quote.getOpen());
		}
		if (!PriceUtils.Equal(symboldata.getdClose(), dPrice))
		{
			symboldata.setdClose(dPrice);
		}
		symboldata.setdCurTotalVolume(quote.getTotalVolume());
		symboldata.setdCurTurnover(quote.getTurnover());
		return true;
	}
}
