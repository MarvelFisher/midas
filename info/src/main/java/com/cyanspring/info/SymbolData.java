package com.cyanspring.info;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final String insertPrice = "insert into %s (TRADEDATE,KEYTIME,DATATIME,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME) " + 
            "values ('%s','%s','%s','%s',%.5f,%.5f,%.5f,%.5f,%d) ON DUPLICATE KEY " + 
            "Update TRADEDATE=%s,DATATIME=%s,OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d;";
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
//		int tickCount = centralDB.getTickCount() ;
		priceData.clear();
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
		cal.set(Calendar.SECOND, 0);
		double dPrice = (bid + ask) / 2 ;
		HistoricalPrice price = priceData.get(cal.getTime()) ;
		if (price == null)
		{
			price = new HistoricalPrice(strSymbol, centralDB.getTradedate(), cal.getTime());
			priceData.put(cal.getTime(), price);
		}
		boolean changed = price.setPrice(dPrice);
		if (changed && writeMin) writeToMin() ;
		price.setDatatime(date) ;
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
                priceData = (TreeMap<Date, HistoricalPrice>)o;
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
    	log.debug(strSymbol + "Processing type \"" + strType + "\" chart");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date currentDate = cal.getTime();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Date keyDate = cal.getTime();
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;
		String tradeDate = centralDB.getTradedate() ;
		HistoricalPrice lastPrice;
		if (strType.equals("W") || strType.equals("M"))
		{
			lastPrice = centralDB.dbhnd.getLastValue(market, strType, getStrSymbol(), false) ;
			Calendar cal_ = Calendar.getInstance() ;
			if (lastPrice != null && lastPrice.getKeytime() != null)
			{
				cal_.setTime(lastPrice.getKeytime());
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
				//centralDB.dbhnd.deletePrice(market, strType, getStrSymbol(), lastPrice);
			}
			lastPrice.setTradedate(tradeDate);
			lastPrice.setKeytime(keyDate);
			lastPrice.setDatatime(currentDate);
			if (!bDelete)
			{
				if (PriceUtils.isZero(dOpen) == false) 
				{
					lastPrice.setOpen(dOpen);
				}
				lastPrice.setVolume((int)dCurVolume);
			}
			else
			{
				lastPrice.setVolume(lastPrice.getVolume() + (int)dCurVolume);
			}
			if (lastPrice.getHigh() < dCurHigh && PriceUtils.isZero(dCurHigh) == false)
			{
				lastPrice.setHigh(dCurHigh);
			}
			if (lastPrice.getLow() > dCurLow && PriceUtils.isZero(dCurLow) == false)
			{
				lastPrice.setLow(dCurLow);
			}
			if (PriceUtils.isZero(dClose) == false) 
			{
				lastPrice.setClose(dClose);
			}
		}
		else
		{
			lastPrice = new HistoricalPrice(centralDB.getTradedate(),
											keyDate,
											currentDate,
											strSymbol,
											dOpen,
											dCurHigh,
											dCurLow,
											dClose,
											(int)dCurVolume);
		}
		sqlcmd = String.format(insertPrice, 
				strTable, tradeDate, lastPrice.getKeytime(), lastPrice.getDatatime(), 
				getStrSymbol(), lastPrice.getOpen(), lastPrice.getClose(), 
				lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume(), 
				tradeDate, lastPrice.getDatatime(), lastPrice.getOpen(), 
				lastPrice.getClose(), lastPrice.getHigh(), lastPrice.getLow(), lastPrice.getVolume()) ;
		centralDB.dbhnd.updateSQL(sqlcmd);
		logHistoricalPrice(lastPrice);
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
		String sqlcmd = String.format("SELECT * FROM %s_W WHERE SYMBOL='%s' ORDER BY KEYTIME desc LIMIT 52;", 
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
		Calendar pricetime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		Calendar emptytime = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		int priceMin = 0;
		int emptyMin = 0;
		HistoricalPrice price;
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
		HistoricalPrice curPrice = new HistoricalPrice(centralDB.getTradedate(), 
													   date, 
													   date, 
													   getStrSymbol(), 
													   dOpen, 
													   dCurHigh, 
													   dCurLow, 
													   dClose, 
													   (int)dCurVolume) ;
		HistoricalPrice lastPrice = null ;
		if (pricelist.size() > 1)
		{
			lastPrice = pricelist.get(pricelist.size()-1) ;
			cal.setTime(lastPrice.getKeytime());
		}
		if (strType.equals("D") && lastPrice != null)
		{
			if (day == cal.get(Calendar.DATE))
			{
				if (PriceUtils.isZero(dCurHigh) && lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (PriceUtils.isZero(dCurLow) && lastPrice.getLow() > dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				if (!PriceUtils.isZero(dClose))
				{
					lastPrice.setClose(dClose);
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
			if (week == cal.get(Calendar.WEEK_OF_YEAR))
			{
				lastPrice.setDatatime(date);
				if (PriceUtils.isZero(dCurHigh) && lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (PriceUtils.isZero(dCurLow) && lastPrice.getLow() > dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				if (!PriceUtils.isZero(dClose))
				{
					lastPrice.setClose(dClose);
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
			if (month == cal.get(Calendar.MONTH))
			{
				lastPrice.setDatatime(date);
				if (PriceUtils.isZero(dCurHigh) && lastPrice.getHigh() < dCurHigh)
				{
					lastPrice.setHigh(dCurHigh);
				}
				if (PriceUtils.isZero(dCurLow) && lastPrice.getLow() > dCurLow)
				{
					lastPrice.setLow(dCurLow);
				}
				if (!PriceUtils.isZero(dClose))
				{
					lastPrice.setClose(dClose);
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

    	String prefix = (market.equals("FX")) ? "0040" : market;
		String strTable = String.format("%s_%s", prefix, strType) ;
		String sqlcmd = "" ;//"START TRANSACTION;" ;
		String strDateTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		centralDB.dbhnd.createStatement();
		for (HistoricalPrice price : prices)
		{
			if (price.getDatatime() == null)
			{
				continue;
			}
			strDateTime = sdf.format(price.getDatatime()) ;
			sqlcmd = String.format(insertPrice, 
					strTable, strDateTime, price.getKeytime(), price.getDatatime(), 
					getStrSymbol(), price.getOpen(), price.getClose(), 
					price.getHigh(), price.getLow(), price.getVolume(), 
					strDateTime, price.getDatatime(), price.getOpen(), 
					price.getClose(), price.getHigh(), price.getLow(), price.getVolume()) ;
			centralDB.dbhnd.addBatch(sqlcmd);
			logHistoricalPrice(price);
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
	public void logHistoricalPrice(HistoricalPrice hp)
	{
		//if (...)
		log.debug(String.format("%s|%s : %s open: %.5f, high: %.5f, low: %.5f, close:%.5f, volume: %d", 
				hp.getKeytime(), hp.getDatatime(), hp.getSymbol(), hp.getOpen(),
				hp.getHigh(), hp.getLow(), hp.getClose(), hp.getVolume()));
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
