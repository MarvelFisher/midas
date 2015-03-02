package com.cyanspring.info;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.info.PriceHighLowEvent;
import com.cyanspring.common.event.info.PriceHighLowType;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.PriceHighLow;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.PriceUtils;

public class SymbolData implements Comparable<SymbolData>
{
	private static final Logger log = LoggerFactory
			.getLogger(SymbolData.class);
	private CentralDbProcessor centralDB = null;
	private boolean isUpdating = false;
	private boolean writeMin = false;
	private String strSymbol = null;
	private String market = null;
	private double d52WHigh = 0;
	private double d52WLow = 0; 
	private double dCurPrice = 0;
	private double dCurHigh = 0;
	private double dCurLow = 0;
	private double dOpen = 0;
	private double dClose = 0;
	private double dCurVolume = 0;
	private ArrayList<HistoricalPrice> priceData = new ArrayList<HistoricalPrice>() ;
	private ArrayList<Quote> quoteTmp = new ArrayList<Quote>() ;
	
	public SymbolData(String strSymbol, String market, CentralDbProcessor centralDB)
	{
		log.info("Create SymbolData " + strSymbol + " Market=" + market + " TradeDate=" + centralDB.getTradedate());
		this.setStrSymbol(strSymbol) ;
		this.centralDB = centralDB ;
		this.market = market;
		readFromTick() ;
		get52WHighLow() ;
	}
	public SymbolData(String symbol) {
		this.setStrSymbol(symbol) ;
	}
	public void resetPriceData()
	{
		int tickCount = centralDB.getTickCount() ;
		priceData.clear();
		for (int ii = 0; ii < tickCount; ii++)
		{
			priceData.add(new HistoricalPrice(this.getStrSymbol(), false)) ;
		}
	}
	public void setPrice(Quote quote)
	{
		if (isUpdating)
		{
			quoteTmp.add(quote) ;
		}
		else
		{
			if (!quoteTmp.isEmpty())
			{
				for (Quote q : quoteTmp)
				{
					setPrice(q.getBid(), q.getAsk(), q.getTimeStamp()) ;
				}
				quoteTmp.clear();
			}
			setPrice(quote.getBid(), quote.getAsk(), quote.getTimeStamp()) ;
			dCurVolume = quote.getTotalVolume() ;
		}
	}
	public boolean setPrice(double bid, double ask, Date date)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.setTime(date) ;
		int curTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) ;
		int nPos = centralDB.getPosByTime(curTime) ;
		double dPrice = (bid + ask) / 2 ;
		HistoricalPrice price = priceData.get(nPos) ;
		boolean changed = price.setPrice(dPrice);
		if (changed && writeMin) writeToMin() ;
		price.setTimestamp(date) ;
		if (d52WHigh < dPrice)
		{
			d52WHigh = dPrice ;
		}
		if (PriceUtils.isZero(d52WLow) || d52WLow > dPrice)
		{
			d52WLow = dPrice ;
		}
		if (dCurHigh < dPrice)
		{
			dCurHigh = dPrice ;
		}
		if (PriceUtils.isZero(dCurLow) || dCurLow > dPrice)
		{
			dCurLow = dPrice ;
		}
		if (PriceUtils.isZero(dOpen))
		{
			dOpen = dPrice ;
		}
		dCurPrice = dPrice ;
		dClose = dPrice ;
		return changed;
	}
	
	public void readFromTick()
	{
		resetPriceData();
		isUpdating = true ;
		String tradedate = centralDB.getTradedate();
		if (tradedate == null)
		{
			isUpdating = false ;
			return;
		}
		String strFile = String.format("./DAT/%s/%s.1M", 
				centralDB.getTradedate(), getStrSymbol()) ;
		File file = new File(strFile);
		if (file.exists() == false)
		{
			isUpdating = false ;
			return;
		}
	    try
        {
	        FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try
            {
                Object o = ois.readObject();
                priceData = (ArrayList<HistoricalPrice>)o;
            }
            catch (EOFException e)
            {
            }
            catch (Exception e)
            {
            	log.error(e.getMessage(), e);
            }
            ois.close();
            fis.close();
			isUpdating = false ;
        }
        catch (Exception e)
        {
        	log.error(e.getMessage(), e);
			isUpdating = false ;
        }
	}
	
	public void writeToMin()
	{
		writeMin = false;
		if (centralDB.getTradedate() == null)
		{
			return;
		}
		String strFile = String.format("./DAT/%s/%s.1M", 
				centralDB.getTradedate(), getStrSymbol()) ;
		File file = new File(strFile) ;
        file.getParentFile().mkdirs();
        boolean fileExist = file.exists();
        try
        {
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos); 
        	oos.writeObject(priceData);
            oos.flush();
            oos.close();
            fos.close();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
	}
	
	public void insertSQLDate(String strType)
	{
    	if (market == null)
    	{
    		return;
    	}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;
		String strDateTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00") ;
		strDateTime = sdf.format(cal.getTime()) ;
		HistoricalPrice lastPrice;
		if (strType.equals("W") || strType.equals("M"))
		{
			lastPrice = centralDB.dbhnd.getLastValue(market, strType, getStrSymbol(), false) ;
			Calendar cal_ = Calendar.getInstance() ;
			if (lastPrice != null)
			{
				cal_.setTime(lastPrice.getTimestamp());
			}
			boolean bDelete = false ;
			if (strType.equals("W"))
			{
				bDelete = cal_.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) ;
			}
			else
			{
				bDelete = cal_.get(Calendar.MONTH) == cal.get(Calendar.MONTH) ;
			}
			if (bDelete)
			{
				centralDB.dbhnd.deletePrice(market, strType, getStrSymbol(), lastPrice);
			}
			lastPrice.setTimestamp(cal.getTime());
			if (!bDelete)
			{
				if (dOpen != 0) 
				{
					lastPrice.setOpen(dOpen);
				}
				lastPrice.setVolume((int)dCurVolume);
			}
			else
			{
				lastPrice.setVolume(lastPrice.getVolume() + (int)dCurVolume);
			}
			if (lastPrice.getHigh() < dCurHigh && dCurHigh != 0)
			{
				lastPrice.setHigh(dCurHigh);
			}
			if (lastPrice.getLow() > dCurLow && dCurLow != 0)
			{
				lastPrice.setLow(dCurLow);
			}
			if (dClose != 0) 
			{
				lastPrice.setClose(dClose);
			}
		}
		else
		{
			lastPrice = new HistoricalPrice(cal.getTime(),
											strSymbol,
											dOpen,
											dCurHigh,
											dCurLow,
											dClose,
											(int)dCurVolume);
		}
		sqlcmd = String.format(
				"insert into %s (TRADEDATE,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME) ", strTable) ;
		sqlcmd += String.format(
                "values ('%s','%s',%.5f,%.5f,%.5f,%.5f,%d) ON DUPLICATE KEY ",
                strDateTime, getStrSymbol(), lastPrice.getOpen(), 
                lastPrice.getClose(), lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume()) ;
		sqlcmd += String.format(
				"Update OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d;",
				lastPrice.getOpen(), lastPrice.getClose(), lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume()) ;
		centralDB.dbhnd.updateSQL(sqlcmd);
	}
	
	public PriceHighLow getPriceHighLow(PriceHighLowType type)
	{
		PriceHighLow phl = new PriceHighLow() ;
		if (type == PriceHighLowType.DAY)
		{
			phl.setHigh(this.dCurHigh);
			phl.setLow(this.dCurLow);
			phl.setSymbol(this.getStrSymbol());
			phl.setType(type) ;
		}
		else if (type == PriceHighLowType.W52)
		{
			phl.setHigh(this.d52WHigh);
			phl.setLow(this.d52WLow);
			phl.setSymbol(this.getStrSymbol());
			phl.setType(type) ;
		}
		return phl ;
	}
	
	public void get52WHighLow()
	{
    	if (market == null)
    	{
    		return;
    	}
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String sqlcmd = String.format("SELECT * FROM %s_W WHERE SYMBOL='%s' ORDER BY TRADEDATE desc LIMIT 52;", 
				prefix, getStrSymbol()) ;
		ResultSet rs = centralDB.dbhnd.querySQL(sqlcmd) ;
		try {
			double dHigh = 0 ; 
			double dLow = 0 ;
			while(rs.next())
			{
				dHigh = rs.getDouble("HIGH_PRICE") ;
				dLow = rs.getDouble("LOW_PRICE") ;
				if (this.d52WHigh < dHigh)
				{
					this.d52WHigh = dHigh ;
				}
				if (PriceUtils.isZero(this.d52WLow) || this.d52WLow > dLow)
				{
					this.d52WLow = dLow ;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ;
	}
	
	public ArrayList<HistoricalPrice> getPriceList(String strType, 
												   Date end, 
												   boolean fill,
												   ArrayList<HistoricalPrice> prices) throws ParseException
	{
		HistoricalPrice priceEmpty = null ;
		if (prices.isEmpty())
		{
			priceEmpty = centralDB.dbhnd.getLastValue(market, strType, getStrSymbol(), false) ;
			if (priceEmpty != null)
			{
				prices.add(priceEmpty) ;
			}
		}
		else
		{
			priceEmpty = prices.get(prices.size()-1);
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar firsttime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar pricetime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar emptytime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		firsttime.setTime(sdf.parse(centralDB.getTradedate()));
		int open = centralDB.getOpen() ;
		int close = centralDB.getClose() ;
		firsttime.set(Calendar.HOUR_OF_DAY, open/100);
		firsttime.set(Calendar.MINUTE, open%100) ;
		int priceMin = 0;
		int emptyMin = 0;
		if (open > close)
		{
			firsttime.add(Calendar.DATE, -1);
		}
		if (strType.equals("1"))
		{
			for( HistoricalPrice price : priceData)
			{
				if (price.getTimestamp() == null)
				{
//					if (fill == true)
//					{
//						price.copy((HistoricalPrice)priceEmpty.clone());
//						price.setTimestamp(firsttime.getTime());
//					}
					continue;
				}
				else if (end != null && 0 < price.getTimestamp().compareTo(end))
				{
					return prices ;
				}
				else
				{
					priceEmpty = price ;
				}
				firsttime.add(Calendar.MINUTE, 1) ;
				if (price.getTimestamp() != null)
				{
					prices.add(price) ;
				}
			}
		}
		else
		{
			for( HistoricalPrice price : priceData)
			{
				if (price.getTimestamp() == null)
				{
					continue ;
				}
				if (end != null && 0 < price.getTimestamp().compareTo(end))
				{
					return prices ;
				}
				pricetime.setTime(price.getTimestamp());
				emptytime.setTime(priceEmpty.getTimestamp());
				priceMin = pricetime.get(Calendar.HOUR_OF_DAY)*60 + pricetime.get(Calendar.MINUTE);
				emptyMin = emptytime.get(Calendar.HOUR_OF_DAY)*60 + emptytime.get(Calendar.MINUTE);
				if (strType.equals("R") 
						&& (priceMin/5 != emptyMin/5))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 5) * 5)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("A") 
						&& (priceMin/10 != emptyMin/10))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 10) * 10)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("Q") 
						&& (priceMin/15 != emptyMin/15))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 15) * 15)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("H") 
						&& (priceMin/30 != emptyMin/30))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 30) * 30)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("6") 
						&& ((pricetime.get(Calendar.HOUR_OF_DAY) != emptytime.get(Calendar.HOUR_OF_DAY))
								|| (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE))))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.HOUR_OF_DAY, pricetime.get(Calendar.HOUR_OF_DAY)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("T") 
						&& ((pricetime.get(Calendar.HOUR_OF_DAY)/4 != emptytime.get(Calendar.HOUR_OF_DAY)/4)
								|| (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE))))
				{
					priceEmpty = (HistoricalPrice)price.clone() ;
					prices.add(priceEmpty) ;
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.HOUR_OF_DAY, (pricetime.get(Calendar.HOUR_OF_DAY) / 4) * 4) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else
				{
					priceEmpty.update(price);
				}
			}
		}
		return prices ;
	}
	
	public void getPriceDate(String strType, ArrayList<HistoricalPrice> pricelist)
	{
		if (PriceUtils.isZero(dOpen))
		{
			return;
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date date = cal.getTime() ;
		int day = cal.get(Calendar.DATE); 
		int week = cal.get(Calendar.WEEK_OF_YEAR) ;
		int month = cal.get(Calendar.MONTH) ;
		HistoricalPrice curPrice = new HistoricalPrice(date, getStrSymbol(), dOpen, dCurHigh, dCurLow, dClose, (int)dCurVolume) ;
		HistoricalPrice lastPrice = null ;
		if (pricelist.size() > 1)
		{
			lastPrice = pricelist.get(pricelist.size()-1) ;
			cal.setTime(lastPrice.getTimestamp());
		}
		if (strType.equals("D") && lastPrice != null)
		{
			if (day == cal.get(Calendar.DATE))
			{
				if (lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (lastPrice.getLow() < dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				lastPrice.setClose(dCurPrice);
				lastPrice.setTimestamp(date);
			}
			else
			{
				pricelist.add(curPrice) ;
			}
		}
		else if (strType.equals("W") && lastPrice != null)
		{
			if (week == cal.get(Calendar.WEEK_OF_YEAR))
			{
				if (lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (lastPrice.getLow() < dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				lastPrice.setClose(dCurPrice);
				lastPrice.setTimestamp(date);
			}
			else
			{
				pricelist.add(curPrice) ;
			}
		}
		else if (strType.equals("M") && lastPrice != null)
		{
			if (month == cal.get(Calendar.MONTH))
			{
				if (lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (lastPrice.getLow() < dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				lastPrice.setClose(dCurPrice);
				lastPrice.setTimestamp(date);
			}
			else
			{
				pricelist.add(curPrice) ;
			}
		}
		
	}
	
	public void insertSQLTick(String strType)
	{
    	if (market == null)
    	{
    		return;
    	}
		ArrayList<HistoricalPrice> prices = new ArrayList<HistoricalPrice>() ;
		try {
			prices = getPriceList(strType, null, true, prices);
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
			return ;
		}
		if (prices.isEmpty())
		{
			return ;
		}

    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;//"START TRANSACTION;" ;
		String strDateTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		centralDB.dbhnd.createStatement();
		for (HistoricalPrice price : prices)
		{
			if (price.getTimestamp() == null)
			{
				continue;
			}
			strDateTime = sdf.format(price.getTimestamp()) ;
			sqlcmd = String.format(
					"insert into %s (TRADEDATE,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME) " + 
	                "values ('%s','%s',%.5f,%.5f,%.5f,%.5f,%d) ON DUPLICATE KEY " + 
					"Update OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d;",
					strTable, strDateTime, price.getSymbol(), price.getOpen(),
					price.getClose(), price.getHigh(), price.getLow(), (int)price.getVolume(),
					price.getOpen(), price.getClose(), price.getHigh(), price.getLow(), (int)price.getVolume()) ;
			centralDB.dbhnd.addBatch(sqlcmd);
		}
		centralDB.dbhnd.executeBatch();
		return ;
	}
	
	public List<HistoricalPrice> getHistoricalPrice(String type, String symbol, Date start, Date end)
	{
    	if (market == null)
    	{
    		return null;
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		SimpleDateFormat sdfprice = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		sdfprice.setTimeZone(TimeZone.getTimeZone("GMT"));
		String tradedate = centralDB.getTradedate() ;
		String enddate = sdf.format(end) ;
		String strtmp ;
		ArrayList<HistoricalPrice> listPrice = new ArrayList<HistoricalPrice>() ;
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String sqlcmd = String.format("SELECT * FROM %s_%s WHERE `SYMBOL`='%s' AND `TRADEDATE`>='%s' AND `TRADEDATE`<'%s' ORDER BY `TRADEDATE`;", 
				prefix, type, symbol, sdfprice.format(start), sdfprice.format(end)) ;
		ResultSet rs = centralDB.dbhnd.querySQL(sqlcmd) ;
		try {
			while(rs.next())
			{
				HistoricalPrice price = new HistoricalPrice(); 
				price.setTimestamp(sdfprice.parse(rs.getString("TRADEDATE")));
				price.setSymbol(rs.getString("SYMBOL"));
				price.setOpen(rs.getDouble("OPEN_PRICE"));
				price.setClose(rs.getDouble("CLOSE_PRICE"));
				price.setHigh(rs.getDouble("HIGH_PRICE"));
				price.setLow(rs.getDouble("LOW_PRICE"));
				strtmp = rs.getString("VOLUME") ;
				if (strtmp != null && !strtmp.toLowerCase().equals("null"))
				{
					price.setVolume(Integer.parseInt(strtmp));
				}
				listPrice.add(price) ;
			}
		} catch (SQLException | ParseException e) {
			log.error(e.getMessage(), e);
			return null ;
		}
		if (0 <= tradedate.compareTo(enddate) && centralDB.getSessionType() == MarketSessionType.OPEN)
		{
			try {
				switch(type)
				{
				case "1":
				case "R":
				case "A":
				case "Q":
				case "H":
				case "6":
				case "T":
				{
					getPriceList(type, end, false, listPrice) ;
					break ;
				}
				case "D":
				case "W":
				case "M":
				{
					getPriceDate(type, listPrice) ;
					break;
				}
				default:
					return null ;
				}
			} catch (ParseException e) {
				log.error(e.getMessage(), e);
				return null ;
			}
		}
		return listPrice ;
	}
	@Override
	public int compareTo(SymbolData o) {
		// TODO Auto-generated method stub
		return this.getStrSymbol().compareTo(o.getStrSymbol());
	}
	public String getStrSymbol() {
		return strSymbol;
	}
	public void setStrSymbol(String strSymbol) {
		this.strSymbol = strSymbol;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public boolean isWriteMin() {
		return writeMin;
	}
	public void setWriteMin(boolean writeMin) {
		this.writeMin = writeMin;
	}
	
}
