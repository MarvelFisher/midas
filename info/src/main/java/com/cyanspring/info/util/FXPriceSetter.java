package com.cyanspring.info.util;

import java.util.Calendar;
import java.util.TimeZone;

import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.info.SymbolData;

public class FXPriceSetter implements IPriceSetter
{
	@Override
	public boolean setPrice(HistoricalPrice price, Quote quote, double LastVolume) 
	{
		if (PriceUtils.EqualLessThan(quote.getBid(), 0) || PriceUtils.EqualLessThan(quote.getAsk(), 0))
		{
			return false;
		}
		double dPrice = (quote.getBid() + quote.getAsk()) / 2;
		boolean changed = price.setPrice(dPrice);
		price.setDatatime(quote.getTimeStamp()) ;
		return changed;
	}

	@Override
	public boolean setDataPrice(SymbolData symboldata, Quote quote) 
	{
		if (quote.getBid() < 0 || quote.getAsk() < 0)
		{
			return false;
		}
		double dPrice = (quote.getBid() + quote.getAsk()) / 2;
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
		return true;
	}
}
