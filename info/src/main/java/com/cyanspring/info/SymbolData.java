package com.cyanspring.info;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.info.PriceHighLowType;
import com.cyanspring.common.info.IRefSymbolInfo;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.PriceHighLow;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.info.util.IPriceSetter;

public class SymbolData implements Comparable<SymbolData>
{
	private static final Logger log = LoggerFactory
			.getLogger(SymbolData.class);
	private static final String insertPrice = "insert into %s (TRADEDATE,KEYTIME,DATATIME,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME,TOTALVOLUME,TURNOVER) " + 
            "values ('%s','%s','%s','%s',%.5f,%.5f,%.5f,%.5f,%d,%.0f,%.5f) ON DUPLICATE KEY " + 
            "Update TRADEDATE='%s',DATATIME='%s',OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d,TOTALVOLUME=%.0f,TURNOVER=%.5f;";
	private static final String DateFormat = "yyyy-MM-dd";
	private static final String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
	
	private static IPriceSetter setter;
	private CentralDbProcessor centralDB = null;
	private boolean isUpdating = false;
	private boolean writeMin = false;
	private String strSymbol = null;
	private String market = null;
	private double d52WHigh = 0;
	private double d52WLow = 0; 
	private double dCurHigh = 0;
	private double dCurLow = 0;
	private double dOpen = 0;
	private double dClose = 0;
	private double dCurVolume = 0;
	private TreeMap<Date, HistoricalPrice> priceData = new TreeMap<Date, HistoricalPrice>() ;
	private LinkedBlockingQueue<Quote> quoteTmp = new LinkedBlockingQueue<Quote>() ;
	
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
//		int tickCount = centralDB.getTickCount() ;
		synchronized(priceData)
		{
			priceData.clear();
		}
	}
	public void parseQuote(Quote quote)
	{
		try 
		{
			quoteTmp.put(quote) ;
		} 
		catch (InterruptedException e) 
		{
			log.error(e.toString(), e);
		}
		if (isUpdating)
		{
			return;
		}
		else
		{
			Quote q;
			while ((q = quoteTmp.poll()) != null)
			{
				setPrice(q) ;
				dCurVolume = q.getTotalVolume() ;
			}
		}
	}
	public boolean setPrice(Quote quote)
	{
		boolean changed;
		synchronized(priceData)
		{
			cal.setTime(quote.getTimeStamp()) ;
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			HistoricalPrice price = priceData.get(cal.getTime()) ;
			if (price == null)
			{
				price = new HistoricalPrice(strSymbol, centralDB.getTradedate(), cal.getTime());
				changed = setter.setPrice(price, quote, dCurVolume);
				if (changed)
					priceData.put(cal.getTime(), price);
			}
			else
			{
				changed = setter.setPrice(price, quote, dCurVolume);
			}
		}
		if (changed && writeMin) //writeToMin() ; 
		{
			centralDB.getChartCacheProcessor().put(this);
			writeMin = false;
		}
		setter.setDataPrice(this, quote);
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
		String strFile = String.format("./DAT/%s/%s", 
				centralDB.getTradedate(), getStrSymbol()) ;
		File fileMins = new File(strFile + ".1M");
		File fileCache = new File(strFile + ".Ch");
		if (fileMins.exists() == false)
		{
			isUpdating = false ;
			return;
		}
	    try
        {
	        FileInputStream fis = new FileInputStream(fileMins);
            FSTObjectInput in = new FSTObjectInput(fis);
            priceData = (TreeMap<Date, HistoricalPrice>) in.readObject(TreeMap.class);
            in.close();
            fis.close();
			isUpdating = false ;
			
			if (fileCache.exists())
			{
				Scanner data = new Scanner(fileCache);
				data.useDelimiter(";");
				String str = data.next();
				dOpen = Double.parseDouble(str);
				str = data.next();
				dCurHigh = Double.parseDouble(str);
				str = data.next();
				dCurLow = Double.parseDouble(str);
				str = data.next();
				dClose = Double.parseDouble(str);
				str = data.next();
				d52WHigh = Double.parseDouble(str);
				str = data.next();
				d52WLow = Double.parseDouble(str);
				str = data.next();
				dCurVolume = (double)Integer.parseInt(str);
				data.close();
			}
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
		String strFile = String.format("./DAT/%s/%s", 
				centralDB.getTradedate(), getStrSymbol()) ;
		File fileMins = new File(strFile + ".1M") ;
        fileMins.getParentFile().mkdirs();
        try
        {
        	FileWriter fwrite = new FileWriter(strFile + ".Ch");
        	fwrite.write(String.format("%f;%f;%f;%f;%f;%f;%d", dOpen, dCurHigh, dCurLow, dClose, d52WHigh, d52WLow, (int)dCurVolume));
        	fwrite.flush();
        	fwrite.close();
        	synchronized(priceData)
    		{
	            FileOutputStream fos = new FileOutputStream(fileMins, false);
	            FSTObjectOutput out = new FSTObjectOutput(fos);
	            out.writeObject( priceData, TreeMap.class );
	            // DON'T out.close() when using factory method;
	            out.flush();
	            out.close();
	            fos.close();
    		}
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
	}
	
	public void insertSQLDate(String strType)
	{
    	if (market == null || PriceUtils.isZero(dOpen))
    	{
    		return;
    	}
    	log.debug(strSymbol + "Processing type \"" + strType + "\" chart");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date currentDate = cal.getTime();
		if (strType.equals("W"))
		{
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}
		else if (strType.equals("M"))
		{
			cal.set(Calendar.DAY_OF_MONTH, 1);
		}
		Date keyDate = cal.getTime();
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;
		String tradeDate = centralDB.getTradedate() ;
		HistoricalPrice lastPrice;
		IRefSymbolInfo refsymbol = centralDB.getRefSymbolInfo();
		SymbolInfo symbolinfo = refsymbol.get(refsymbol.at(new SymbolInfo(centralDB.getServerMarket(), getStrSymbol())));
		String strSymbol = null;
		if (symbolinfo != null)
		{
			if (symbolinfo.getHint() != null)
			{
				strSymbol = String.format("%s.%s", symbolinfo.getHint(), symbolinfo.getExchange());
			}
		}
		if (strSymbol == null)
		{
			strSymbol = getStrSymbol();
		}
		if (strType.equals("W") || strType.equals("M"))
		{
			lastPrice = centralDB.getDbhnd().getLastValue(market, strType, getStrSymbol(), false) ;
			SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
			Calendar cal_ = Calendar.getInstance() ;
			boolean bDelete = false ;
			if (lastPrice != null && lastPrice.getKeytime() != null)
			{
				cal_.setTime(lastPrice.getKeytime());
				if (strType.equals("W"))
				{
					bDelete = getWeek(sdf.format(cal_.getTime())) == getWeek(sdf.format(cal.getTime())) ;
				}
				else
				{
					bDelete = (cal_.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
								&& cal_.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) ;
				}
			}
			else
			{
				lastPrice = new HistoricalPrice() ;
			}
			lastPrice.setTradedate(tradeDate);
			lastPrice.setKeytime(keyDate);
			lastPrice.setDatatime(currentDate);
			if (!bDelete)
			{
				if (PriceUtils.isZero(getdOpen()) == false) 
				{
					lastPrice.setOpen(getdOpen());
				}
				lastPrice.setHigh(0.0);
				lastPrice.setLow(0.0);
				lastPrice.setClose(0.0);
				lastPrice.setVolume((long)dCurVolume);
			}
			else
			{
				lastPrice.setVolume(lastPrice.getVolume() + (long)dCurVolume);
			}
			if (PriceUtils.isZero(lastPrice.getHigh()) 
					|| (lastPrice.getHigh() < getdCurHigh() && PriceUtils.isZero(getdCurHigh()) == false))
			{
				lastPrice.setHigh(getdCurHigh());
			}
			if (PriceUtils.isZero(lastPrice.getLow()) 
					|| (lastPrice.getLow() > getdCurLow() && PriceUtils.isZero(getdCurLow()) == false))
			{
				lastPrice.setLow(getdCurLow());
			}
			if (PriceUtils.isZero(getdClose()) == false) 
			{
				lastPrice.setClose(getdClose());
			}
		}
		else
		{
			lastPrice = new HistoricalPrice(centralDB.getTradedate(),
											keyDate,
											currentDate,
											getStrSymbol(),
											getdOpen(),
											getdCurHigh(),
											getdCurLow(),
											getdClose(),
											(int)dCurVolume);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormat);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		sqlcmd = String.format(insertPrice, 
				strTable, tradeDate, sdf.format(lastPrice.getKeytime()), sdf.format(lastPrice.getDatatime()), 
				getStrSymbol(), lastPrice.getOpen(), lastPrice.getClose(), 
				lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume(), 
				lastPrice.getTotalVolume(), lastPrice.getTurnover(), 
				tradeDate, sdf.format(lastPrice.getDatatime()), lastPrice.getOpen(), 
				lastPrice.getClose(), lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume(), 
				lastPrice.getTotalVolume(), lastPrice.getTurnover()) ;
		centralDB.getDbhnd().updateSQL(sqlcmd);
		if (strSymbol.equals(getStrSymbol()) == false)
		{
			sqlcmd = String.format(insertPrice, 
					strTable, tradeDate, sdf.format(lastPrice.getKeytime()), sdf.format(lastPrice.getDatatime()), 
					strSymbol, lastPrice.getOpen(), lastPrice.getClose(), 
					lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume(),  
					lastPrice.getTotalVolume(), lastPrice.getTurnover(), 
					tradeDate, sdf.format(lastPrice.getDatatime()), lastPrice.getOpen(), 
					lastPrice.getClose(), lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume(), 
					lastPrice.getTotalVolume(), lastPrice.getTurnover()) ;
			centralDB.getDbhnd().updateSQL(sqlcmd);
		}
		logHistoricalPrice(lastPrice);
	}
	
	public PriceHighLow getPriceHighLow(PriceHighLowType type)
	{
		PriceHighLow phl = new PriceHighLow() ;
		if (type == PriceHighLowType.DAY)
		{
			phl.setHigh(this.getdCurHigh());
			phl.setLow(this.getdCurLow());
			phl.setSymbol(this.getStrSymbol());
			phl.setType(type) ;
		}
		else if (type == PriceHighLowType.W52)
		{
			phl.setHigh(this.getD52WHigh());
			phl.setLow(this.getD52WLow());
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
    	ArrayList<String> symbolarr = new ArrayList<String>();
    	symbolarr.add(strSymbol);
    	ArrayList<SymbolInfo> symbolinfos = (ArrayList<SymbolInfo>) centralDB.getRefSymbolInfo().getBySymbolStrings(symbolarr);
    	if (symbolinfos.size() < 1)
    	{
    		return;
    	}
    	String symbol;
    	if (symbolinfos.get(0).getHint() == null)
    	{
    		symbol = symbolinfos.get(0).getCode(); 
    	}
    	else
    	{
    		symbol = symbolinfos.get(0).getHint() + "." + symbolinfos.get(0).getCode().split("\\.")[1];
    	}
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String sqlcmd = String.format("SELECT * FROM %s_W WHERE SYMBOL='%s' ORDER BY KEYTIME desc LIMIT 52;", 
				prefix, symbol) ;
		ResultSet rs = centralDB.getDbhnd().querySQL(sqlcmd) ;
		try {
			double dHigh = 0 ; 
			double dLow = 0 ;
			while(rs.next())
			{
				dHigh = rs.getDouble("HIGH_PRICE") ;
				dLow = rs.getDouble("LOW_PRICE") ;
				if (this.getD52WHigh() < dHigh)
				{
					this.setD52WHigh(dHigh) ;
				}
				if (PriceUtils.isZero(this.getD52WLow()) || this.getD52WLow() > dLow)
				{
					this.setD52WLow(dLow) ;
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
			priceEmpty = centralDB.getDbhnd().getLastValue(market, strType, getStrSymbol(), false) ;
			if (priceEmpty != null)
			{
				prices.add(priceEmpty) ;
			}
		}
		else
		{
			priceEmpty = prices.get(prices.size()-1);
		}
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar pricetime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar emptytime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		int priceMin = 0;
		int emptyMin = 0;
		HistoricalPrice price;
		synchronized(priceData)
		{
			if (strType.equals("1"))
			{
				for(Map.Entry<Date, HistoricalPrice> entry : priceData.entrySet())
				{
					price = entry.getValue();
					if (price.getDatatime() == null)
					{
						continue;
					}
					else if (end != null && 0 < price.getDatatime().compareTo(end))
					{
						return prices ;
					}
					prices.add(price) ;
				}
			}
			else
			{
				for(Map.Entry<Date, HistoricalPrice> entry : priceData.entrySet())
				{
					price = entry.getValue();
					if (price.getDatatime() == null)
					{
						continue ;
					}
					if (priceEmpty == null)
					{
						priceEmpty = (HistoricalPrice)price.clone();
						continue;
					}
					if (priceEmpty.getDatatime() == null)
					{
						priceEmpty.update(price);
						continue;
					}
					if (end != null && 0 < price.getDatatime().compareTo(end))
					{
						return prices ;
					}
					pricetime.setTime(price.getDatatime());
					emptytime.setTime(priceEmpty.getDatatime());
					priceMin = pricetime.get(Calendar.HOUR_OF_DAY)*60 + pricetime.get(Calendar.MINUTE);
					emptyMin = emptytime.get(Calendar.HOUR_OF_DAY)*60 + emptytime.get(Calendar.MINUTE);
					if (strType.equals("R") 
							&& (priceMin/5 != emptyMin/5))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 5) * 5)) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else if (strType.equals("A") 
							&& (priceMin/10 != emptyMin/10))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 10) * 10)) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else if (strType.equals("Q") 
							&& (priceMin/15 != emptyMin/15))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 15) * 15)) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else if (strType.equals("H") 
							&& (priceMin/30 != emptyMin/30))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 30) * 30)) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else if (strType.equals("6") 
							&& ((pricetime.get(Calendar.HOUR_OF_DAY) != emptytime.get(Calendar.HOUR_OF_DAY))
									|| (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE))))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.HOUR_OF_DAY, pricetime.get(Calendar.HOUR_OF_DAY)) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else if (strType.equals("T") 
							&& ((pricetime.get(Calendar.HOUR_OF_DAY)/4 != emptytime.get(Calendar.HOUR_OF_DAY)/4)
									|| (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE))))
					{
						priceEmpty = (HistoricalPrice)price.clone() ;
						prices.add(priceEmpty) ;
						cal.setTime(price.getDatatime());
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.HOUR_OF_DAY, (pricetime.get(Calendar.HOUR_OF_DAY) / 4) * 4) ;
						priceEmpty.setKeytime(cal.getTime());
					}
					else
					{
						priceEmpty.update(price);
					}
				}
			}
		}
		return prices ;
	}
	
	public void getPriceDate(String strType, ArrayList<HistoricalPrice> pricelist)
	{
		if (PriceUtils.isZero(getdOpen()))
		{
			return;
		}
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar pricetime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar emptytime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		emptytime.set(Calendar.HOUR_OF_DAY, 0);
		emptytime.set(Calendar.MINUTE, 0);
		emptytime.set(Calendar.SECOND, 0);
		Date date = emptytime.getTime() ;
		HistoricalPrice curPrice = new HistoricalPrice(centralDB.getTradedate(), 
													   date, 
													   date, 
													   getStrSymbol(), 
													   getdOpen(), 
													   getdCurHigh(), 
													   getdCurLow(), 
													   getdClose(), 
													   (int)dCurVolume) ;
		HistoricalPrice lastPrice = null ;
		if (pricelist.size() > 1)
		{
			lastPrice = pricelist.get(pricelist.size()-1) ;
			pricetime.setTime(lastPrice.getKeytime());
		}
		if (strType.equals("D") && lastPrice != null)
		{
			if (emptytime.get(Calendar.DATE) == pricetime.get(Calendar.DATE))
			{
				if (PriceUtils.isZero(getdCurHigh()) && lastPrice.getHigh() < getdCurHigh())
				{
					lastPrice.setHigh(getdCurHigh());
				}
				if (PriceUtils.isZero(getdCurLow()) && lastPrice.getLow() > getdCurLow())
				{
					lastPrice.setLow(getdCurLow());
				}
				if (!PriceUtils.isZero(getdClose()))
				{
					lastPrice.setClose(getdClose());
				}
				lastPrice.setDatatime(date);
			}
			else
			{
				pricelist.add(curPrice) ;
			}
		}
		else if (strType.equals("W") && lastPrice != null)
		{
			if (getWeek(sdf.format(emptytime.getTime())) == getWeek(sdf.format(pricetime.getTime())))
			{
				lastPrice.setDatatime(date);
				if (PriceUtils.isZero(getdCurHigh()) && lastPrice.getHigh() < getdCurHigh())
				{
					lastPrice.setHigh(getdCurHigh());
				}
				if (PriceUtils.isZero(getdCurLow()) && lastPrice.getLow() > getdCurLow())
				{
					lastPrice.setLow(getdCurLow());
				}
				if (!PriceUtils.isZero(getdClose()))
				{
					lastPrice.setClose(getdClose());
				}
				lastPrice.setDatatime(date);
			}
			else
			{
				pricelist.add(curPrice) ;
			}
		}
		else if (strType.equals("M") && lastPrice != null)
		{
			if (emptytime.get(Calendar.MONTH) == pricetime.get(Calendar.MONTH) 
					&& emptytime.get(Calendar.YEAR) == pricetime.get(Calendar.YEAR))
			{
				lastPrice.setDatatime(date);
				if (PriceUtils.isZero(getdCurHigh()) && lastPrice.getHigh() < getdCurHigh())
				{
					lastPrice.setHigh(getdCurHigh());
				}
				if (PriceUtils.isZero(getdCurLow()) && lastPrice.getLow() > getdCurLow())
				{
					lastPrice.setLow(getdCurLow());
				}
				if (!PriceUtils.isZero(getdClose()))
				{
					lastPrice.setClose(getdClose());
				}
				lastPrice.setDatatime(date);
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
    	log.debug(strSymbol + "Processing type \"" + strType + "\" chart");
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
		IRefSymbolInfo refsymbol = centralDB.getRefSymbolInfo();
		SymbolInfo symbolinfo = refsymbol.get(refsymbol.at(new SymbolInfo(centralDB.getServerMarket(), getStrSymbol())));
		String strSymbol = null;
		if (symbolinfo != null)
		{
			if (symbolinfo.getHint() != null)
			{
				strSymbol = String.format("%s.%s", symbolinfo.getHint(), symbolinfo.getExchange());
			}
		}
		if (strSymbol == null)
		{
			strSymbol = getStrSymbol();
		}

    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;//"START TRANSACTION;" ;
		String strKeyTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormat) ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		for (HistoricalPrice price : prices)
		{
			if (price.getDatatime() == null)
			{
				continue;
			}
			strKeyTime = sdf.format(price.getKeytime()) ;
			sqlcmd = String.format(insertPrice, 
					strTable, price.getTradedate(), strKeyTime, sdf.format(price.getDatatime()), 
					getStrSymbol(), price.getOpen(), price.getClose(), 
					price.getHigh(), price.getLow(), price.getVolume(),  
					price.getTotalVolume(), price.getTurnover(), 
					price.getTradedate(), sdf.format(price.getDatatime()), price.getOpen(), 
					price.getClose(), price.getHigh(), price.getLow(), price.getVolume(),  
					price.getTotalVolume(), price.getTurnover()) ;
			centralDB.getDbhnd().addBatch(sqlcmd);
			if (strSymbol.equals(getStrSymbol()) == false)
			{
				sqlcmd = String.format(insertPrice, 
						strTable, price.getTradedate(), strKeyTime, sdf.format(price.getDatatime()), 
						strSymbol, price.getOpen(), price.getClose(), 
						price.getHigh(), price.getLow(), price.getVolume(),  
						price.getTotalVolume(), price.getTurnover(),
						price.getTradedate(), sdf.format(price.getDatatime()), price.getOpen(), 
						price.getClose(), price.getHigh(), price.getLow(), price.getVolume(),  
						price.getTotalVolume(), price.getTurnover()) ;
				centralDB.getDbhnd().addBatch(sqlcmd);
			}
			logHistoricalPrice(price);
		}
		centralDB.getDbhnd().executeBatch();
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
		IRefSymbolInfo refsymbol = centralDB.getRefSymbolInfo();
		SymbolInfo symbolinfo = refsymbol.get(refsymbol.at(new SymbolInfo(centralDB.getServerMarket(), symbol)));
		String strSymbol = null;
		if (symbolinfo != null)
		{
			if (symbolinfo.getHint() != null)
			{
				strSymbol = String.format("%s.%s", symbolinfo.getHint(), symbolinfo.getExchange());
			}
		}
		if (strSymbol == null)
		{
			strSymbol = symbol;
		}
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String sqlcmd = String.format("SELECT * FROM %s_%s WHERE `SYMBOL`='%s' AND `KEYTIME`>='%s' AND `KEYTIME`<'%s' ORDER BY `KEYTIME`;", 
				prefix, type, strSymbol, sdfprice.format(start), sdfprice.format(end)) ;
		ResultSet rs = centralDB.getDbhnd().querySQL(sqlcmd) ;
		try {
			while(rs.next())
			{
				HistoricalPrice price = new HistoricalPrice(); 
				price.setTradedate(rs.getString("TRADEDATE"));
				if (rs.getString("KEYTIME") != null) price.setKeytime(sdfprice.parse(rs.getString("KEYTIME")));
				if (rs.getString("DATATIME") != null) price.setDatatime(sdfprice.parse(rs.getString("DATATIME")));
				price.setSymbol(rs.getString("SYMBOL"));
				price.setOpen(rs.getDouble("OPEN_PRICE"));
				price.setClose(rs.getDouble("CLOSE_PRICE"));
				price.setHigh(rs.getDouble("HIGH_PRICE"));
				price.setLow(rs.getDouble("LOW_PRICE"));
				strtmp = rs.getString("VOLUME") ;
				if (strtmp != null && !strtmp.toLowerCase().equals("null"))
				{
					price.setVolume(Long.parseLong(strtmp));
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
	
	public static int getWeek(String strDate) 
	{
		// strDate: yyyy-MM-dd
		int year,month,day,total_day;
		int  monthday[] = {0,31,59,90,120,151,181,212,243,273,304,334,365};
		int smonthday[] = {0,31,60,91,121,152,182,213,244,274,305,335,366};

	 	try 
	 	{
	 		year  = Integer.parseInt(strDate.substring(0,4));
	 		month = Integer.parseInt(strDate.substring(5,7));
			day   = Integer.parseInt(strDate.substring(8,10));
		} 
	 	catch (Exception e) 
	 	{
	 		return 0;
		}
	        
		if (year%4==0)
			total_day = (year-1)*365+(year-1)/4+smonthday[month-1]+(day-1);
		else
			total_day = (year-1)*365+(year-1)/4+ monthday[month-1]+(day-1);

		return total_day / 7;
	}
	
	public void logHistoricalPrice(HistoricalPrice hp)
	{
		//if (...)
		log.debug(String.format("%s|%s : %s open: %.5f, high: %.5f, low: %.5f, close:%.5f, volume: %d", 
				hp.getKeytime(), hp.getDatatime(), hp.getSymbol(), hp.getOpen(),
				hp.getHigh(), hp.getLow(), hp.getClose(), hp.getVolume()));
	}
	@Override
	public int compareTo(SymbolData o) {
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
	public static IPriceSetter getSetter() {
		return setter;
	}
	public static void setSetter(IPriceSetter setter) {
		SymbolData.setter = setter;
	}
	public double getD52WHigh() {
		return d52WHigh;
	}
	public void setD52WHigh(double d52wHigh) {
		d52WHigh = d52wHigh;
	}
	public double getD52WLow() {
		return d52WLow;
	}
	public void setD52WLow(double d52wLow) {
		d52WLow = d52wLow;
	}
	public double getdCurHigh() {
		return dCurHigh;
	}
	public void setdCurHigh(double dCurHigh) {
		this.dCurHigh = dCurHigh;
	}
	public double getdCurLow() {
		return dCurLow;
	}
	public void setdCurLow(double dCurLow) {
		this.dCurLow = dCurLow;
	}
	public double getdOpen() {
		return dOpen;
	}
	public void setdOpen(double dOpen) {
		this.dOpen = dOpen;
	}
	public double getdClose() {
		return dClose;
	}
	public void setdClose(double dClose) {
		this.dClose = dClose;
	}
	
}
