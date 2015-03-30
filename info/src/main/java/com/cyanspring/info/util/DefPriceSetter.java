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
		double dPrice = quote.getLast();
		boolean pricechanged = price.setPrice(dPrice);
		price.setDatatime(quote.getTimeStamp()) ;
//		if (PriceUtils.Equal(quote.getTotalVolume(), LastVolume))
//		{
			price.setVolume((int) (price.getVolume() + quote.getLastVol()));
//		}
		return pricechanged;
	}

	@Override
	public boolean setDataPrice(SymbolData symboldata, Quote quote) 
	{
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
		if (!PriceUtils.Equal(symboldata.getdClose(), dPrice))
		{
			symboldata.setdClose(dPrice);
		}
		return true;
	}
}
