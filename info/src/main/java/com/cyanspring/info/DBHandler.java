package com.cyanspring.info;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.data.JdbcSQLHandler;
import com.cyanspring.common.info.IRefSymbolInfo;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.util.PriceUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBHandler 
{
	private static final Logger log = LoggerFactory
			.getLogger(DBHandler.class);
	private final String createTable = "CREATE TABLE `%s` (`TRADEDATE`  date NULL DEFAULT NULL ,`KEYTIME`  datetime NOT NULL ,`DATATIME`  datetime NULL DEFAULT NULL ,`SYMBOL`  varchar(16) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL ,`OPEN_PRICE`  double NULL DEFAULT NULL ,`CLOSE_PRICE`  double NULL DEFAULT NULL ,`HIGH_PRICE`  double NULL DEFAULT NULL ,`LOW_PRICE`  double NULL DEFAULT NULL ,`VOLUME`  int(11) NULL DEFAULT NULL ,`TOTALVOLUME`  bigint(20) NULL DEFAULT NULL ,UNIQUE INDEX `TradeDate_Symbol` USING BTREE (`KEYTIME`, `SYMBOL`)) ENGINE=MyISAM DEFAULT CHARACTER SET=latin1 COLLATE=latin1_swedish_ci CHECKSUM=0 ROW_FORMAT=Dynamic DELAY_KEY_WRITE=0 ;";
	private JdbcSQLHandler handler;
	
	public DBHandler(ComboPooledDataSource cpds) throws Exception
	{
		handler = new JdbcSQLHandler(cpds);
	}
	public Connection getConnect()
	{
		return handler.getConnect();
	}
	private void closeConnect(Connection conn) {
		handler.closeConnect(conn);
    }
	public boolean isConnected()
    {
        return handler.isConnected();
    }
    public void reconnectSQL()
    {
    	handler.reconnectSQL();
    }
    public void disconnectSQL()
    {
    	handler.disconnectSQL();
    }
    public boolean updateSQL(String sqlcmd)
    {
    	return handler.updateSQL(sqlcmd);
    }
    public ResultSet querySQL(Connection connect, String sqlcmd)
    {
        return handler.querySQL(connect, sqlcmd);
    }
    public boolean checkSQLConnect()
    {
    	return handler.checkSQLConnect();
    }
    public void createStatement()
    {
    	handler.createStatement();
    }
    public void closeStatement()
    {
    	handler.closeStatement();
    }
    public void addBatch(String sqlcmd)
    {
    	handler.addBatch(sqlcmd);
    }
    public boolean executeBatch()
    {
    	return handler.executeBatch();
    }
    public void get52WHighLow(SymbolData symboldata, String market, String symbol)
    {
        Connection connect = getConnect();
        if (connect == null)
        {
        	return ;
        }
    	String prefix = (market.equals("FX")) ? "0040" : market;
		String sqlcmd = String.format("SELECT * FROM %s_W WHERE SYMBOL='%s' ORDER BY KEYTIME desc LIMIT 52;", 
				prefix, symbol) ;
        ResultSet rs = null;
        Statement stat = null ;
        try
        {
            stat = connect.createStatement();
        	connect.setAutoCommit(false);
            rs = stat.executeQuery(sqlcmd);
			connect.commit();
        }
        catch (SQLException se)
        {
            log.error(se.getMessage(), se);
			log.warn("Exception while: " + sqlcmd);
			closeConnect(connect);
        }
		try 
		{
			double dHigh = 0 ; 
			double dLow = 0 ;
			while(rs.next())
			{
				dHigh = rs.getDouble("HIGH_PRICE") ;
				dLow = rs.getDouble("LOW_PRICE") ;
				if (symboldata.getD52WHigh() < dHigh)
				{
					symboldata.setD52WHigh(dHigh) ;
				}
				if (PriceUtils.isZero(symboldata.getD52WLow()) || symboldata.getD52WLow() > dLow)
				{
					symboldata.setD52WLow(dLow) ;
				}
			}
			rs.close();
		} 
        catch (SQLException e) 
        {
			log.error(e.getMessage(), e) ;
		}
		finally
		{
			closeConnect(connect);
		}
    }
    public List<SymbolInfo> getGroupSymbol(String user, String group, String market, IRefSymbolInfo refSymbolInfo, boolean set)
    {
    	ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>(); 
		String sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `GROUP`='%s' AND `MARKET`='%s' ORDER BY `NO`;", 
				user, group, market) ;

		Connection connect = getConnect();
		if (connect == null)
		{
			return retsymbollist;
		}
		ResultSet rs = querySQL(connect, sqlcmd);
		SymbolInfo symbolinfo;
		int index;
		try {
			while(rs.next())
			{
				if (set)
				{
					index = refSymbolInfo.at(new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE")));
					if (index >= 0)
					{
						symbolinfo = refSymbolInfo.get(index);
						retsymbollist.add(symbolinfo);
					}
				}
				else
				{
					symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
					symbolinfo.setHint(rs.getString("HINT"));
					retsymbollist.add(symbolinfo);
				}
			}
		}
        catch (SQLException e) 
        {
			log.error(e.getMessage(), e) ;
		}
		finally
		{
			closeConnect(connect);
		}
		return retsymbollist;
    }
    public List<String> getGroupList(String user, String market)
    {
    	ArrayList<String> retsymbollist = new ArrayList<String>(); 
		String sqlcmd = String.format("SELECT `GROUP` FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `MARKET`='%s';", 
				user, market) ;

		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		ResultSet rs = querySQL(connect, sqlcmd);
		try 
		{
			int pos;
			String group;
			while(rs.next())
			{
				group = rs.getString("GROUP").toLowerCase();
				pos = Collections.binarySearch(retsymbollist, group);
				if (pos < 0)
				{
					retsymbollist.add(~pos, group);
				}
			}
		} 
		catch (SQLException e) 
		{
			log.error(e.getMessage(), e) ;
		}
		finally
		{
			closeConnect(connect);
		}
		return retsymbollist;
    }
    public HistoricalPrice getLastValue(String market, String type, String symbol, boolean dir)
    {
    	if (market == null)
    	{
    		return null;
    	}
    	HistoricalPrice price = new HistoricalPrice() ;
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
    	String sqlcmd = "" ;
    	boolean getPrice = false;
    	if (dir)
    	{
    		sqlcmd = String.format("select * from %s where `SYMBOL` = '%s' order by `KEYTIME` limit 1 ;", 
    				strTable, symbol) ;
    	}
    	else
    	{
    		sqlcmd = String.format("select * from %s where `SYMBOL` = '%s' order by `KEYTIME` desc limit 1 ;", 
    				strTable, symbol) ;
    	}
		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		ResultSet rs = querySQL(connect, sqlcmd);
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			if (rs.next())
			{
				price.setTradedate(rs.getString("TRADEDATE"));
				if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
				if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
				price.setSymbol(rs.getString("SYMBOL")) ;
				price.setOpen(rs.getDouble("OPEN_PRICE"));
				price.setClose(rs.getDouble("CLOSE_PRICE"));
				price.setHigh(rs.getDouble("HIGH_PRICE"));
				price.setLow(rs.getDouble("LOW_PRICE"));
				price.setVolume(rs.getLong("VOLUME"));
				price.setTotalVolume(rs.getLong("TOTALVOLUME"));
				price.setTurnover(rs.getLong("TURNOVER"));
				getPrice = true;
			}
			rs.close();
			if (getPrice)
			{
				return price ;
			}
			else
			{
				return null;
			}
		} 
    	catch (SQLException e) 
    	{
            log.error(e.getMessage(), e) ;
            log.trace(sqlcmd);
			return null ;
		} 
    	catch (ParseException e) 
    	{
            log.error(e.getMessage(), e) ;
			return null ;
		}
    	finally
    	{
    		closeConnect(connect);
    	}
    }
    public List<HistoricalPrice> getPeriodValue(String market, String type, String symbol, Date startdate)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
    	String sqlcmd = "" ;
		sqlcmd = String.format("SELECT * FROM %s WHERE `SYMBOL`='%s' AND `KEYTIME`>='%s' ORDER BY `KEYTIME`;", 
				strTable, symbol, sdf.format(startdate));

		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		ResultSet rs = querySQL(connect, sqlcmd);
    	try 
    	{
    		HistoricalPrice price;
    		ArrayList<HistoricalPrice> retList = new ArrayList<HistoricalPrice>(); 
			while (rs.next())
			{
				price = new HistoricalPrice();
				price.setTradedate(rs.getString("TRADEDATE"));
				if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
				if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
				price.setSymbol(rs.getString("SYMBOL")) ;
				price.setOpen(rs.getDouble("OPEN_PRICE"));
				price.setClose(rs.getDouble("CLOSE_PRICE"));
				price.setHigh(rs.getDouble("HIGH_PRICE"));
				price.setLow(rs.getDouble("LOW_PRICE"));
				price.setVolume(rs.getLong("VOLUME"));
				price.setTotalVolume(rs.getLong("TOTALVOLUME"));
				price.setTurnover(rs.getLong("TURNOVER"));
				retList.add(price);
			}
			rs.close();
			return retList;
		} 
    	catch (SQLException e) 
		{
            log.error(e.getMessage(), e) ;
            log.trace(sqlcmd);
			return null ;
		} 
    	catch (ParseException e) 
    	{
            log.error(e.getMessage(), e) ;
			return null ;
		}
    	finally
    	{
    		closeConnect(connect);
    	}
    }
    
    public List<HistoricalPrice> getPeriodStartEndValue(String market, String type, String symbol, Date startDate, Date endDate)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
    	String sqlcmd = "" ;
    	if (startDate == null)
    	{
    		sqlcmd = String.format("SELECT * FROM %s WHERE `SYMBOL`='%s' AND `KEYTIME`<='%s' ORDER BY `KEYTIME`;", 
					strTable, symbol, sdf.format(endDate));
    	}
    	else
    	{
			sqlcmd = String.format("SELECT * FROM %s WHERE `SYMBOL`='%s' AND `KEYTIME`>='%s' AND `KEYTIME`<='%s' ORDER BY `KEYTIME`;", 
					strTable, symbol, sdf.format(startDate), sdf.format(endDate));
    	}

		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		ResultSet rs = querySQL(connect, sqlcmd);
		if (rs == null)
		{
			return null;
		}
    	try 
    	{
    		HistoricalPrice price;
    		ArrayList<HistoricalPrice> retList = new ArrayList<HistoricalPrice>(); 
			while (rs.next())
			{
				try
				{
					price = new HistoricalPrice();
					price.setTradedate(rs.getString("TRADEDATE"));
					if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
					if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
					price.setSymbol(rs.getString("SYMBOL")) ;
					price.setOpen(rs.getDouble("OPEN_PRICE"));
					price.setClose(rs.getDouble("CLOSE_PRICE"));
					price.setHigh(rs.getDouble("HIGH_PRICE"));
					price.setLow(rs.getDouble("LOW_PRICE"));
					price.setVolume(rs.getLong("VOLUME"));
					price.setTotalVolume(rs.getLong("TOTALVOLUME"));
					price.setTurnover(rs.getLong("TURNOVER"));
					retList.add(price);
				}
		    	catch (Exception e) 
				{
		            log.error(e.getMessage(), e) ;
		            continue;
				} 
			}
			rs.close();
			return retList;
		} 
    	catch (SQLException e) 
		{
            log.error(e.getMessage(), e) ;
            log.trace(sqlcmd);
			return null ;
		} 
    	finally
    	{
    		closeConnect(connect);
    	}
    }
    
    public List<HistoricalPrice> getCountsValue(String market, String type, String symbol, int dataCount, String retrieveDate,boolean bLimit)
    {
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
    	String sqlcmd = "" ;
    	if(bLimit) 
    	{
    		sqlcmd = String.format("SELECT * FROM %s WHERE `SYMBOL`='%s' AND `KEYTIME`>'%s' ORDER BY `KEYTIME` DESC LIMIT %d;", 
				strTable, symbol, retrieveDate, dataCount);
    	} 
    	else 
    	{
    		sqlcmd = String.format("SELECT * FROM %s WHERE `SYMBOL`='%s' ORDER BY `KEYTIME` DESC LIMIT %d;", 
    				strTable, symbol, dataCount);    		
    	}

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		log.debug("Query: " + sqlcmd);
		ResultSet rs = querySQL(connect, sqlcmd);
		log.debug("Query return: " + sqlcmd);
    	try 
    	{
    		HistoricalPrice price;
    		ArrayList<HistoricalPrice> retList = new ArrayList<HistoricalPrice>(); 
			while (rs.next())
			{
				try
				{
					price = new HistoricalPrice();
					price.setTradedate(rs.getString("TRADEDATE"));
					if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
					if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
					price.setSymbol(rs.getString("SYMBOL")) ;
					price.setOpen(rs.getDouble("OPEN_PRICE"));
					price.setClose(rs.getDouble("CLOSE_PRICE"));
					price.setHigh(rs.getDouble("HIGH_PRICE"));
					price.setLow(rs.getDouble("LOW_PRICE"));
					price.setVolume(rs.getLong("VOLUME"));
					price.setTotalVolume(rs.getLong("TOTALVOLUME"));
					price.setTurnover(rs.getLong("TURNOVER"));
					retList.add(price);
				}
		    	catch (Exception e) 
				{
		            log.error(e.getMessage(), e) ;
		            continue;
				}
			}
			rs.close();
			log.debug("Get Historical List size: " + retList.size());
			Collections.sort(retList);
			return retList;
		} 
    	catch (SQLException e) 
		{
            log.error(e.getMessage(), e) ;
            log.trace(sqlcmd);
			return null ;
		} 
    	finally
    	{
    		closeConnect(connect);
    	}
    }
    
    public Map<String, List<HistoricalPrice>> getTotalValue(String market, String type, String retrieveDate)
    {
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
    	String sqlcmd = "" ;
    	if (retrieveDate == null)
    		sqlcmd = String.format("SELECT * FROM %s;", strTable);
    	else
    		sqlcmd = String.format("SELECT * FROM %s WHERE `KEYTIME`>'%s';", strTable, retrieveDate);

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Connection connect = getConnect();
		if (connect == null)
		{
			return null;
		}
		log.debug("Query: " + sqlcmd);
		ResultSet rs = querySQL(connect, sqlcmd);
		log.debug("Query return: " + sqlcmd);		
    	try 
    	{
    		HistoricalPrice price;
    		HashMap<String, List<HistoricalPrice>> retMap = new HashMap<String, List<HistoricalPrice>>();
    		String symbol;
    		List<HistoricalPrice> lst;    
    		int nCount = 0;
    		int nPos;
			while (rs.next())
			{
				try
				{
					symbol = rs.getString("SYMBOL");
					lst = retMap.get(symbol);
					if (lst == null)
					{
						lst = new ArrayList<HistoricalPrice>();
						retMap.put(symbol, lst);
					}
					price = new HistoricalPrice();
					price.setTradedate(rs.getString("TRADEDATE"));
					if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
					if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
					price.setSymbol(rs.getString("SYMBOL")) ;
					price.setOpen(rs.getDouble("OPEN_PRICE"));
					price.setClose(rs.getDouble("CLOSE_PRICE"));
					price.setHigh(rs.getDouble("HIGH_PRICE"));
					price.setLow(rs.getDouble("LOW_PRICE"));
					price.setVolume(rs.getLong("VOLUME"));
					price.setTotalVolume(rs.getLong("TOTALVOLUME"));
					price.setTurnover(rs.getLong("TURNOVER"));
					nPos = Collections.binarySearch(lst, price);
					
					if (price.notNull() == false) continue;
					if (nPos < 0)
					{
						lst.add(~nPos, price);
						if (lst.size() > 1440)
						{
							lst.remove(0);
						}
					}
	//				lst.add(price);
					nCount++;
				}
				catch (Exception e)
				{
		            log.error(e.getMessage(), e) ;
		            continue;
				}
			}
			rs.close();
			log.debug("Get Historical List size: " + nCount);
			return retMap;
		} 
    	catch (SQLException e) 
		{
            log.error(e.getMessage(), e) ;
            log.trace(sqlcmd);
			return null ;
		} 
    	finally
    	{
    		closeConnect(connect);
    	}
    }
    
//    public List<HistoricalPrice> getHistoricalPrice(String sqlcmd)
//    {
//    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		Connection connect = getConnect();
//		if (connect == null)
//		{
//			return null;
//		}
//		log.debug("Query: " + sqlcmd);
//		ResultSet rs = querySQL(connect, sqlcmd);
//		log.debug("Query return: " + sqlcmd);
//    	try 
//    	{
//    		HistoricalPrice price;
//    		ArrayList<HistoricalPrice> retList = new ArrayList<HistoricalPrice>(); 
//			while (rs.next())
//			{
//				price = new HistoricalPrice();
//				price.setTradedate(rs.getString("TRADEDATE"));
//				if (rs.getString("KEYTIME") != null) price.setKeytime(sdf.parse(rs.getString("KEYTIME")));
//				if (rs.getString("DATATIME") != null) price.setDatatime(sdf.parse(rs.getString("DATATIME")));
//				price.setSymbol(rs.getString("SYMBOL")) ;
//				price.setOpen(rs.getDouble("OPEN_PRICE"));
//				price.setClose(rs.getDouble("CLOSE_PRICE"));
//				price.setHigh(rs.getDouble("HIGH_PRICE"));
//				price.setLow(rs.getDouble("LOW_PRICE"));
//				price.setVolume(rs.getLong("VOLUME"));
//				price.setTotalVolume(rs.getLong("TOTALVOLUME"));
//				price.setTurnover(rs.getLong("TURNOVER"));
//				retList.add(price);
//			}
//			rs.close();
//			log.debug("Get Historical List size: " + retList.size());
//			return retList;
//		} 
//    	catch (SQLException e) 
//		{
//            log.error(e.getMessage(), e) ;
//            log.trace(sqlcmd);
//			return null ;
//		} 
//    	catch (ParseException e) 
//    	{
//            log.error(e.getMessage(), e) ;
//			return null ;
//		}
//    	finally
//    	{
//    		closeConnect(connect);
//    	}
//    }
    
    public void deletePrice(String market, String type, String symbol, HistoricalPrice price)
    {
    	if (price == null)
    	{
    		return;
    	}
    	if (market == null)
    	{
    		return;
    	}
    	String prefix = (market.equals("FX")) ? "0040" : market;
    	String strTable = String.format("%s_%s", prefix, type) ;
		String sqlcmd = String.format("DELETE FROM %s WHERE `SYMBOL`='%s' AND `KEYTIME`='%s';", 
				strTable, symbol, price.getKeytime()) ;
		updateSQL(sqlcmd) ;
    }
    public void checkMarketExist(String market)
    {
    	String prefix = null;
    	if (market.equals("FX"))
    	{
    		prefix = "0040";
    	}
    	else
    	{
    		prefix = market;
    	}
    	checkTableExist(prefix + "_1");
    	checkTableExist(prefix + "_R");
    	checkTableExist(prefix + "_A");
    	checkTableExist(prefix + "_Q");
    	checkTableExist(prefix + "_H");
    	checkTableExist(prefix + "_6");
    	checkTableExist(prefix + "_T");
    	checkTableExist(prefix + "_D");
    	checkTableExist(prefix + "_W");
    	checkTableExist(prefix + "_M");
    }
    public void checkTableExist(String table)
    {
        Connection connect = getConnect();
        if (connect == null)
        {
        	return;
        }
    	if (table == null)
    		return;
    	try 
    	{
        	DatabaseMetaData dbm = connect.getMetaData();
        	ResultSet tables = dbm.getTables(null, null, table, null);
        	if (tables.next()) {
        	  // Table exists
        	}
        	else {
				String sqlcmd = String.format(createTable, table);
				updateSQL(sqlcmd);
        	}
		} 
    	catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
    	finally
    	{
    		closeConnect(connect);
    	}
    }
}
