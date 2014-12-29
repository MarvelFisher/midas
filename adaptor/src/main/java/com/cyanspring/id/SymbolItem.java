package com.cyanspring.id;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.StringUtil;

public class SymbolItem implements AutoCloseable {

	private static final Logger log = LoggerFactory
			.getLogger(IdMarketDataAdaptor.class);

	@Autowired
	public static IdMarketDataAdaptor instance;
	String symbol;
	int dp;

	ArrayList<String> vecLast = new ArrayList<String>();
	long priceLastKey;
	double priceLast;

	long priceKey;
	double open;
	double high;
	double low;
	double price;
	double preclose;
	double close;
	ArrayList<Long> vecTickPrice = new ArrayList<Long>();

	TickItem tick = new TickItem();

	int status;

	/**
	 * 
	 * @return
	 */
	public boolean isWatchID() {
		return symbol.equals("USDJPY");
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return symbol;
	}

	/**
	 * 
	 * @param nDP
	 * @return
	 */
	static long getBase(int nDP) {
		final long arrBase[] = { 1, 10, 100, 1000, 10000, 100000, 1000000 };
		if (nDP >= 0 && nDP <= 6)
			return arrBase[nDP];

		long lBase = arrBase[6];
		for (int i = 6; i < nDP; i++)
			lBase *= 10;
		return lBase;
	}

	/**
	 * 
	 * @param dPrice
	 * @param nDP
	 * @return
	 */
	long getPriceKey(Double dPrice, int nDP) {
		long lBase = getBase(nDP);
		long lPrice = (long) (dPrice * lBase + 0.5);
		dPrice = (double) lPrice / lBase;
		return lPrice;
	}

	/**
	 * SymbolItem
	 * @param strID symbol ID e.g. USDJPY
	 */
	SymbolItem(String strID) {
		clear(false);
		symbol = strID;
	}

	/**
	 * SymbolItem
	 */
	SymbolItem() {
		clear(false);
		symbol = "";
	}

	/**
	 * 
	 */
	public void sunrise() {
		clear(true);
	}

	/**
	 * 
	 */
	void uninit() {
		clear(false);
		vecTickPrice.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	/**
	 * 
	 * @param bSunrise
	 */
	void clear(boolean bSunrise) {

		if (bSunrise) {
			if (0 == close) {
				return;
			}

			preclose = close;
			close = 0;
			if (bSunrise) {
				log.info(String.format("%s Sunrise Preclose=%s", symbol, StringUtil.formatDouble(dp, preclose)));
			}
		} else {
			close = preclose = 0.0;
		}
		status = 0;
		priceKey = 0;
		open = high = low = price = priceLast = 0.0;
		tick.clear();
	}

	/**
	 * 
	 */
	public void doSendPrecloseJob() {
		// Preclose
	}

	/**
	 * 
	 */
	public void doRefreshJob() {
		String strDoubleformat = String.format("%%.%df", dp);
		log.info(String.format("Refresh [%s ] High=%s Low= %s Preclose=%s",
				symbol, StringUtil.formatDouble(dp, high),
				String.format(strDoubleformat, low),
				String.format(strDoubleformat, preclose)));

		ArrayList<String> vecTokens = new ArrayList<String>();

		// Preclose
		vecTokens.add(String.valueOf(preclose));

		// High
		vecTokens.add(String.valueOf(high));

		// Low
		vecTokens.add(String.valueOf(low));

	}

	/**
	 * 
	 * @param dPrice
	 * @param dBidAskPrice
	 * @return
	 */
	public static boolean checkPrice(double dPrice, double dBidAskPrice) {

		double dLimit = 10;
		double dDiff = Math.abs(dBidAskPrice - dPrice);
		double dPercent = dDiff * 100 / dPrice;

		return dPercent < dLimit;
	}

	/**
	 * 
	 * @param timeRev
	 * @param timeTick
	 * @param nDP
	 * @param table
	 */
	public void parseTick(Date timeRev, Date timeTick, int nDP,
			Hashtable<Integer, String> table) {
		
		Date timeGmt = DateUtil.toGmt(timeTick);
		
		dp = nDP;
		boolean bTick = false;
		tick.reset();
		if (table.containsKey(FieldID.AskPrice)
				&& table.containsKey(FieldID.BidPrice)) {
			bTick = true;
			tick.setValue(FieldID.LastTradeTime, timeGmt);
		}

		Set<Integer> set = table.keySet();
		Iterator<Integer> itr = set.iterator();
		while (itr.hasNext()) {
			int nField = itr.next();
			String strValue = table.get(nField);
			switch (nField) {
			case FieldID.AskPrice:
			case FieldID.BidPrice: {
				double dValue = Double.parseDouble(strValue);
				tick.setValue(nField, dValue);
			}
				break;			
			default:
				break;
			}
		}

		double dPrice = 0;
		//long lPrice = 0;
		if (bTick) {
			dPrice = (tick.ask + tick.bid) / 2;
			//lPrice = getPriceKey(dPrice, dp);
			if (0.0 == dPrice || false == checkPrice(dPrice, tick.ask)) {
				bTick = false;
			} else {
				tick.setValue(FieldID.CurrentPrice, dPrice);
			}
		}

		if (!bTick) {
			tick.reset();
			return;
		}
		if (0.0 == open)
		{
			open = dPrice;
		}

		if (0.0 == low || low > dPrice)
		{
			low = dPrice;
		}

		if (0.0 == high || high < dPrice)
		{
			high = dPrice;
		}
		Quote quote = getQuote();		
		IdMarketDataAdaptor.instance.sendQuote(quote);
	}

	/**
	 * 
	 * @return Quote
	 */
	Quote getQuote() {
		Quote quote = new Quote(symbol, null, null);
		quote.setBid(tick.bid);
		quote.setAsk(tick.ask);
		quote.setTimeStamp(tick.time);
		if (open != 0)
			quote.setOpen(open);
		if (high != 0)
			quote.setHigh(high);
		if (low != 0)
			quote.setLow(low);
		if (preclose != 0)
			quote.setClose(preclose);
		return quote;
	}
	
	/**
	 * 
	 */
	public void setClose() {
		close = price;
	}

	/**
	 * 
	 * @param strLine
	 * @return
	 */
	boolean loadFromFile(String strLine) {
		clear(false);

		String[] vec = StringUtil.split(strLine, (char) 0x06);
		if (vec.length != 4)
			return false;

		try {
			this.symbol = vec[0];
			// DP
			dp = Integer.parseInt(vec[1]);
			// Header
			String[] vec1 = StringUtil.split(vec[2], '|');
			for (int i = 0; i < vec1.length; i++) {
				String[] vecTokens = StringUtil.split(vec1[i], '=');
				if (vecTokens.length != 2)
					return false;

				switch (vecTokens[0]) {
				case "Open": {
					open = Double.parseDouble(vecTokens[1]);
				}
					break;
				case "High": {
					high = Double.parseDouble(vecTokens[1]);
				}
					break;
				case "Low": {
					low = Double.parseDouble(vecTokens[1]);
				}
					break;
				case "Preclose": {
					preclose = Double.parseDouble(vecTokens[1]);
				}
					break;
				case "Price": {
					price = Double.parseDouble(vecTokens[1]);
				}
					break;
				case "Close": {
					close = Double.parseDouble(vecTokens[1]);
				}
					break;
				default:
					break;
				}
			}

			// Tick
			tick.loadFromFile(vec[3]);

			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * 
	 */
	public String toString() {

		String strValue;

		FixStringBuilder header = new FixStringBuilder('=', '|');
		header.append("Open");
		header.append(open, dp);
		header.append("High");
		header.append(high, dp);
		header.append("Low");
		header.append(low, dp);
		header.append("Preclose");
		header.append(preclose, dp);
		header.append("Price");
		header.append(price, dp);
		header.append("Close");
		header.append(close, dp);
		char chSep = 0x06;
		strValue = String.format("%s%c%d%c%s%c%s%c%c", symbol, chSep, dp,
				chSep, header.toString(), chSep, tick.toString(dp), 0x0d, 0x0a);
		return strValue;
	}
}
