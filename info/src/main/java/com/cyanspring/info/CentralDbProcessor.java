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
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.info.CentralDbReadyEvent;
import com.cyanspring.common.event.info.CentralDbSubscribeEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestEvent;
import com.cyanspring.common.event.info.PriceHighLowRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeRequestEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.SymbolEvent;
import com.cyanspring.common.event.marketdata.SymbolRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.info.FCRefSymbolInfo;
import com.cyanspring.common.info.FXRefSymbolInfo;
import com.cyanspring.common.info.IRefSymbolInfo;
import com.cyanspring.common.info.RefSubName;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.event.AsyncEventProcessor;
import com.cyanspring.info.util.DefPriceSetter;
import com.cyanspring.info.util.FXPriceSetter;


public class CentralDbProcessor implements IPlugin
{
	private static final Logger log = LoggerFactory
			.getLogger(CentralDbProcessor.class);
	
	private Map<String, RefSubName> subNameMap = new HashMap<String, RefSubName>();

	private String driverClass;
	private String jdbcUrl;
	private String serverMarket;
	private int nChefCount = 5;
	private ArrayList<String> preSubscriptionList;
	private ArrayList<SymbolChef> SymbolChefList = new ArrayList<SymbolChef>();
	private ChartCacheProc chartCacheProcessor;
	private CentralDbEventProc centralDbEventProcessor;
	
	private MarketSessionType sessionType = null ;
	private String tradedate ;
	static boolean isStartup = true;
	private boolean calledRefdata = false;
	private boolean processQuote = false;
	private Queue<QuoteEvent> quoteBuffer;

	// for checking SQL connect 
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private AsyncTimerEvent checkEvent = new AsyncTimerEvent();
	private AsyncTimerEvent insertEvent = new AsyncTimerEvent();
	private long SQLDelayInterval = 1;
	private long timeInterval = 600000;
	private long checkSQLInterval = 10 * 60 * 1000;
	
	private Date sessionEnd;
	
//	private HashMap<String, ArrayList<String>> mapDefaultSymbol = new HashMap<String, ArrayList<String>>();
//	private ArrayList<SymbolData> listSymbolData = new ArrayList<SymbolData>();
	private ArrayList<SymbolInfo> defaultSymbolInfo = new ArrayList<SymbolInfo>();
	private IRefSymbolInfo refSymbolInfo;
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
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}

	};

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
		if (event == timerEvent && !isStartup)
		{
			resetSymbolDataStat();
		}
		else if (event == checkEvent)
		{
			log.info("Check SQL connection");
			dbhnd.checkSQLConnect();
		}
		else if (event == insertEvent)
		{
			insertSQL();
//			System.out.println("insert");
		}
	}
	
	public void resetSymbolDataStat()
	{
		for (SymbolChef chef : SymbolChefList)
		{
			chef.resetSymbolDataStat();
		}
	}
	
	public void processQuoteEvent(QuoteEvent event)
	{
		if (SymbolChefList.size() != nChefCount)
		{
			return;
		}
		if (processQuote == false)
		{
			return;
		}
		Quote quote = event.getQuote();
		if (sessionType == MarketSessionType.OPEN 
				&& quote.getTimeStamp().getTime() >= sessionEnd.getTime())
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(sessionEnd);
			cal.add(Calendar.SECOND, -1);
			quote.setTimeStamp(cal.getTime());
		}
		else if (sessionType == MarketSessionType.PREOPEN)
		{
			quote.setTimeStamp(sessionEnd);
		}
		getChefBySymbol(quote.getSymbol()).onQuote(quote);
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event)
	{
		centralDbEventProcessor.onEvent(event);
	}
	
	public void processPriceHighLowRequestEvent(PriceHighLowRequestEvent event)
	{
		centralDbEventProcessor.onEvent(event);
	}
	
	public void processHistoricalPriceRequestEvent(HistoricalPriceRequestEvent event)
	{
		centralDbEventProcessor.onEvent(event);
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
		centralDbEventProcessor.onEvent(event);
	}
	
	public void requestDefaultSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<String> defaultSymbol = /*mapDefaultSymbol.get(market)*/preSubscriptionList;
		if (defaultSymbol == null || defaultSymbol.isEmpty())
		{
			retEvent.setOk(false);
//			retEvent.setMessage("Can't find requested Market in default map");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.REF_SYMBOL_NOT_FOUND, "Can't find requested Market in default map"));
			log.debug("Process Request Default Symbol fail: Can't find requested Market in default map");
			sendEvent(retEvent);
			return ;
		}
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		if (defaultSymbolInfo == null || defaultSymbolInfo.isEmpty())
		{
			retEvent.setOk(false);
//			retEvent.setMessage("Default SymbolInfo is empty");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.REF_SYMBOL_NOT_FOUND, "Default SymbolInfo is empty"));
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
//			retEvent.setMessage("Can't find requested symbol");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SEARCH_SYMBOL_ERROR, "Can't find requested symbol"));
		}
		else
		{
			retEvent.setOk(true);
		}
		retEvent.setSymbolList(retsymbollist);
		log.info("Process Request Default Symbol success Symbol: " + retsymbollist.size());
		sendEvent(retEvent);
	}
	
	public void userRequestAllSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<SymbolInfo> retSymbolInfo = (ArrayList<SymbolInfo>)getRefSymbolInfo().getAllSymbolInfo(market);
		retEvent.setSymbolList(retSymbolInfo);
		retEvent.setOk(true);
		log.info("Process Request All Symbol success Symbol: " + retSymbolInfo.size());
		sendEvent(retEvent);
	}
	
	public void userRequestGroupSymbol(SymbolListSubscribeEvent retEvent, String user, String market, String group)
	{
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> symbolinfoTmp = new ArrayList<SymbolInfo>();
		String sqlcmd = String.format("SELECT * FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s' AND `GROUP`='%s' AND `MARKET`='%s' ORDER BY `NO`;", 
				user, group, market) ;
		ResultSet rs = dbhnd.querySQL(sqlcmd);
		int index;
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
				symbolinfoTmp.add(symbolinfo);
//				index = getRefSymbolInfo().at(new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE")));
//				if (index >= 0)
//				{
//					symbolinfo = getRefSymbolInfo().get(index);
//					symbolinfos.add(symbolinfo);
//				}
			}
			symbolinfos = (ArrayList<SymbolInfo>) getRefSymbolInfo().getBySymbolInfos(symbolinfoTmp);
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
//			retEvent.setMessage(e.toString());
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SQL_SYNTAX_ERROR, e.toString()));
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
//			retEvent.setMessage("Recieved null argument");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SYMBOLIST_ERROR, "Recieved null argument"));
			log.debug("Process Request Group Symbol fail: Recieved null argument");
		}
		String sqlcmd ;
		sqlcmd = String.format("DELETE FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
				" AND `GROUP`='%s' AND `MARKET`='%s';", user, group, market) ;
		dbhnd.updateSQL(sqlcmd);
		ArrayList<SymbolInfo> symbolinfos = (ArrayList<SymbolInfo>)getRefSymbolInfo().getBySymbolStrings(symbols);
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		try
		{
			sqlcmd = String.format("INSERT INTO Subscribe_Symbol_Info (`USER_ID`,`GROUP`,`MARKET`,`EXCHANGE`,`CODE`,`HINT`,`WINDCODE`,`EN_NAME`,`CN_NAME`,`TW_NAME`,`JP_NAME`,`KR_NAME`,`ES_NAME`,`NO`) VALUES");
			boolean first = true;
			int No = 0;
			for (SymbolInfo syminfo : symbolinfos)
			{
				No = symbolinfos.indexOf(syminfo);
				if (first == false)
				{
					sqlcmd += "," ;
				}
				retsymbollist.add(syminfo);
				sqlcmd += String.format("('%s','%s','%s',", user, group, market);
				sqlcmd += (syminfo.getExchange() == null) ? "null," : String.format("'%s',", syminfo.getExchange());
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
			sqlcmd += ";" ;
			if (retsymbollist.size() != symbols.size())
			{
				retEvent.setSymbolList(null);
				retEvent.setOk(false);
//				retEvent.setMessage("Can't find requested symbol");
				retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SYMBOLIST_ERROR, "Can't find requested symbol"));
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
				int index;
				while(rs.next())
				{
//					symbolinfo = new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE"));
//					symbolinfo.setWindCode(rs.getString("WINDCODE"));
//					symbolinfo.setHint(rs.getString("HINT"));
//					symbolinfo.setCnName(rs.getString("CN_NAME"));
//					symbolinfo.setEnName(rs.getString("EN_NAME"));
//					symbolinfo.setTwName(rs.getString("TW_NAME"));
//					symbolinfo.setJpName(rs.getString("JP_NAME"));
//					symbolinfo.setKrName(rs.getString("KR_NAME"));
//					symbolinfo.setEsName(rs.getString("ES_NAME"));
//					retsymbollist.add(symbolinfo);
					index = getRefSymbolInfo().at(new SymbolInfo(rs.getString("MARKET"), rs.getString("CODE")));
					if (index >= 0)
					{
						symbolinfo = getRefSymbolInfo().get(index);
						retsymbollist.add(symbolinfo);
					}
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
//			retEvent.setMessage(e.toString());
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SQL_SYNTAX_ERROR, e.toString()));
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
		if (refList.isEmpty())
		{
			log.warn("refData is empty: " + refList.isEmpty());
			return ;
		}
		if (calledRefdata)
		{
			log.warn("refData is already read");
			return ;
		}
		synchronized(this)
		{
			PrintWriter outSymbol;
			ArrayList<String> marketList = new ArrayList<String>();
			try 
			{
				outSymbol = new PrintWriter(new BufferedWriter(new FileWriter(serverMarket)));
				defaultSymbolInfo.clear();
				getRefSymbolInfo().reset();
				getRefSymbolInfo().setByRefData(refList);
				defaultSymbolInfo = (ArrayList<SymbolInfo>)getRefSymbolInfo().getBySymbolStrings(preSubscriptionList);
				for(RefData refdata : refList)
				{
					if (refdata.getExchange() == null) continue;

					if (!marketList.contains(refdata.getExchange()))
					{
						dbhnd.checkMarketExist(refdata.getExchange());
						marketList.add(refdata.getExchange());
					}
					int chefNum = getChefNumber(refdata.getSymbol());
					SymbolChef chef = SymbolChefList.get(chefNum);
					chef.createSymbol(refdata, this);
//					SymbolData symbolData = new SymbolData(refdata.getSymbol(), refdata.getExchange(), this) ;
					
					if (refdata.getExchange() != null && refdata.getExchange().equals("FX"))
					{
						outSymbol.println(refdata.getSymbol());
					}
				}
				outSymbol.close();
				calledRefdata = true;
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
		for (SymbolChef chef : SymbolChefList)
		{
			chef.chefStart();
		}
		log.info("Call refData finish");
	}
	
	public void resetStatement()
	{
		isStartup = true;
		synchronized(this)
		{
			this.clearSymbolChefData();
			this.quoteBuffer.clear();
			getRefSymbolInfo().reset();
			calledRefdata = false;
		}
	}
	
	public void setSessionType(MarketSessionType sessionType, String market) 
	{
		boolean insert = false;
		boolean reset = false;
		if (this.sessionType == MarketSessionType.OPEN)
		{
			if (sessionType == MarketSessionType.CLOSE)
			{
//				insertSQL(market);
				insert = true;
			}
			else if (sessionType == MarketSessionType.PREOPEN)
			{
//				resetStatement() ;
//				onCallRefData();
				reset = true;
			}
		}
		else if (this.sessionType == MarketSessionType.CLOSE)
		{
			if (sessionType == MarketSessionType.OPEN 
					|| sessionType == MarketSessionType.PREOPEN)
			{
//				resetStatement() ;
//				onCallRefData();
				reset = true;
			}
		}
		else if (this.sessionType == null)
		{
//			onCallRefData();
			reset = true;
		}
		if (sessionType == MarketSessionType.OPEN || sessionType == MarketSessionType.PREOPEN)
			processQuote = true;
		this.sessionType = sessionType;
		
		if (insert)
		{
//			insertSQL();
			scheduleManager.scheduleTimerEvent(getSQLDelayInterval(), eventProcessor, insertEvent);
		}
		if (reset)
		{
			resetStatement() ;
			onCallRefData();
		}
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
		switch (serverMarket)
		{
		case "FC":
			this.refSymbolInfo = new FCRefSymbolInfo(serverMarket);
			SymbolData.setSetter(new DefPriceSetter());
			break;
		case "FX":
		default:
			this.refSymbolInfo = new FXRefSymbolInfo(serverMarket);
			SymbolData.setSetter(new FXPriceSetter());
			break;
		}
		if (getnChefCount() <= 1)
		{
			SymbolChefList.add(new SymbolChef("Symbol_Chef_0"));
		}
		else
		{
			for (int ii = 0; ii < getnChefCount(); ii++)
			{
				SymbolChefList.add(new SymbolChef("Symbol_Chef_" + ii));
			}
		}
		chartCacheProcessor = new ChartCacheProc();
		centralDbEventProcessor = new CentralDbEventProc(this);
		SymbolInfo.setSubNameMap(subNameMap);
		resetStatement() ;
		requestMarketSession() ;

		scheduleManager.scheduleRepeatTimerEvent(timeInterval, eventProcessor, timerEvent);
		scheduleManager.scheduleRepeatTimerEvent(checkSQLInterval, eventProcessor, checkEvent);
	}
	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessorMD.uninit();
		eventProcessor.uninit();
	}
	
	public String getTradedate() {
		return tradedate;
	}
	public void setTradedate(String tradedate)
	{
		this.tradedate = tradedate;
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

	public String getServerMarket() {
		return serverMarket;
	}

	public void setServerMarket(String serverMarket) {
		this.serverMarket = serverMarket;
	}
	public int getChefNumber(String strKey)
	{
		if(getnChefCount() <= 1)
			return 0;
		
		int nCode = strKey.hashCode();
		if(nCode < 0)
		    nCode = Math.abs(nCode);
			
		return nCode % getnChefCount();
	}
	public SymbolChef getChefBySymbol(String symbol)
	{
		int chefNum = getChefNumber(symbol);
		return SymbolChefList.get(chefNum);
	}
	public void clearSymbolChefData()
	{
		for (SymbolChef chef : SymbolChefList)
		{
			chef.chefStop();
			chef.clearSymbol();
		}
	}
	
	public void insertSQL()
	{
		processQuote = false;
		for (SymbolChef chef : SymbolChefList)
		{
			chef.insertSQL();
		}
	}

	public int getnChefCount() {
		return nChefCount;
	}

	public void setnChefCount(int nChefCount) {
		this.nChefCount = nChefCount;
	}

	public ChartCacheProc getChartCacheProcessor() {
		return chartCacheProcessor;
	}

	public Map<String, RefSubName> getSubNameMap() {
		return subNameMap;
	}

	public void setSubNameMap(Map<String, RefSubName> subNameMap) {
		this.subNameMap = subNameMap;
	}

	public IRefSymbolInfo getRefSymbolInfo() {
		return refSymbolInfo;
	}

	public long getSQLDelayInterval() {
		return SQLDelayInterval;
	}

	public void setSQLDelayInterval(long interval) {
		SQLDelayInterval = (interval > 0) ? interval : 1;
	}

	public Date getSessionEnd() {
		return sessionEnd;
	}

	public void setSessionEnd(Date sessionEnd) {
		this.sessionEnd = sessionEnd;
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
