package com.cyanspring.info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.info.CentralDbReadyEvent;
import com.cyanspring.common.event.info.CentralDbSubscribeEvent;
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
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.event.AsyncEventProcessor;


public class CentralDbProcessor implements IPlugin
{
	private static final Logger log = LoggerFactory
			.getLogger(CentralDbProcessor.class);

	private String driverClass;
	private String jdbcUrl;
	private int open ;
	private int preopen ;
	private int close ;
	private int    nOpen ;
	private int    nClose ;
	private int    nPreOpen ;
	private ArrayList<String> preSubscriptionList;
	
	private int    nTickCount ;
	private MarketSessionType sessionType = null ;
	private String tradedate ;
	static boolean isStartup = true;
	private Queue<QuoteEvent> quoteBuffer;

	// for checking SQL connect 
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long timeInterval = 60000;
	private long checkSQLInterval = 10 * 60 * 1000;
	private long checkSQLTimer = 0;
	
	private HashMap<String, ArrayList<String>> mapDefaultSymbol = new HashMap<String, ArrayList<String>>();
	private ArrayList<SymbolData> listSymbolData = new ArrayList<SymbolData>();
	private ArrayList<SymbolInfo> defaultSymbolInfo = new ArrayList<SymbolInfo>();
	private ArrayList<SymbolInfo> refSymbolInfo = new ArrayList<SymbolInfo>();
	private ArrayList<String> appServIDList = new ArrayList<String>();
	DBHandler dbhnd ;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	private SystemInfo systemInfoMD;
	
	@Autowired
	private RefDataManager refDataManager;
	
	@Autowired
	private TickTableManager tickTableManager;
	
	@Autowired
	ScheduleManager scheduleManager;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor(){

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PriceHighLowRequestEvent.class, null);
			subscribeToEvent(SearchSymbolRequestEvent.class, null);
			subscribeToEvent(SymbolListSubscribeRequestEvent.class, null);
			subscribeToEvent(HistoricalPriceRequestEvent.class, null);
			subscribeToEvent(AsyncTimerEvent.class, null);
			subscribeToEvent(CentralDbSubscribeEvent.class, null);
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
		int curTime = (inputTime < this.nOpen) ? inputTime + 1440 : inputTime ;
		if (overDay)
		{
			if (curTime < nPreOpen && curTime >= nClose)
			{
				return nTickCount - 1;
			}
			else if (curTime >= nPreOpen)
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
			if (curTime >= nClose)
			{
				return nTickCount - 1;
			}
			else 
			{
				return curTime - nOpen ;
			}
		}
	}

	public void processCentralDbSubscribeEvent(CentralDbSubscribeEvent event) 
	{
		int index = Collections.binarySearch(appServIDList, event.getSender());
		if (index < 0)
		{
			appServIDList.add(~index, event.getSender());
		}
		if (!isStartup)
			sendCentralReady(event.getSender());
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) 
	{
		if (!isStartup)
		{
			checkSQLTimer += timeInterval;
			if (checkSQLTimer >= checkSQLInterval)
			{
				dbhnd.checkSQLConnect();
				checkSQLTimer = 0;
			}
		}
		resetSymbolDataStat();
	}
	
	public void resetSymbolDataStat()
	{
		for (SymbolData symboldata : listSymbolData)
		{
			symboldata.setWriteMin(true);
		}
	}
	
	public void processQuoteEvent(QuoteEvent event)
	{
		if (sessionType == MarketSessionType.CLOSE)
		{
			return;
		}
		if (isStartup)
		{
			quoteBuffer.offer(event);
			return;
		}
		Quote quote;
		while(quoteBuffer.isEmpty() == false)
		{
			quote = quoteBuffer.poll().getQuote() ;
			writeToTick(quote) ;
			SymbolData symbolData = new SymbolData(quote.getSymbol()) ;
			synchronized(this)
			{
				int index = Collections.binarySearch(listSymbolData, symbolData) ;
				if (index < 0)
				{
					listSymbolData.add(~index, new SymbolData(quote.getSymbol(), Default.getMarket(), this)) ;
					index = ~index ;
				}
				listSymbolData.get(index).setPrice(quote);
			}
			log.debug("Quote: " + quote);
		}
		quote = event.getQuote() ;
		writeToTick(quote) ;
		SymbolData symbolData = new SymbolData(quote.getSymbol()) ;
		synchronized(this)
		{
			int index = Collections.binarySearch(listSymbolData, symbolData) ;
			if (index < 0)
			{
				listSymbolData.add(~index, new SymbolData(quote.getSymbol(), Default.getMarket(), this)) ;
				index = ~index ;
			}
			listSymbolData.get(index).setPrice(quote);
		}
		log.debug("Quote: " + quote);
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event)
	{
		log.info("Process MarketSession: " + event.getTradeDate() + " " + event.getSession());
		this.tradedate = event.getTradeDate() ;
		setSessionType(event.getSession(), event.getMarket()) ;
	}
	
	public void processPriceHighLowRequestEvent(PriceHighLowRequestEvent event)
	{
		log.info("Process Price High Low Request");
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
			log.debug("Process Price High Low Request fail: No requested symbol is find");
		}
		else
		{
			retEvent.setOk(true);
			retEvent.setListHighLow(phlList);
			log.info("Process Price High Low Request success Symbol: " + phlList.size());
		}
		sendEvent(retEvent) ;
	}
	
	public void processHistoricalPriceRequestEvent(HistoricalPriceRequestEvent event)
	{
		int index;
		String symbol = event.getSymbol() ;
		HistoricalPriceEvent retEvent = new HistoricalPriceEvent(null, event.getSender());
		retEvent.setSymbol(symbol);
		log.info("Process Historical Price Request");
		String type   = event.getHistoryType() ;
		Date   start  = event.getStartDate() ;
		Date   end    = event.getEndDate() ;
		List<HistoricalPrice> listPrice = null;
		log.debug("Process Historical Price Request Symbol: " + symbol + " Type: " + type + " Start: " +  start + " End: " + end);
		synchronized(this)
		{
			index = Collections.binarySearch(listSymbolData, new SymbolData(symbol)) ;
			if (index < 0)
			{
				retEvent.setOk(false) ;
				retEvent.setMessage("Can't find requested symbol");
				sendEvent(retEvent) ;
				log.debug("Process Historical Price Request fail: Can't find requested symbol");
				return ;
			}
		}
		listPrice = listSymbolData.get(index).getHistoricalPrice(type, symbol, start, end) ;
		if (listPrice == null || listPrice.isEmpty())
		{
			retEvent.setOk(false) ;
			retEvent.setMessage("Get price list fail");
			sendEvent(retEvent) ;
			log.debug("Process Historical Price Request fail: Get price list fail");
			return ;
		}
		else
		{
			retEvent.setOk(true) ;
			retEvent.setHistoryType(event.getHistoryType());
			retEvent.setDataCount(listPrice.size());
			retEvent.setPriceList(listPrice);
			sendEvent(retEvent) ;
			log.info("Process Historical Price Request success Symbol: " + listPrice.size());
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
		retEvent.setQueryType(event.getQueryType());
		String user = event.getUserID();
		String market = event.getMarket();
		String group = event.getGroup();
		ArrayList<String> symbols = (ArrayList<String>) event.getSymbolList();
		switch(type)
		{
		case DEFAULT:
			log.info("Process Symbol List Subscribe Type: DEFAULT Market: " + market);
			requestDefaultSymbol(retEvent, market);
			break;
		case ADD:
			log.info("Process Symbol List Subscribe Type: ADD User: " + user + " Market: " + market + " Group: " + group);
			userAddSubscribeSymbol(retEvent, user, market, group, symbols);
			break;
		case ALLSYMBOL:
			log.info("Process Symbol List Subscribe Type: ALLSYMBOL Market: " + market);
			userRequestAllSymbol(retEvent, market);
			break;
		case DELETE:
			log.info("Process Symbol List Subscribe Type: DELETE User: " + user + " Market: " + market + " Group: " + group);
			userDelSubscribeSymbol(retEvent, user, market, group, symbols);
			break;
		case GROUPSYMBOL:
			log.info("Process Symbol List Subscribe Type: GROUPSYMBOL User: " + user + " Market: " + market + " Group: " + group);
			userRequestGroupSymbol(retEvent, user, market, group);
			break;
		case SET:
			log.info("Process Symbol List Subscribe Type: SET User: " + user + " Market: " + market + " Group: " + group);
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
		log.info("Process Search Symbol Request Type: " + type + " Keyword: " + searchkey);
		if (type == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage("Recieved null type");
			sendEvent(retEvent);
			log.debug("Process Search Symbol Request fail: Recieved null type");
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
			SymbolInfo symbolinfo = null;
			while(rs.next())
			{
				symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
				symbolinfo.setWindCode(rs.getString("WINDCODE"));
				symbolinfo.setHint(rs.getString("HINT"));
				symbolinfo.setCnName(rs.getString("CN_NAME"));
				symbolinfo.setEnName(rs.getString("EN_NAME"));
				symbolinfo.setTwName(rs.getString("TW_NAME"));
				symbolinfo.setJpName(rs.getString("JP_NAME"));
				symbolinfo.setKrName(rs.getString("KR_NAME"));
				symbolinfo.setEsName(rs.getString("ES_NAME"));
				symbolinfos.add(symbolinfo);
											   
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
			log.info("Process Search Symbol Request success Symbol: " + retsymbollist.size());
			sendEvent(retEvent);
		} catch (SQLException e) {

			retEvent.setSymbolinfo(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
			log.debug("Process Search Symbol Request fail: " + e.toString());
			sendEvent(retEvent);
		}
	}
	
	public void requestDefaultSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<String> defaultSymbol = /*mapDefaultSymbol.get(market)*/preSubscriptionList;
		if (defaultSymbol == null || defaultSymbol.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage("Can't find requested Market in default map");
			log.debug("Process Request Default Symbol fail: Can't find requested Market in default map");
			sendEvent(retEvent);
			return ;
		}
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		if (defaultSymbolInfo == null || defaultSymbolInfo.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage("Default SymbolInfo is empty");
			log.debug("Process Request Default Symbol fail: Default SymbolInfo is empty");
			sendEvent(retEvent);
			return ;
		}
		else
		{
			retsymbollist.addAll(defaultSymbolInfo);
		}
		if (retsymbollist.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage("Can't find requested symbol");
		}
		else
		{
			retEvent.setOk(true);
		}
		retEvent.setSymbolList(retsymbollist);
		log.info("Process Request Default Symbol success Symbol: " + retsymbollist.size());
		sendEvent(retEvent);
	}
	
	public void userAddSubscribeSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		String sqlcmd = String.format("INSERT INTO `Subscribe_Symbol_Info` (`USER_ID`,`GROUP`,`MARKET`,`CODE`,`HINT`,`WINDCODE`,`EN_NAME`,`CN_NAME`,`TW_NAME`,`JP_NAME`,`KR_NAME`,`ES_NAME`) VALUES");
		String sqlselect;
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		boolean first = true;
		for (String symbol : symbols)
		{
			sqlselect = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
					" AND `GROUP`='%s' AND `MARKET`='%s' AND `CODE`='%s';", user, group, market, symbol) ;
			ResultSet rs = dbhnd.querySQL(sqlselect);
			try 
			{
				if (rs.next())
				{
					retEvent.setOk(false);
					retEvent.setMessage(String.format("Duplicate symbol %s", symbol));
					log.debug("Process Add Symbol fail Duplicated symbol: " + symbol);
					sendEvent(retEvent);
					return;
				}
				else
				{
					SymbolInfo symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
					int index = Collections.binarySearch(refSymbolInfo, symbolinfo);
					if (index >= 0)
					{
						symbolinfo = refSymbolInfo.get(index);
						retsymbollist.add(symbolinfo);
						if (first == false)
						{
							sqlcmd += "," ;
						}
						sqlcmd += String.format("('%s','%s','%s',", user, group, market);
						sqlcmd += (symbolinfo.getCode() == null) ? "null," : String.format("'%s',", symbolinfo.getCode());
						sqlcmd += (symbolinfo.getHint() == null) ? "null," : String.format("'%s',", symbolinfo.getHint());
						sqlcmd += (symbolinfo.getWindCode() == null) ? "null," : String.format("'%s',", symbolinfo.getWindCode());
						sqlcmd += (symbolinfo.getEnName() == null) ? "null," : String.format("'%s',", symbolinfo.getEnName());
						sqlcmd += (symbolinfo.getCnName() == null) ? "null," : String.format("'%s',", symbolinfo.getCnName());
						sqlcmd += (symbolinfo.getTwName() == null) ? "null," : String.format("'%s',", symbolinfo.getTwName());
						sqlcmd += (symbolinfo.getJpName() == null) ? "null," : String.format("'%s',", symbolinfo.getJpName());
						sqlcmd += (symbolinfo.getKrName() == null) ? "null," : String.format("'%s',", symbolinfo.getKrName());
						sqlcmd += (symbolinfo.getEsName() == null) ? "null" : String.format("'%s'", symbolinfo.getEsName());
						sqlcmd += ")";
						first = false;
					}
				}
			} 
			catch (SQLException e) 
			{
				continue;
			}
		}
		dbhnd.updateSQL(sqlcmd);
		retEvent.setSymbolList(retsymbollist);
		retEvent.setOk(true);
		log.info("Process Add Symbol success added Symbol: " + retsymbollist.size());
		sendEvent(retEvent);
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
			sqlcmd = String.format("DELETE FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
					" AND `GROUP`='%s' AND `MARKET`='%s' AND `CODE`='%s';", user, group, market, symbol) ;
			dbhnd.updateSQL(sqlcmd);
		}
		retEvent.setOk(true);
		log.info("Process Delete Symbol success deleted Symbol: " + symbols.size());
		sendEvent(retEvent);
		
	}
	
	public void userRequestAllSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<SymbolInfo> retSymbolInfo = new ArrayList<SymbolInfo>();
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolinfo.getMarket() != null && market != null && symbolinfo.getMarket().equals(market))
				retSymbolInfo.add(symbolinfo);
		}
		retEvent.setSymbolList(retSymbolInfo);
		retEvent.setOk(true);
		log.info("Process Request All Symbol success Symbol: " + retSymbolInfo.size());
		sendEvent(retEvent);
	}
	
	public void userRequestGroupSymbol(SymbolListSubscribeEvent retEvent, String user, String market, String group)
	{
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		String sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `GROUP`='%s' AND `MARKET`='%s' ORDER BY `NO`;", 
				user, group, market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		try 
		{
			SymbolInfo symbolinfo = null;
			while(rs.next())
			{
				symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
				symbolinfo.setWindCode(rs.getString("WINDCODE"));
				symbolinfo.setHint(rs.getString("HINT"));
				symbolinfo.setCnName(rs.getString("CN_NAME"));
				symbolinfo.setEnName(rs.getString("EN_NAME"));
				symbolinfo.setTwName(rs.getString("TW_NAME"));
				symbolinfo.setJpName(rs.getString("JP_NAME"));
				symbolinfo.setKrName(rs.getString("KR_NAME"));
				symbolinfo.setEsName(rs.getString("ES_NAME"));
				symbolinfos.add(symbolinfo);
			}
			if (symbolinfos.isEmpty())
			{
				requestDefaultSymbol(retEvent, market);
				return;
			}
			retEvent.setSymbolList(symbolinfos);
			retEvent.setOk(true);
			log.info("Process Request Group Symbol success Symbol: " + symbolinfos.size());
			sendEvent(retEvent);
		} 
		catch (SQLException e) 
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
			log.debug("Process Request Group Symbol fail: " + e.toString());
			sendEvent(retEvent);
		}
	}
	

	public void userSetGroupSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		if (symbols == null || symbols.isEmpty())
		{
			symbols = preSubscriptionList;
		}
		if (user == null || market == null || group == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage("Recieved null argument");
			log.debug("Process Request Group Symbol fail: Recieved null argument");
		}
		String sqlcmd ;
		sqlcmd = String.format("DELETE FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
				" AND `GROUP`='%s' AND `MARKET`='%s';", user, group, market) ;
		dbhnd.updateSQL(sqlcmd);
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		try
		{
			for (SymbolInfo symbolinfo : refSymbolInfo)
			{
				if (symbolinfo.getMarket() != null && symbolinfo.getMarket().equals(market))
					symbolinfos.add(symbolinfo);
			}
			sqlcmd = String.format("INSERT INTO Subscribe_Symbol_Info (`USER_ID`,`GROUP`,`MARKET`,`CODE`,`HINT`,`WINDCODE`,`EN_NAME`,`CN_NAME`,`TW_NAME`,`JP_NAME`,`KR_NAME`,`ES_NAME`,`NO`) VALUES");
			boolean first = true;
			int No = 0;
			for (SymbolInfo syminfo : symbolinfos)
			{
				No = symbols.indexOf(syminfo.getCode());
				if (No >= 0)
				{
					if (first == false)
					{
						sqlcmd += "," ;
					}
					retsymbollist.add(syminfo);
					sqlcmd += String.format("('%s','%s','%s',", user, group, market);
					sqlcmd += (syminfo.getCode() == null) ? "null," : String.format("'%s',", syminfo.getCode());
					sqlcmd += (syminfo.getHint() == null) ? "null," : String.format("'%s',", syminfo.getHint());
					sqlcmd += (syminfo.getWindCode() == null) ? "null," : String.format("'%s',", syminfo.getWindCode());
					sqlcmd += (syminfo.getEnName() == null) ? "null," : String.format("'%s',", syminfo.getEnName());
					sqlcmd += (syminfo.getCnName() == null) ? "null," : String.format("'%s',", syminfo.getCnName());
					sqlcmd += (syminfo.getTwName() == null) ? "null," : String.format("'%s',", syminfo.getTwName());
					sqlcmd += (syminfo.getJpName() == null) ? "null," : String.format("'%s',", syminfo.getJpName());
					sqlcmd += (syminfo.getKrName() == null) ? "null," : String.format("'%s',", syminfo.getKrName());
					sqlcmd += (syminfo.getEsName() == null) ? "null," : String.format("'%s',", syminfo.getEsName());
					sqlcmd += No;
					sqlcmd += ")";
					first = false;
				}
			}
			sqlcmd += ";" ;
			if (retsymbollist.size() != symbols.size())
			{
				retEvent.setSymbolList(null);
				retEvent.setOk(false);
				retEvent.setMessage("Can't find requested symbol");
				log.debug("Process Request Group Symbol fail: Can't find requested symbol");
			}
			else
			{
				dbhnd.updateSQL(sqlcmd);
				retsymbollist.clear();
				sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `GROUP`='%s' AND `MARKET`='%s' ORDER BY `NO`;", 
						user, group, market) ;
				ResultSet rs = dbhnd.querySQL(sqlcmd);
				SymbolInfo symbolinfo;
				while(rs.next())
				{
					symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
					symbolinfo.setWindCode(rs.getString("WINDCODE"));
					symbolinfo.setHint(rs.getString("HINT"));
					symbolinfo.setCnName(rs.getString("CN_NAME"));
					symbolinfo.setEnName(rs.getString("EN_NAME"));
					symbolinfo.setTwName(rs.getString("TW_NAME"));
					symbolinfo.setJpName(rs.getString("JP_NAME"));
					symbolinfo.setKrName(rs.getString("KR_NAME"));
					symbolinfo.setEsName(rs.getString("ES_NAME"));
					retsymbollist.add(symbolinfo);
				}
				if (symbolinfos.isEmpty())
				{
					requestDefaultSymbol(retEvent, market);
					return;
				}
				retEvent.setSymbolList(retsymbollist);
				retEvent.setOk(true);
				log.info("Process Request Group Symbol success Symbol: " + symbolinfos.size());
			}
			sendEvent(retEvent);
		} 
		catch (SQLException e) 
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(e.toString());
			log.debug("Process Request Group Symbol fail: " + e.toString());
			sendEvent(retEvent);
		}
	}
	
	public void writeToTick(Quote quote)
	{
		if (this.tradedate == null)
		{
			return;
		}
		
		String strFile = String.format("./DAT/%s/%s.TCK", 
				getTradedate(), quote.getSymbol()) ;
		File file = new File(strFile) ;
        file.getParentFile().mkdirs();
        boolean fileExist = file.exists();
        try
        {
            FileOutputStream fos = new FileOutputStream(file, true);
            ObjectOutputStream oos = (!fileExist) ? new ObjectOutputStream(fos) : new AppendingObjectOutputStream(fos); 
        	oos.writeObject(quote);
            oos.flush();
            oos.close();
            fos.close();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
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
		log.info("Call refData start");
		try 
		{
			refDataManager.init();
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage(), e);
		}
		ArrayList<RefData> refList = (ArrayList<RefData>)refDataManager.getRefDataList();
		if (refList.isEmpty() || this.listSymbolData.isEmpty() == false)
		{
			log.warn("refData is empty: " + refList.isEmpty() + " or already read");
			return ;
		}
		synchronized(this)
		{
			PrintWriter outSymbol;
			ArrayList<String> marketList = new ArrayList<String>();
			try 
			{
				outSymbol = new PrintWriter(new BufferedWriter(new FileWriter("Symbol")));
				defaultSymbolInfo.clear();
				for (int ii = 0; ii < preSubscriptionList.size(); ii++)
					defaultSymbolInfo.add(null);
				refSymbolInfo.clear();
				SymbolInfo symbolinfo = null;
				for(RefData refdata : refList)
				{
					if (refdata.getExchange() == null) continue;

					if (!marketList.contains(refdata.getExchange()))
					{
						dbhnd.checkMarketExist(refdata.getExchange());
						marketList.add(refdata.getExchange());
					}
					SymbolData symbolData = new SymbolData(refdata.getSymbol(), refdata.getExchange(), this) ;
					symbolinfo = new SymbolInfo(refdata.getExchange(), refdata.getSymbol());
					symbolinfo.setWindCode(null);
					symbolinfo.setHint(refdata.getRefSymbol());
					symbolinfo.setCnName(refdata.getCNDisplayName());
					symbolinfo.setEnName(refdata.getENDisplayName());
					symbolinfo.setTwName(refdata.getTWDisplayName());
					symbolinfo.setJpName(refdata.getENDisplayName());
					symbolinfo.setKrName(refdata.getENDisplayName());
					symbolinfo.setEsName(refdata.getENDisplayName());
					symbolinfo.setLotSize(refdata.getLotSize());
					symbolinfo.setTickTable(refdata.getTickTable());
					if (refdata.getExchange() != null && refdata.getExchange().equals("FX"))
					{
						outSymbol.println(refdata.getSymbol());
					}
					int index = Collections.binarySearch(listSymbolData, symbolData) ;
					if (index < 0)
					{
						listSymbolData.add(~index, new SymbolData(refdata.getSymbol(), refdata.getExchange(), this)) ;
						index = ~index ;
					}
					index = Collections.binarySearch(refSymbolInfo, symbolinfo);
					if (index < 0)
					{
						refSymbolInfo.add(~index, symbolinfo);
					}
					if (preSubscriptionList.contains(refdata.getSymbol()))
					{
						index = preSubscriptionList.indexOf(refdata.getSymbol());
						defaultSymbolInfo.remove(index);
						defaultSymbolInfo.add(index, symbolinfo);
					}
				}
				outSymbol.close();
			} 
			catch (IOException e) 
			{
				log.error(e.getMessage(), e);;
			}
			for (String appserv : appServIDList)
			{
				sendCentralReady(appserv);
			}
		}
		isStartup = false;
		log.info("Call refData finish");
	}
	
	public void resetStatement()
	{
		isStartup = true;
		synchronized(this)
		{
			log.debug("Clear listSymbolData " + listSymbolData.size() + " symbols");
			this.listSymbolData.clear();
			this.quoteBuffer.clear();
			this.refSymbolInfo.clear();
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")) ;
		cal.add(Calendar.HOUR_OF_DAY, -2);
		nOpen = (open/100) * 60 + (open%100) ;
		nPreOpen = (preopen/100) * 60 + (preopen%100) ;
		nClose = (close/100) * 60 + (close%100) ;
		nTickCount = getTickCount() ;
	}
	
	public void setSessionType(MarketSessionType sessionType, String market) {
		if (this.sessionType == MarketSessionType.OPEN)
		{
			if (sessionType == MarketSessionType.CLOSE)
			{
				for (SymbolData symbol : listSymbolData)
				{
					if (symbol.getMarket() == null || symbol.getMarket().equals(market) == false)
					{
						continue;
					}
					symbol.insertSQLDate("D");
					symbol.insertSQLDate("W");
					symbol.insertSQLDate("M");
					symbol.insertSQLTick("1");
					symbol.insertSQLTick("R");
					symbol.insertSQLTick("A");
					symbol.insertSQLTick("Q");
					symbol.insertSQLTick("H");
					symbol.insertSQLTick("6");
					symbol.insertSQLTick("T");
				}
			}
			else if (sessionType == MarketSessionType.PREOPEN)
			{
				resetStatement() ;
				onCallRefData();
			}
		}
		else if (this.sessionType == MarketSessionType.CLOSE)
		{
			if (sessionType == MarketSessionType.OPEN 
					|| sessionType == MarketSessionType.PREOPEN)
			{
				resetStatement() ;
				onCallRefData();
			}
		}
		else if (this.sessionType == null)
		{
			onCallRefData();
		}
		this.sessionType = sessionType;
	}
	public void sendCentralReady(String appserv)
	{
		CentralDbReadyEvent event = new CentralDbReadyEvent(null, appserv);
		event.setTickTableList(tickTableManager.getTickTables());
		log.info("Sending ReadyEvent to: " + appserv);
		sendEvent(event);
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
		dbhnd = new DBHandler(jdbcUrl, driverClass) ;
		quoteBuffer = new LinkedList<QuoteEvent>();
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
//		onCallRefData();
		requestMarketSession() ;

		scheduleManager.scheduleRepeatTimerEvent(timeInterval, eventProcessor, timerEvent);
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

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public ArrayList<String> getPreSubscriptionList() {
		return preSubscriptionList;
	}

	public void setPreSubscriptionList(ArrayList<String> preSubscriptionList) {
		this.preSubscriptionList = preSubscriptionList;
	}

	public MarketSessionType getSessionType() {
		return sessionType;
	}
	
}

class AppendingObjectOutputStream extends ObjectOutputStream 
{

    public AppendingObjectOutputStream(OutputStream out) throws IOException 
    {
      super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException 
    {
		// do not write a header, but reset:
		// this line added after another question
	 	// showed a problem with the original
	 	reset();
    }

}
