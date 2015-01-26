package com.cyanspring.info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.info.HistoricalPriceEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestEvent;
import com.cyanspring.common.event.info.PriceHighLowEvent;
import com.cyanspring.common.event.info.PriceHighLowRequestEvent;
import com.cyanspring.common.event.info.SearchSymbolEvent;
import com.cyanspring.common.event.info.SearchSymbolRequestEvent;
import com.cyanspring.common.event.info.SearchSymbolType;
import com.cyanspring.common.event.info.SymbolListSubscribeEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeType;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.SymbolEvent;
import com.cyanspring.common.event.marketdata.SymbolRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.PriceHighLow;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.event.AsyncEventProcessor;


public class CentralDbProcessor implements IPlugin
{
	private static final Logger log = LoggerFactory
			.getLogger(CentralDbProcessor.class);

	private String host;
	private String user;
	private String pass;
	private String database;
	private int open ;
	private int preopen ;
	private int close ;
	private HashMap<String, ArrayList<String>> userSymbolList;
	
	private int    nOpen ;
	private int    nClose ;
	private int    nPreOpen ;
	
	private int    nTickCount ;
	private MarketSessionType sessionType ;
	private String tradedate ;
	
	private HashMap<String, ArrayList<String>> mapDefaultSymbol = new HashMap<String, ArrayList<String>>();
	private ArrayList<SymbolData> listSymbolData = new ArrayList<SymbolData>();
	DBHandler dbhnd ;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	private SystemInfo systemInfoMD;
	
	@Autowired
	private RefDataManager refDataManager;
	
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor(){

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PriceHighLowRequestEvent.class, null);
			subscribeToEvent(SearchSymbolRequestEvent.class, null);
			subscribeToEvent(SymbolListSubscribeRequestEvent.class, null);
			subscribeToEvent(HistoricalPriceRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}		
	};
	
	
	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(SymbolEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}

	};
	
	public int getTickCount()
	{
		int nOpen = this.nOpen ;
		int nClose = (this.nClose < this.nOpen) ? (this.nClose + 1440) : this.nClose ;
		return nClose - nOpen ;
	}
	
	public int getPosByTime(int inputTime)
	{
		boolean overDay = (this.nClose < this.nOpen) ;
		int nOpen = this.nOpen ;
		int nClose = (overDay) ? (this.nClose + 1440) : this.nClose ;
		int nPreOpen = (overDay) ? (this.nPreOpen + 1440) : this.nPreOpen ;
		int curTime = (inputTime < this.nOpen && inputTime < this.nClose) ? inputTime + 1440 : inputTime ;
		if (overDay)
		{
			if (curTime < nPreOpen && curTime > nClose)
			{
				return nTickCount ;
			}
			else if (curTime > nPreOpen)
			{
				return 0 ;
			}
			else if (curTime < nOpen)
			{
				return 0 ;
			}
			else
			{
				return curTime - nOpen ;
			}
		}
		else
		{
			if (curTime > nClose)
			{
				return nTickCount ;
			}
			else if (curTime < nOpen)
			{
				return 0 ;
			}
			else 
			{
				return curTime - nOpen ;
			}
		}
	}
	
	public void processQuoteEvent(QuoteEvent event)
	{
		Quote quote = event.getQuote() ;
		writeToTick(quote) ;
		SymbolData symbolData = new SymbolData(quote.getSymbol(), this) ;
		synchronized(this)
		{
			int index = Collections.binarySearch(listSymbolData, symbolData) ;
			if (index < 0)
			{
				listSymbolData.add(~index, symbolData) ;
				index = ~index ;
			}
			listSymbolData.get(index).setPrice(quote);
		}
		log.debug("Quote: " + quote);
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event)
	{
		setSessionType(event.getSession()) ;
		this.tradedate = event.getTradeDate() ;
	}
	
	public void processPriceHighLowRequestEvent(PriceHighLowRequestEvent event)
	{
		int index;
		String sender = event.getSender() ;
		List<String> symbolList = event.getSymbolList() ;
//		Collections.sort(listSymbolData) ;
		ArrayList<PriceHighLow> phlList = new ArrayList<PriceHighLow>() ;
		synchronized(this)
		{
			for (String symbol : symbolList)
			{
				index = Collections.binarySearch(listSymbolData, new SymbolData(symbol));
				if (index < 0)
				{
					continue ;
				}
				phlList.add(listSymbolData.get(index).getPriceHighLow(event.getType())) ;
			}
		}
		PriceHighLowEvent retEvent = new PriceHighLowEvent(null, sender) ;
		retEvent.setType(event.getType());
		if (phlList.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage("No requested symbol is find");
		}
		else
		{
			retEvent.setOk(true);
			retEvent.setListHighLow(phlList);
		}
		sendEvent(retEvent) ;
	}
	
	public void processHistoricalPriceRequestEvent(HistoricalPriceRequestEvent event)
	{
		int index;
		String symbol = event.getSymbol() ;
		HistoricalPriceEvent retEvent = new HistoricalPriceEvent(null, event.getSender());
		retEvent.setSymbol(symbol);
//		Collections.sort(listSymbolData) ;
		synchronized(this)
		{
			index = Collections.binarySearch(listSymbolData, new SymbolData(symbol)) ;
			if (index < 0)
			{
				retEvent.setOk(false) ;
				retEvent.setMessage("Can't find requested symbol");
				sendEvent(retEvent) ;
				return ;
			}
		}
		String type   = event.getHistoryType() ;
		Date   start  = event.getStartDate() ;
		Date   end    = event.getEndDate() ;
		List<HistoricalPrice> listPrice = listSymbolData.get(index).getHistoricalPrice((byte)0x40, type, symbol, start, end) ;
		if (listPrice == null || listPrice.isEmpty())
		{
			retEvent.setOk(false) ;
			retEvent.setMessage("Get price list fail");
			sendEvent(retEvent) ;
			return ;
		}
		else
		{
			retEvent.setOk(true) ;
			retEvent.setHistoryType(event.getHistoryType());
			retEvent.setDataCount(listPrice.size());
			retEvent.setPriceList(listPrice);
			sendEvent(retEvent) ;
			return ;
		}
	}
	

	public void processSymbolEvent(SymbolEvent event)
	{
		ArrayList<SymbolInfo> symbolInfoList = (ArrayList<SymbolInfo>)event.getSymbolInfoList();
		ArrayList<String> MarketList = new ArrayList<String>();
		int index;
		for (SymbolInfo symbolInfo : symbolInfoList)
		{
			index = Collections.binarySearch(MarketList, symbolInfo.getMarket());
			if (index < 0)
			{
				MarketList.add(~index, symbolInfo.getMarket());
			}
		}
		String sqlcmd;
		for (String market : MarketList)
		{
			sqlcmd = String.format("DELETE FROM Symbol_Info WHERE MARKET='%s';", market);
			dbhnd.updateSQL(sqlcmd);
		}
		this.writeSymbolInfo(symbolInfoList) ;
	}
	

	public void processSymbolListSubscribeRequestEvent(SymbolListSubscribeRequestEvent event)
	{
		SymbolListSubscribeEvent retEvent = new SymbolListSubscribeEvent(null, event.getSender());
		SymbolListSubscribeType type = event.getType() ;
		retEvent.setUserID(event.getUserID());
		retEvent.setGroup(event.getGroup());
		retEvent.setMarket(event.getMarket());
		retEvent.setTxId(event.getTxId());
		retEvent.setType(event.getType());
		String user = event.getUserID();
		String market = event.getMarket();
		String group = event.getGroup();
		ArrayList<String> symbols = (ArrayList<String>) event.getSymbolList();
		switch(type)
		{
		case DEFAULT:
			requestDefaultSymbol(retEvent, market);
			break;
		case ADD:
			userAddSubscribeSymbol(retEvent, user, market, group, symbols);
			break;
		case ALLSYMBOL:
			userRequestAllSymbol(retEvent, market);
			break;
		case DELETE:
			userDelSubscribeSymbol(retEvent, user, market, group, symbols);
			break;
		case GROUPSYMBOL:
			userRequestGroupSymbol(retEvent, user, market, group);
			break;
		case SET:
			userSetGroupSymbol(retEvent, user, market, group, symbols);
			break;
		default:
			break;
		}
	}
	
	public void processSearchSymbolRequestEvent(SearchSymbolRequestEvent event)
	{
		SearchSymbolEvent retEvent = new SearchSymbolEvent(null, event.getSender(), event);
		SearchSymbolType type = event.getType();
		String sqlcmd = null;
		String searchkey = event.getKeyword();
		String strColumn = null;
		int page = event.getPage();
		int perpage = event.getSymbolPerPage();
		int totalpage = 0;
		int index;
		if (type == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage("Recieved null type");
			sendEvent(retEvent);
			return;
		}
		switch(type)
		{
		case CN_NAME:
			strColumn = "CN_NAME";
			break;
		case CODE:
			strColumn = "CODE";
			break;
		case EN_NAME:
			strColumn = "EN_NAME";
			break;
		case TW_NAME:
			strColumn = "TW_NAME";
			break;
		default:
			break;
		}
		sqlcmd = String.format("SELECT * FROM `Symbol_Info` WHERE `%s` LIKE '%%%s%%' ORDER BY `CODE`;", strColumn, searchkey);
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		try {
			while(rs.next())
			{
				symbolinfos.add(new SymbolInfo(rs.getString("MARKET"), 
											   rs.getString("CODE"), 
											   rs.getString("WINDCODE"), 
											   rs.getString("CN_NAME"),
											   rs.getString("EN_NAME"),
											   rs.getString("TW_NAME")));
			}
			totalpage = symbolinfos.size() / perpage;
			for (int ii = 0; ii < perpage; ii++)
			{
				index = (perpage * page + ii);
				if (index >= symbolinfos.size())
				{
					break ;
				}
				retsymbollist.add(symbolinfos.get(index));
			}
			retEvent.setSymbolinfo(retsymbollist);
			retEvent.setTotalpage(totalpage);
			retEvent.setOk(true);
		} catch (SQLException e) {

			retEvent.setSymbolinfo(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
		}
		finally
		{
			sendEvent(retEvent);
		}
	}
	
	public void requestDefaultSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		String sqlcmd;
		sqlcmd = String.format("SELECT * FROM `Symbol_Info` WHERE `MARKET`='%s';", market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		ArrayList<String> defaultSymbol = mapDefaultSymbol.get(market);
		if (defaultSymbol == null || defaultSymbol.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage("Can't find requested Market in default map");
			sendEvent(retEvent);
			return ;
		}
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		try
		{
			while(rs.next())
			{
				symbolinfos.add(new SymbolInfo(rs.getString("MARKET"), 
											   rs.getString("CODE"), 
											   rs.getString("WINDCODE"), 
											   rs.getString("CN_NAME"),
											   rs.getString("EN_NAME"),
											   rs.getString("TW_NAME")));
			}
			for (SymbolInfo symbolinfo : symbolinfos)
			{
				if (defaultSymbol.contains(symbolinfo.getCode()))
				{
					retsymbollist.add(symbolinfo);
				}
			}
			if (retsymbollist.isEmpty())
			{
				retEvent.setOk(false);
				retEvent.setMessage("Can't find requested symbol");
			}
			retEvent.setSymbolList(retsymbollist);
		} 
		catch (SQLException e) 
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
		}
		finally
		{
			sendEvent(retEvent);
		}
	}
	
	public void userAddSubscribeSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		String sqlcmd;
		for (String symbol : symbols)
		{
			sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
					" AND `GROUP`='%s' AND `MARKET`='%s' AND `CODE`='%s';", user, group, market, symbol) ;
			ResultSet rs = dbhnd.querySQL(sqlcmd);
			try 
			{
				if (rs.isLast() == false)
				{
					retEvent.setOk(false);
					retEvent.setMessage(String.format("Duplicate symbol %s", symbol));
				}
			} 
			catch (SQLException e) 
			{
				continue;
			}
		}
	}
	
	public void userDelSubscribeSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		String sqlcmd;
		for (String symbol : symbols)
		{
			sqlcmd = String.format("DELECT FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
					" AND `GROUP`='%s' AND `MARKET`='%s' AND `CODE`='%s';", user, group, market, symbol) ;
			dbhnd.updateSQL(sqlcmd);
		}
	}
	
	public void userRequestAllSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		String sqlcmd = String.format("SELECT * FROM `Symbol_Info` WHERE `MARKET`='%s';", market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		try
		{
			while(rs.next())
			{
				symbolinfos.add(new SymbolInfo(rs.getString("MARKET"), 
											   rs.getString("CODE"), 
											   rs.getString("WINDCODE"), 
											   rs.getString("CN_NAME"),
											   rs.getString("EN_NAME"),
											   rs.getString("TW_NAME")));
			}
			retEvent.setSymbolList(symbolinfos);
			retEvent.setOk(true);
		} 
		catch (SQLException e) 
		{
			symbolinfos.clear();
			retEvent.setSymbolList(symbolinfos);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
		}
		finally
		{
			sendEvent(retEvent);
		}
	}
	
	public void userRequestGroupSymbol(SymbolListSubscribeEvent retEvent, String user, String market, String group)
	{
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		String sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `GROUP`='%s' AND `MARKET`='%s';", 
				user, group, market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		try 
		{
			while(rs.next())
			{
				symbolinfos.add(new SymbolInfo(rs.getString("MARKET"), 
											   rs.getString("CODE"), 
											   rs.getString("WINDCODE"), 
											   rs.getString("CN_NAME"),
											   rs.getString("EN_NAME"),
											   rs.getString("TW_NAME")));
			}
			retEvent.setSymbolList(symbolinfos);
			retEvent.setOk(true);
		} 
		catch (SQLException e) 
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
		}
		finally
		{
			sendEvent(retEvent);
		}
	}
	

	public void userSetGroupSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		if (user == null || market == null || group == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage("Recieved null argument");
		}
		String sqlcmd ;
		sqlcmd = String.format("DELETE FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
				" AND `GROUP`='%s' AND `MARKET`='%s';", user, group, market) ;
		dbhnd.updateSQL(sqlcmd);
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		sqlcmd = String.format("SELECT * FROM `Symbol_Info` WHERE `MARKET`='%s';", market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		try
		{
			while(rs.next())
			{
				symbolinfos.add(new SymbolInfo(rs.getString("MARKET"), 
											   rs.getString("CODE"), 
											   rs.getString("WINDCODE"), 
											   rs.getString("CN_NAME"),
											   rs.getString("EN_NAME"),
											   rs.getString("TW_NAME")));
			}
			sqlcmd = String.format("INSERT INTO Subscribe_Symbol_Info (`USER_ID`,`GROUP`,`MARKET`,`CODE`,`WINDCODE`,`EN_NAME`,`CN_NAME`,`TW_NAME`) VALUES");
			boolean first = true;
			for (SymbolInfo symbolinfo : symbolinfos)
			{
				if (symbols.contains(symbolinfo.getCode()))
				{
					if (first == false)
					{
						sqlcmd += "," ;
					}
					retsymbollist.add(symbolinfo);
					sqlcmd += String.format("('%s','%s','%s',", user, group, market);
					sqlcmd += (symbolinfo.getCode() == null) ? "null," : String.format("'%s',", symbolinfo.getCode());
					sqlcmd += (symbolinfo.getWindCode() == null) ? "null," : String.format("'%s',", symbolinfo.getWindCode());
					sqlcmd += (symbolinfo.getEnName() == null) ? "null," : String.format("'%s',", symbolinfo.getEnName());
					sqlcmd += (symbolinfo.getCnName() == null) ? "null," : String.format("'%s',", symbolinfo.getCnName());
					sqlcmd += (symbolinfo.getTwName() == null) ? "null" : String.format("'%s'", symbolinfo.getTwName());
					sqlcmd += ")";
					first = false;
				}
			}
			sqlcmd += ";" ;
			if (retsymbollist.isEmpty())
			{
				retsymbollist.clear();
				retEvent.setOk(false);
				retEvent.setMessage("Can't find requested symbol");
			}
			else
			{
				dbhnd.updateSQL(sqlcmd);
				retEvent.setOk(true);
			}
			retEvent.setSymbolList(retsymbollist);
		} 
		catch (SQLException e) 
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
		}
		finally
		{
			sendEvent(retEvent);
		}
	}
	
	public void writeToTick(Quote quote)
	{
		Calendar calStamp = Calendar.getInstance() ;
		Calendar calSent = Calendar.getInstance() ;
		calStamp.setTime(quote.getTimeStamp()) ;
		calSent.setTime(quote.getTimeSent()) ;
		
		String strFile = String.format("./DAT/%s/%s.TCK", 
				getTradedate(), quote.getSymbol()) ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss") ;
		double bid = quote.getBid() ;
		double ask = quote.getAsk() ;
		double price = (bid + ask) / 2 ;
		File file = new File(strFile) ;
		String strOut = String.format("%s|%.5f|%.5f|%.5f|%f|%f|%.5f|%f|%.5f|%.5f|%.5f|%.5f|" + 
				"%f|%s|%s", 
				quote.getSymbol(), price, bid, ask, quote.getBidVol(), 
				quote.getAskVol(), quote.getLast(), quote.getLastVol(), quote.getHigh(), 
				quote.getLow(), quote.getOpen(), quote.getClose(), quote.getTotalVolume(),
				sdf.format(quote.getTimeStamp()), sdf.format(quote.getTimeSent())) ;
        try
        {
            file.getParentFile().mkdirs();
            if (!file.exists())
            {
                file.createNewFile() ;
            }
            BufferedWriter fileOut = new BufferedWriter(new FileWriter(file, true)) ;
            fileOut.write(strOut) ;
            fileOut.newLine() ;
            fileOut.close() ;
        }
        catch (IOException e)
        {
            log.error(e.toString(), e);
        }
	}
	
	public void writeSubscribeSymbolInfo(String user, String group, ArrayList<SymbolInfo> symbolInfoList)
	{
		String sqlcmd = "INSERT IGNORE INTO Symbol_Info (MARKET,CODE,WINDCODE,EN_NAME,CN_NAME,TW_NAME) VALUES";
		boolean first = true;
		for(SymbolInfo symbolinfo : symbolInfoList)
		{
			if (first == false)
			{
				sqlcmd += "," ;
			}
			sqlcmd += "(";
			sqlcmd += (symbolinfo.getMarket() == null) ? "null," : String.format("'%s',", symbolinfo.getMarket());
			sqlcmd += (symbolinfo.getCode() == null) ? "null," : String.format("'%s',", symbolinfo.getCode());
			sqlcmd += (symbolinfo.getWindCode() == null) ? "null," : String.format("'%s',", symbolinfo.getWindCode());
			sqlcmd += (symbolinfo.getEnName() == null) ? "null," : String.format("'%s',", symbolinfo.getEnName());
			sqlcmd += (symbolinfo.getCnName() == null) ? "null," : String.format("'%s',", symbolinfo.getCnName());
			sqlcmd += (symbolinfo.getTwName() == null) ? "null," : String.format("'%s'", symbolinfo.getTwName());
			sqlcmd += ")";
			if (first == true)
			{
				first = false ;
			}
		}
		sqlcmd += ";" ;
		dbhnd.updateSQL(sqlcmd);
	}
	
	public void writeSymbolInfo(ArrayList<SymbolInfo> symbolInfoList)
	{
		String sqlcmd = "INSERT IGNORE INTO Symbol_Info (MARKET,CODE,WINDCODE,EN_NAME,CN_NAME,TW_NAME) VALUES";
		boolean first = true;
		for(SymbolInfo symbolinfo : symbolInfoList)
		{
			if (first == false)
			{
				sqlcmd += "," ;
			}
			sqlcmd += "(";
			sqlcmd += (symbolinfo.getMarket() == null) ? "null," : String.format("'%s',", symbolinfo.getMarket());
			sqlcmd += (symbolinfo.getCode() == null) ? "null," : String.format("'%s',", symbolinfo.getCode());
			sqlcmd += (symbolinfo.getWindCode() == null) ? "null," : String.format("'%s',", symbolinfo.getWindCode());
			sqlcmd += (symbolinfo.getEnName() == null) ? "null," : String.format("'%s',", symbolinfo.getEnName());
			sqlcmd += (symbolinfo.getCnName() == null) ? "null," : String.format("'%s',", symbolinfo.getCnName());
			sqlcmd += (symbolinfo.getTwName() == null) ? "null," : String.format("'%s'", symbolinfo.getTwName());
			sqlcmd += ")";
			if (first == true)
			{
				first = false ;
			}
		}
		sqlcmd += ";" ;
		dbhnd.updateSQL(sqlcmd);
	}
	
	public void onCallRefData()
	{
		ArrayList<RefData> refList = (ArrayList<RefData>)refDataManager.getRefDataList();
		if (refList.isEmpty())
		{
			return ;
		}

		String sqlcmd = "INSERT IGNORE INTO Symbol_Info (MARKET,CODE,EN_NAME,CN_NAME) VALUES";
		boolean first = true;
		synchronized(this)
		{
			for(RefData refdata : refList)
			{
				SymbolData symbolData = new SymbolData(refdata.getSymbol(), this) ;
				int index = Collections.binarySearch(listSymbolData, symbolData) ;
				if (index < 0)
				{
					listSymbolData.add(~index, symbolData) ;
					index = ~index ;
				}
				if (first == false)
				{
					sqlcmd += "," ;
				}
				sqlcmd += "(";
				sqlcmd += (refdata.getExchange() == null) ? "null," : String.format("'%s',", refdata.getExchange());
				sqlcmd += (refdata.getSymbol() == null) ? "null," : String.format("'%s',", refdata.getSymbol());
				sqlcmd += (refdata.getENDisplayName() == null) ? "null," : String.format("'%s',", refdata.getENDisplayName());
				sqlcmd += (refdata.getCNDisplayName() == null) ? "null" : String.format("'%s'", refdata.getCNDisplayName());
				sqlcmd += ")";
				if (first == true)
				{
					first = false ;
				}
			}
		}
		sqlcmd += ";" ;
		dbhnd.updateSQL(sqlcmd);
	}
	
	public void resetStatement()
	{
		synchronized(this)
		{
			this.listSymbolData.clear();
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.add(Calendar.HOUR_OF_DAY, -2);
		nTickCount = getTickCount() ;
		nOpen = (open/100) * 60 + (open%100) ;
		nPreOpen = (preopen/100) * 60 + (preopen%100) ;
		nClose = (close/100) * 60 + (close%100) ;
	}
	
	public void setSessionType(MarketSessionType sessionType) {
		synchronized(this)
		{
			if (this.sessionType == MarketSessionType.OPEN)
			{
				if (sessionType == MarketSessionType.CLOSE)
				{
					for (SymbolData symbol : listSymbolData)
					{
						symbol.insertSQLDate((byte)0x40, "D");
						symbol.insertSQLDate((byte)0x40, "W");
						symbol.insertSQLDate((byte)0x40, "M");
						symbol.insertSQLTick((byte)0x40, "1");
						symbol.insertSQLTick((byte)0x40, "R");
						symbol.insertSQLTick((byte)0x40, "A");
						symbol.insertSQLTick((byte)0x40, "Q");
						symbol.insertSQLTick((byte)0x40, "H");
						symbol.insertSQLTick((byte)0x40, "6");
						symbol.insertSQLTick((byte)0x40, "T");
					}
				}
				else if (sessionType == MarketSessionType.PREOPEN)
				{
					resetStatement() ;
				}
			}
			else if (this.sessionType == MarketSessionType.CLOSE)
			{
				if (sessionType == MarketSessionType.OPEN 
						|| sessionType == MarketSessionType.PREOPEN)
				{
					resetStatement() ;
				}
			}
			this.sessionType = sessionType;
		}
	}
	
	public void sendMDEvent(RemoteAsyncEvent event) {
		RemoteAsyncEvent remoteEvent = (RemoteAsyncEvent)event;
		try {
			eventManagerMD.sendRemoteEvent(remoteEvent);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	public void sendEvent(RemoteAsyncEvent event) {
		RemoteAsyncEvent remoteEvent = (RemoteAsyncEvent)event;
		try {
			eventManager.sendRemoteEvent(remoteEvent);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public AsyncEventProcessor getEventProcessorMD() {
		return eventProcessorMD;
	}
	public void setEventProcessorMD(AsyncEventProcessor eventProcessor) {
		this.eventProcessorMD = eventProcessor;
	}	
	public void requestMarketSession()
	{
		String receiver = String.format("%s.%s.%s", systemInfoMD.getEnv(), systemInfoMD.getCategory(), systemInfoMD.getId()) ;
		sendMDEvent(new MarketSessionRequestEvent(null, receiver)) ;
	}	
	public void requestSymbolList()
	{
		String receiver = String.format("%s.%s.%s", systemInfoMD.getEnv(), systemInfoMD.getCategory(), systemInfoMD.getId()) ;
		sendMDEvent(new SymbolRequestEvent(null, receiver)) ;
	}
	@Override
	public void init() throws Exception {
		log.info("Initialising...");
		Class.forName("com.mysql.jdbc.Driver");
		dbhnd = new DBHandler(host, user, pass, database) ;
		resetStatement() ;
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("CentralDBProcessor");
				
		// subscribe to events
		eventProcessorMD.setHandler(this);
		eventProcessorMD.init();
		if(eventProcessorMD.getThread() != null)
			eventProcessorMD.getThread().setName("CentralDBProcessor-MD");
		refDataManager.init();
		requestMarketSession() ;
//		requestSymbolList() ;
		onCallRefData();
		userSymbolList = new HashMap<String, ArrayList<String>>();
	}
	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessorMD.uninit();
		eventProcessor.uninit();
	}

	public int getOpen() {
		return open;
	}
	public void setOpen(int open) {
		this.open = open;
	}
	public int getPreopen() {
		return preopen;
	}
	public void setPreopen(int preopen) {
		this.preopen = preopen;
	}
	public int getClose() {
		return close;
	}
	public void setClose(int close) {
		this.close = close;
	}
	public String getTradedate() {
		return tradedate;
	}
	
}
