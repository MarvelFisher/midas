package com.cyanspring.info;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

public class SymbolData implements Comparable<SymbolData>
{
	private static final Logger log = LoggerFactory
			.getLogger(SymbolData.class);
	private CentralDbProcessor centralDB ;
	private boolean isUpdating ;
	private String strSymbol ;
	private double d52WHigh ;
	private double d52WLow ; 
	private double dCurHigh ;
	private double dCurLow ;
	private double dOpen ;
	private double dClose ;
	private double dCurVolume ;
	private ArrayList<HistoricalPrice> priceData = new ArrayList<HistoricalPrice>() ;
	private ArrayList<Quote> quoteTmp = new ArrayList<Quote>() ;
	
	SymbolData(String strSymbol, CentralDbProcessor centralDB)
	{
		this.strSymbol = strSymbol ;
		this.centralDB = centralDB ;
		int tickCount = centralDB.getTickCount() ;
		for (int ii = 0; ii < tickCount; ii++)
		{
			priceData.add(new HistoricalPrice(this.strSymbol)) ;
		}
		readFromTick() ;
		get52WHighLow((byte)0x40) ;
	}
	public SymbolData(String symbol) {
		this.strSymbol = symbol ;
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
	public void setPrice(double bid, double ask, Date date)
	{
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(date) ;
		int curTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) ;
		int nPos = centralDB.getPosByTime(curTime) ;
		double dPrice = (bid + ask) / 2 ;
		HistoricalPrice price = priceData.get(nPos) ;
		price.setPrice(dPrice) ;
		price.setTimestamp(date) ;
		if (d52WHigh < dPrice)
		{
			d52WHigh = dPrice ;
		}
		else if (d52WLow > dPrice)
		{
			d52WLow = dPrice ;
		}
		if (dCurHigh < dPrice)
		{
			dCurHigh = dPrice ;
		}
		else if (dCurLow > dPrice)
		{
			dCurLow = dPrice ;
		}
		if (dOpen == 0)
		{
			dOpen = dPrice ;
		}
		dClose = dPrice ;
	}
	
	public void readFromTick()
	{
		isUpdating = true ;
		Calendar calStamp = Calendar.getInstance() ;
		String strFile = String.format("./DAT/%04d%02d%02d/%s.TCK", 
				calStamp.get(Calendar.YEAR), (calStamp.get(Calendar.MONTH)+1), calStamp.get(Calendar.DATE), strSymbol) ;
		String strIn = "" ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Quote quote = new Quote(strSymbol, null, null) ;
		StringTokenizer strtok ;
		try 
		{
			BufferedReader fileIn = new BufferedReader(new FileReader(strFile)) ;
			while((strIn = fileIn.readLine()) != null)
			{
				strtok = new StringTokenizer(strIn) ;
				strtok.nextToken("|") ;
				strtok.nextToken("|") ;
				quote.setBid(Double.parseDouble(strtok.nextToken("|")));
				quote.setAsk(Double.parseDouble(strtok.nextToken("|")));
				quote.setBidVol(Double.parseDouble(strtok.nextToken("|")));
				quote.setAskVol(Double.parseDouble(strtok.nextToken("|")));
				quote.setLast(Double.parseDouble(strtok.nextToken("|")));
				quote.setLastVol(Double.parseDouble(strtok.nextToken("|")));
				quote.setHigh(Double.parseDouble(strtok.nextToken("|")));
				quote.setLow(Double.parseDouble(strtok.nextToken("|")));
				quote.setOpen(Double.parseDouble(strtok.nextToken("|")));
				quote.setClose(Double.parseDouble(strtok.nextToken("|")));
				quote.setTotalVolume(Double.parseDouble(strtok.nextToken("|")));
				quote.setTimeStamp(dateFormat.parse(strtok.nextToken("|")));
				quote.setTimeSent(dateFormat.parse(strtok.nextToken("|")));
				setPrice(quote.getBid(), quote.getAsk(), quote.getTimeStamp());
			}
			fileIn.close();
		} 
		catch (IOException | ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			isUpdating = false ;
		}
		isUpdating = false ;
	}
	
	public void insertSQLDate(byte service, String strType)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		String strTable = String.format("%04X_%s", service, strType) ;
		String sqlcmd = "" ;
		String strDateTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00") ;
		strDateTime = sdf.format(cal.getTime()) ;
		if (strType.equals("W") || strType.equals("M"))
		{
			HistoricalPrice lastPrice = centralDB.dbhnd.getLastValue(service, strType, strSymbol, false) ;
			Calendar cal_ = Calendar.getInstance() ;
			cal_.setTime(lastPrice.getTimestamp());
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
				centralDB.dbhnd.deletePrice(service, strType, strSymbol, lastPrice);
			}
		}
		sqlcmd = String.format(
				"insert into %s (TRADEDATE,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME) ", strTable) ;
		sqlcmd += String.format(
                "values ('%s','%s',%.5f,%.5f,%.5f,%.5f,%d) ON DUPLICATE KEY ",
                strDateTime, strSymbol, dOpen, dClose, dCurHigh, dCurLow, (int)dCurVolume) ;
		sqlcmd += String.format(
				"Update OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d;",
				dOpen, dClose, dCurHigh, dCurLow, (int)dCurVolume) ;
		centralDB.dbhnd.updateSQL(sqlcmd);
	}
	
	public PriceHighLow getPriceHighLow(PriceHighLowType type)
	{
		PriceHighLow phl = new PriceHighLow() ;
		if (type == PriceHighLowType.DAY)
		{
			phl.setHigh(this.dCurHigh);
			phl.setLow(this.dCurLow);
			phl.setSymbol(this.strSymbol);
			phl.setType(type) ;
		}
		else if (type == PriceHighLowType.W52)
		{
			phl.setHigh(this.d52WHigh);
			phl.setLow(this.d52WLow);
			phl.setSymbol(this.strSymbol);
			phl.setType(type) ;
		}
		return phl ;
	}
	
	public void get52WHighLow(byte service)
	{
		String sqlcmd = String.format("SELECT * FROM %04X_W WHERE SYMBOL='%s' ORDER BY TRADEDATE desc LIMIT 52;", 
				service, strSymbol) ;
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
				if (this.d52WLow > dLow || this.d52WLow == 0)
				{
					this.d52WLow = dLow ;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ;
	}
	
	public ArrayList<HistoricalPrice> getPriceList(byte service, String strType, Date end, boolean fill) throws ParseException
	{
		ArrayList<HistoricalPrice> prices = new ArrayList<HistoricalPrice>() ;
		HistoricalPrice priceEmpty = centralDB.dbhnd.getLastValue(service, strType, strSymbol, false) ;
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
					if (fill == true)
					{
						price.copy((HistoricalPrice)priceEmpty.clone());
						price.setTimestamp(firsttime.getTime());
					}
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
				prices.add(price) ;
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
				if (strType.equals("R") 
						&& (pricetime.get(Calendar.MINUTE)/5 != emptytime.get(Calendar.MINUTE)/5))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 5) * 5)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("A") 
						&& (pricetime.get(Calendar.MINUTE)/10 != emptytime.get(Calendar.MINUTE)/10))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 10) * 10)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("Q") 
						&& (pricetime.get(Calendar.MINUTE)/15 != emptytime.get(Calendar.MINUTE)/15))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 15) * 15)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("H") 
						&& (pricetime.get(Calendar.MINUTE)/30 != emptytime.get(Calendar.MINUTE)/30))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, ((pricetime.get(Calendar.MINUTE) / 30) * 30)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("6") 
						&& (pricetime.get(Calendar.HOUR_OF_DAY) != emptytime.get(Calendar.HOUR_OF_DAY))
						&& (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE)))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
					cal.setTime(price.getTimestamp());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.HOUR_OF_DAY, pricetime.get(Calendar.HOUR_OF_DAY)) ;
					priceEmpty.setTimestamp(cal.getTime());
				}
				else if (strType.equals("T") 
						&& (pricetime.get(Calendar.HOUR_OF_DAY)/4 != emptytime.get(Calendar.HOUR_OF_DAY)/4)
						&& (pricetime.get(Calendar.DATE) != emptytime.get(Calendar.DATE)))
				{
					prices.add((HistoricalPrice)priceEmpty.clone()) ;
					priceEmpty.copy(price);
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
	
	public void insertSQLTick(byte service, String strType)
	{
		ArrayList<HistoricalPrice> prices = null ;
		try {
			prices = getPriceList(service, strType, null, true);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
			return ;
		}
		if (prices.isEmpty())
		{
			return ;
		}
		
		String strTable = String.format("%04X_%s", service, strType) ;
		String sqlcmd = "" ;//"START TRANSACTION;" ;
		String strDateTime = "" ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00") ;
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		for (HistoricalPrice price : prices)
		{
			strDateTime = sdf.format(price.getTimestamp()) ;
			sqlcmd += String.format(
					"insert into %s (TRADEDATE,SYMBOL,OPEN_PRICE,CLOSE_PRICE,HIGH_PRICE,LOW_PRICE,VOLUME) " + 
	                "values ('%s','%s',%.5f,%.5f,%.5f,%.5f,%d) ON DUPLICATE KEY " + 
					"Update OPEN_PRICE=%.5f,CLOSE_PRICE=%.5f,HIGH_PRICE=%.5f,LOW_PRICE=%.5f,VOLUME=%d;",
					strTable, strDateTime, price.getSymbol(), price.getOpen(),
					price.getClose(), price.getHigh(), price.getLow(), (int)price.getVolume(),
					price.getOpen(), price.getClose(), price.getHigh(), price.getLow(), (int)price.getVolume()) ;
			centralDB.dbhnd.updateSQL(sqlcmd);
		}
		return ;
	}
	
	public List<HistoricalPrice> getHistoricalPrice(byte service, String type, Date start, Date end)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;
		SimpleDateFormat sdftime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		SimpleDateFormat sdfprice = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		sdfprice.setTimeZone(TimeZone.getTimeZone("GMT"));
		String tradedate = centralDB.getTradedate() ;
		String enddate = sdf.format(end) ;
		String strtmp ;
		ArrayList<HistoricalPrice> listPrice = new ArrayList<HistoricalPrice>() ;
		String sqlcmd = String.format("SELECT * FROM %04X_%s WHERE TRADEDATE>='%s' AND TRADEDATE<='%s';", 
				service, type, sdftime.format(start), sdftime.format(end)) ;
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
				if (strtmp != null || !strtmp.toLowerCase().equals("null"))
				{
					price.setVolume(Integer.parseInt(strtmp));
				}
				listPrice.add(price) ;
			}
		} catch (SQLException | ParseException e) {
			// TODO Auto-generated catch block
			log.error(e.toString(), e);
			return null ;
		}
		if (0 < tradedate.compareTo(enddate))
		{
			try {
				ArrayList<HistoricalPrice> listDaily = getPriceList((byte)0x40, type, end, false) ;
				listPrice.addAll(listDaily) ;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.error(e.toString(), e);
				return null ;
			}
		}
		return null ;
	}
	@Override
	public int compareTo(SymbolData o) {
		// TODO Auto-generated method stub
		return this.strSymbol.compareTo(o.strSymbol);
	}
	
}
