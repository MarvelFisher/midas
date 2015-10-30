package com.cyanspring.info.cdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.info.CentralDbReadyEvent;
import com.cyanspring.common.event.info.CentralDbSubscribeEvent;
import com.cyanspring.common.event.info.GroupListEvent;
import com.cyanspring.common.event.info.GroupListRequestEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestDateEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestEvent;
import com.cyanspring.common.event.info.PriceHighLowRequestEvent;
import com.cyanspring.common.event.info.RetrieveChartEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeRequestEvent;
import com.cyanspring.common.event.marketdata.InnerQuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.SymbolRequestEvent;
import com.cyanspring.common.event.marketsession.AllIndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent.Action;
import com.cyanspring.common.info.FCRefSymbolInfo;
import com.cyanspring.common.info.FXRefSymbolInfo;
import com.cyanspring.common.info.GroupInfo;
import com.cyanspring.common.info.IRefSymbolInfo;
import com.cyanspring.common.info.RefSubName;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.common.staticdata.fu.IndexSessionType;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.info.util.DefPriceSetter;
import com.cyanspring.info.util.FXPriceSetter;
import com.cyanspring.info.util.InfoUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;


public class CentralDbProcessor implements IPlugin
{
	private static final Logger log = LoggerFactory
			.getLogger(CentralDbProcessor.class);
	
	//Procedures
	private ArrayList<SymbolChef> SymbolChefList = new ArrayList<SymbolChef>();
	private ChartCacheProc chartCacheProcessor;
	private HashMap<String, CentralDbEventProc> mapCentralDbEventProc;
	private HashMap<String, Integer> historicalDataCount;
	private HashMap<String, Integer> historicalDataPeriod;
	public static final String ProcHistory = "History";
	public static final String ProcRequest = "Request";
	public static final String ProcSession = "Session";
	//Session
	private MarketSessionType sessionType = null ;
	private Date sessionEnd = null;
	private String tradedate = null;
	private Map<String, MarketSessionData> sessionMap = new HashMap<String, MarketSessionData>();
	//Flags
	public static boolean isStartup = true;
	boolean isRetrieving = false;
	private boolean calledRefdata = false;
	private boolean isProcessQuote = false;
	private Queue<QuoteEvent> quoteBuffer;
	//SQL
	private ComboPooledDataSource cpds;	
	private String driverClass;
	private String jdbcUrl;
	private DBInsertProc insertProc;
	private boolean runInsertSQL = true;
	//TimerEvents 
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private AsyncTimerEvent checkEvent = new AsyncTimerEvent();
	private AsyncTimerEvent insertEvent = new AsyncTimerEvent();
	private AsyncTimerEvent retrieveEvent = new AsyncTimerEvent();
	private long SQLDelayInterval = 1;
	private long timeInterval = 60000;
	private long checkSQLInterval = 10 * 60 * 1000;
	private int numOfHisThreads = 5;
	private int curHisThread = 0;
	private int[] retrieveTime;
	//Parameters
	private String serverMarket;
	private int nChefCount = 5;
	private ArrayList<String> preSubscriptionList;
	private ArrayList<SymbolInfo> defaultSymbolInfo = new ArrayList<SymbolInfo>();
	private IRefSymbolInfo refSymbolInfo;
	private ArrayList<String> appServIDList = new ArrayList<String>();
	private DBHandler dbhnd ;
	
	private ConcurrentHashMap<String, HashMap<String, List<HistoricalPrice>>> retrieveMap = null;
	
	@Autowired @Qualifier("eventManager")
	private IRemoteEventManager eventManager;
	
	@Autowired @Qualifier("eventManagerMD")
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	private SystemInfo systemInfoMD;
	
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
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(HistoricalPriceRequestDateEvent.class, null);
			subscribeToEvent(GroupListRequestEvent.class, null);
			subscribeToEvent(RetrieveChartEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}		
	};
	
	
	private AsyncEventProcessor eventProcessorMD = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(InnerQuoteEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
			subscribeToEvent(RefDataEvent.class, null);
			subscribeToEvent(RefDataUpdateEvent.class, null);
			subscribeToEvent(IndexSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManagerMD;
		}

	};

	public void processCentralDbSubscribeEvent(CentralDbSubscribeEvent event) 
	{
		log.info("process CentralDbSubscribeEvent " + event.getSender());
		int index = Collections.binarySearch(appServIDList, event.getSender());
		if (index < 0)
		{
			appServIDList.add(~index, event.getSender());
			log.info("appServIDList add " + event.getSender());
		}
		if (!isStartup)
			respondCentralReady(event.getSender());
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) 
	{
		if (event == timerEvent)
		{
			if (isStartup == false)
				resetSymbolDataStat();
			Calendar cal = Calendar.getInstance();
			int now = (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE);
			for (int time : retrieveTime)
			{
				if (now == time)
				{
					retrieveChart(null);
					break;
				}
			}
		}
//		else if (event == checkEvent)
//		{
//			log.info("Check SQL connection");
//			getDbhnd().checkSQLConnect();
//		}
		else if (event == insertEvent)
		{
			log.info("Get insertEvent, start insertSQL");
			insertSQL();
		}
		else if (event == retrieveEvent)
		{
			retrieveChart(null);
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
		if (isStartup)
		{
			return;
		}
		Quote quote = event.getQuote();
		processQuote(quote);
	}
	public void processInnerQuoteEvent(InnerQuoteEvent event)
	{
		if (isStartup)
		{
			return;
		}
		Quote quote = event.getQuote();
		processQuote(quote);
	}
	public void processQuote(Quote quote)
	{
		if (SymbolChefList.size() != nChefCount)
			return;
//		if (isProcessQuote == false)
//			return;
		SymbolData data = null;
		for (SymbolChef chef : SymbolChefList)
		{
			data = chef.getSymbolData(quote.getSymbol());
			if (data != null)
				break;
		}
		if (data == null)
			return;
		
		MarketSessionType sessionType = data.getSessionType();
		Date sessionEnd = data.getSessionEnd();
		if (sessionType == MarketSessionType.OPEN)
		{
			if (quote.getTimeStamp().getTime() >= sessionEnd.getTime())
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(sessionEnd);
				cal.add(Calendar.SECOND, -1);
				quote.setTimeStamp(cal.getTime());
			}
		}
		else if (sessionType == MarketSessionType.PREOPEN)
		{
			quote.setTimeStamp(sessionEnd);
		}
		else 
			return;
		getChefBySymbol(quote.getSymbol()).onQuote(quote);
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event)
	{
		mapCentralDbEventProc.get(ProcSession).onEvent(event);
	}
	
	public void processPriceHighLowRequestEvent(PriceHighLowRequestEvent event)
	{
		mapCentralDbEventProc.get(ProcHistory + curHisThread).onEvent(event);
		curHisThread = (curHisThread + 1) % numOfHisThreads;  
	}
	
	public void processHistoricalPriceRequestEvent(HistoricalPriceRequestEvent event)
	{
		mapCentralDbEventProc.get(ProcHistory + curHisThread).onEvent(event);
		curHisThread = (curHisThread + 1) % numOfHisThreads;  
	}
	
	public void processHistoricalPriceRequestDateEvent(HistoricalPriceRequestDateEvent event)
	{
		mapCentralDbEventProc.get(ProcHistory + curHisThread).onEvent(event);
		curHisThread = (curHisThread + 1) % numOfHisThreads;  
	}
	
	public void processSymbolListSubscribeRequestEvent(SymbolListSubscribeRequestEvent event)
	{
		mapCentralDbEventProc.get(ProcRequest).onEvent(event);
	}
	
	public void processRefDataEvent(RefDataEvent event) 
	{
		mapCentralDbEventProc.get(ProcSession).onEvent(event);
	}
	
	public void processRefDataUpdateEvent(RefDataUpdateEvent event) 
	{
		mapCentralDbEventProc.get(ProcSession).onEvent(event);
	}
	
	public void processRetrieveChartEvent(RetrieveChartEvent event)
	{
		mapCentralDbEventProc.get(ProcRequest).onEvent(event);
	}
	public void processGroupListRequestEvent(GroupListRequestEvent event)
	{
		mapCentralDbEventProc.get(ProcRequest).onEvent(event);
	}
	public void processIndexSessionEvent(IndexSessionEvent event)
	{
//		if (isUsingIndex())
			mapCentralDbEventProc.get(ProcSession).onEvent(event);
	}
	
	public void requestDefaultSymbol(SymbolListSubscribeEvent retEvent, String market)
	{
		ArrayList<String> defaultSymbol = preSubscriptionList;
		if (defaultSymbol == null || defaultSymbol.isEmpty())
		{
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.REF_SYMBOL_NOT_FOUND, "Can't find requested Market in default map"));
			log.debug("Process Request Default Symbol fail: Can't find requested Market in default map");
			sendEvent(retEvent);
			return ;
		}
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		if (defaultSymbolInfo == null || defaultSymbolInfo.isEmpty())
		{
			retEvent.setOk(false);
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
	
	public void userRequestGroupSymbol(SymbolListSubscribeEvent retEvent, String user, String market, String group, boolean allowEmpty)
	{
		ArrayList<SymbolInfo> symbolinfos = new ArrayList<SymbolInfo>();
		ArrayList<SymbolInfo> symbolinfoTmp = new ArrayList<SymbolInfo>();
		symbolinfoTmp.addAll(getDbhnd().getGroupSymbol(user, group, market, refSymbolInfo, false));
		symbolinfos = (ArrayList<SymbolInfo>) getRefSymbolInfo().getBySymbolInfos(symbolinfoTmp);
		if (allowEmpty == false && symbolinfos.isEmpty())
		{
			requestDefaultSymbol(retEvent, market);
			return;
		}
		retEvent.setSymbolList(symbolinfos);
		retEvent.setOk(true);
		log.info("Process Request Group Symbol success Symbol: " + symbolinfos.size());
		sendEvent(retEvent);
	}

	public void userSetGroupSymbol(SymbolListSubscribeEvent retEvent, 
			   String user, 
			   String market, 
			   String group, 
			   ArrayList<String> symbols)
	{
		if (symbols == null || (symbols.isEmpty() && retEvent.isAllowEmpty() == false))
		{
			symbols = preSubscriptionList;
		}
		if (user == null || market == null || group == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SYMBOLIST_ERROR, "Recieved null argument"));
			log.debug("Process Request Group Symbol fail: Recieved null argument");
			sendEvent(retEvent);
			return;
		}
		String userEncode;
		userEncode = InfoUtils.utf8Encode(user);
		String sqlcmd ;
		sqlcmd = String.format("DELETE FROM `Subscribe_Symbol_Info` WHERE `USER_ID`='%s'" + 
				" AND `GROUP`='%s' AND `MARKET`='%s';", userEncode, group, market) ;
		getDbhnd().updateSQL(sqlcmd);
		ArrayList<SymbolInfo> symbolinfos = (ArrayList<SymbolInfo>)getRefSymbolInfo().getBySymbolStrings(symbols);
		ArrayList<SymbolInfo> retsymbollist = new ArrayList<SymbolInfo>();
		sqlcmd = String.format("UPDATE `Subscribe_Group_Info` SET `SYMBOL_COUNT`=%d WHERE `USER_ID`='%s'" + 
				" AND `MARKET`='%s' AND `GROUP_ID`='%s';", symbolinfos.size(), userEncode, market, group) ;
		getDbhnd().updateSQL(sqlcmd);
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
			sqlcmd += String.format("('%s','%s','%s',", userEncode, group, market);
			sqlcmd += (syminfo.getExchange() == null) ? "null," : String.format("'%s',", syminfo.getExchange());
			sqlcmd += (syminfo.getCode() == null) ? "null," : String.format("'%s',", syminfo.getCode());
			sqlcmd += (syminfo.getHint() == null) ? "null," : String.format("'%s',", syminfo.getHint());
			sqlcmd += (syminfo.getWindCode() == null) ? "null," : String.format("'%s',", syminfo.getWindCode());
			sqlcmd += (syminfo.getEnName() == null) ? "null," : String.format("'%s',", syminfo.getEnName().replace("'", "\\'"));
			sqlcmd += (syminfo.getCnName() == null) ? "null," : String.format("'%s',", syminfo.getCnName().replace("'", "\\'"));
			sqlcmd += (syminfo.getTwName() == null) ? "null," : String.format("'%s',", syminfo.getTwName().replace("'", "\\'"));
			sqlcmd += (syminfo.getJpName() == null) ? "null," : String.format("'%s',", syminfo.getJpName().replace("'", "\\'"));
			sqlcmd += (syminfo.getKrName() == null) ? "null," : String.format("'%s',", syminfo.getKrName().replace("'", "\\'"));
			sqlcmd += (syminfo.getEsName() == null) ? "null," : String.format("'%s',", syminfo.getEsName().replace("'", "\\'"));
			sqlcmd += No;
			sqlcmd += ")";
			first = false;
		}
		sqlcmd += ";" ;
		if (retsymbollist.size() != symbols.size())
		{
			retEvent.setSymbolList(null);
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SYMBOLIST_ERROR, "Can't find requested symbol"));
			log.debug("Process Request Group Symbol fail: Can't find requested symbol");
		}
		else
		{
			if (symbolinfos.isEmpty() == false)
				getDbhnd().updateSQL(sqlcmd);
			retsymbollist.clear();
			retsymbollist.addAll(getDbhnd().getGroupSymbol(user, group, market, refSymbolInfo, true));
			if (symbolinfos.isEmpty() && retEvent.isAllowEmpty() == false)
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
	
	public void userRequestGroupList(GroupListEvent retEvent)
	{
		StringBuilder msg;
		msg = new StringBuilder("Process Request Group Symbol user:");
		msg.append(retEvent.getUserID());
		msg.append(" mkt:");
		msg.append(retEvent.getMarket());
		log.debug(msg.toString());
		List<GroupInfo> retList = this.getDbhnd().getGroupList(retEvent.getUserID(), retEvent.getMarket());
		retEvent.setGroupList(retList);
		if (retList == null)
		{
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.CONNECTION_BROKEN, "Lost connection to Info Database"));
			log.debug("Process Request Group Symbol fail: Can't find requested symbol");
		}
		else if (retList.isEmpty())
		{
			msg = new StringBuilder("Empty GroupList result user:");
			msg.append(retEvent.getUserID());
			msg.append(" mkt:");
			msg.append(retEvent.getMarket());
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.DATA_NOT_FOUND, msg.toString()));
			log.debug(msg.toString());
		}
		else
		{
			retEvent.setOk(true);
		}
		sendEvent(retEvent);
	}
	
	public void userSetGroupList(GroupListEvent retEvent, List<GroupInfo> groups)
	{
		StringBuilder msg;
		if (groups == null || groups.isEmpty())
		{
			msg = new StringBuilder("Process Set Group List fail: ");
			if (groups == null) msg.append("groups == null ");
			if (groups.isEmpty()) msg.append("groups is empty");
			log.debug(msg.toString());
			retEvent.setOk(false);
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.SYMBOLIST_ERROR, msg.toString()));
			sendEvent(retEvent);
			return;
		}
		msg = new StringBuilder("Process Set Group List ");
		msg.append(retEvent.getUserID());
		msg.append(" Group:");
		for (GroupInfo g : groups)
		{
			if (groups.indexOf(g) > 0) msg.append(",");
			msg.append(g.getGroupID());
		}
		log.debug(msg.toString());
		List<GroupInfo> orgList = this.getDbhnd().getGroupList(retEvent.getUserID(), retEvent.getMarket());
		List<GroupInfo> delList = new ArrayList<GroupInfo>();
		ArrayList<GroupInfo> sortSource = new ArrayList<GroupInfo>();
		sortSource.addAll(groups);
		int index;
		Collections.sort(sortSource);
		for (GroupInfo ginfo : orgList)
		{
			index = Collections.binarySearch(sortSource, ginfo);
			if (0 > index)
			{
				delList.add(ginfo);
			}
			else
			{
				sortSource.get(index).setSymbolCount(ginfo.getSymbolCount());
			}
		}
		for (GroupInfo ginfo1 : sortSource)
		{
			for (GroupInfo ginfo2 : groups)
			{
				if (ginfo1.getGroupID().equals(ginfo2.getGroupID()))
				{
					ginfo2.setSymbolCount(ginfo1.getSymbolCount());
					break;
				}
			}
		}
		String userEn, groupIDEn, groupNameEn;
		String market = retEvent.getMarket();
		userEn = InfoUtils.utf8Encode(retEvent.getUserID());
		String sqlcmd ;
		sqlcmd = String.format("DELETE FROM `Subscribe_Group_Info` WHERE `USER_ID`='%s' AND `MARKET`='%s';", 
				userEn, retEvent.getMarket()) ;
		getDbhnd().updateSQL(sqlcmd);
		sqlcmd = String.format("INSERT INTO `Subscribe_Group_Info` (`USER_ID`,`MARKET`,`GROUP_ID`,`GROUP_NAME`,`SYMBOL_COUNT`,`NO`) VALUES ");
		boolean first = true;
		for (GroupInfo ginfo : groups)
		{
			groupIDEn = InfoUtils.utf8Encode(ginfo.getGroupID());
			groupNameEn = InfoUtils.utf8Encode(ginfo.getGroupName());
			if (first == true)
			{
				first = false;
			}
			else
			{
				sqlcmd += ",";
			}
			sqlcmd += String.format("('%s','%s','%s','%s',%d,%d)", 
					userEn, market, groupIDEn, groupNameEn, ginfo.getSymbolCount(), groups.indexOf(ginfo));
		}
		sqlcmd += "ON DUPLICATE KEY UPDATE `GROUP_NAME`=Values(GROUP_NAME);" ;
		getDbhnd().updateSQL(sqlcmd);
		userRequestGroupList(retEvent);
	}
	
	public void onCallRefData(RefDataEvent event)
	{
		log.info("Call refData start");
		List<RefData> refList = event.getRefDataList();
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
		PrintWriter outSymbol;
		ArrayList<String> marketList = new ArrayList<String>();
		try
		{
			outSymbol = new PrintWriter(new BufferedWriter(new FileWriter(
					serverMarket)));
			defaultSymbolInfo.clear();
			getRefSymbolInfo().reset();
			getRefSymbolInfo().setByRefData(refList);
			defaultSymbolInfo = (ArrayList<SymbolInfo>) getRefSymbolInfo()
					.getBySymbolStrings(preSubscriptionList);
			boolean isAdded = false;
			for (RefData refdata : refList)
			{
				if (refdata.getExchange() == null || refdata.getSymbol() == null)
					continue;

				if (!marketList.contains(refdata.getExchange()))
				{
					getDbhnd().checkMarketExist(refdata.getExchange());
					marketList.add(refdata.getExchange());
				}
				int chefNum = getChefNumber(refdata.getSymbol());
				SymbolChef chef = SymbolChefList.get(chefNum);
				isAdded |= chef.createSymbol(refdata, this);
				SymbolData data = chef.getSymbolData(refdata.getSymbol());
				data.setSessionIndex(refdata.getIndexSessionType());
				data.setSessionType(this.sessionType);
				data.setSessionEnd(this.sessionEnd);
				data.setTradedate(this.tradedate);

				outSymbol.println(refdata.getSymbol());
			}
			outSymbol.close();
			if (isAdded || isStartup)
			{
				sendCentralReady();
				retrieveChart(null);
			}
			calledRefdata = true;
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
			isStartup = false;
		}
		for (SymbolChef chef : SymbolChefList)
		{
			chef.chefStart();
		}
		log.info("Call refData finish");
		sendSessionRequest();
	}
	
	public void onUpdateRefData(RefDataUpdateEvent event)
	{
		log.info("Update refData start");
		List<RefData> refList = event.getRefDataList();
		String symbol, indexSessionType, key;
		if (event.getAction() == Action.ADD || event.getAction() == Action.MOD)
		{
			int nCount = getRefSymbolInfo().setByRefData(refList);
//			if (nCount == 0)
//			{
//				return;
//			}
			for(RefData refdata : refList)
			{
				if (refdata.getExchange() == null) 
					continue;
				symbol = refdata.getSymbol();
				indexSessionType = refdata.getIndexSessionType();
				SymbolInfo info = getRefSymbolInfo().getbySymbol(symbol);
				if (info != null)
					info.updateByRefData(refdata);
				int chefNum = getChefNumber(symbol);
				SymbolChef chef = SymbolChefList.get(chefNum);
				if (chef.createSymbol(refdata, this))
					log.debug("Add symbol " + symbol);
				SymbolData data = chef.getSymbolData(symbol);
				data.setSessionIndex(indexSessionType);
				log.debug("Set symbol:" + symbol + "sesType:" + indexSessionType);
				if (IndexSessionType.EXCHANGE.name().equals(indexSessionType))
				{
					key = info.getExchange();
				}
				else if (IndexSessionType.SPOT.name().equals(indexSessionType))
				{
					key = info.getCategory();
				}
				else if (IndexSessionType.SETTLEMENT.name().equals(indexSessionType))
				{
					key = info.getCode();
				}
				else 
				{
					continue;
				}
				MarketSessionData sesdata = sessionMap.get(key);
				if (sesdata != null)
				{
					data.setSessionType(sesdata.getSessionType());
				}
			}
		}
		else if (event.getAction() == Action.DEL)
		{
			getRefSymbolInfo().delByRefData(refList);
			for(RefData refdata : refList)
			{
				int chefNum = getChefNumber(refdata.getSymbol());
				SymbolChefList.get(chefNum).removeSymbol(refdata.getSymbol());
			}
		}
		log.info("Update refData finish");
	}
	
	protected void retrieveChart(final RetrieveChartEvent event)
	{
		if (isRetrieving)
		{
			return;
		}
		isRetrieving = true;
		Thread retrieveThread = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				log.debug("Retrieve Chart thread start");
				while (calledRefdata == false)
				{
					try 
					{
						Thread.sleep(100);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
				getAllChartPrice();
				log.debug("Retrieve Chart thread finish");
				isRetrieving = false;
				
				if (event != null)
					sendRetrieveReady(event);
				if (isStartup)
				{
					sendCentralReady();
				}
				for (SymbolChef chef : SymbolChefList)
				{
					chef.checkAllChartPrice();
				}
				log.debug("Retrieve Chart thread finish");
			}
		});
		retrieveThread.setName((event == null) ? "CDP_Retrieve_Chart" : "CDP_Retrieve_Event");
		retrieveThread.start();
	}
	public void retrieveAllChart(RetrieveChartEvent event)
	{
		retrieveChart(event);
	}
	
	public void retrieveCharts(final RetrieveChartEvent event)
	{
		Thread retrieveThread = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				log.debug("Retrieve Chart thread start");
				
				List<String> symbolList = event.getSymbolList();
				for (String symbol : symbolList)
				{
					getChefBySymbol(symbol).retrieveChartPrice(symbol);
				}
				
				log.debug("Retrieve Chart thread finish");
				sendRetrieveReady(event);
				for (SymbolChef chef : SymbolChefList)
				{
					chef.checkAllChartPrice();
				}
				log.debug("Retrieve Chart thread finish");
			}
		});
		retrieveThread.setName("CDP_Retrieve_Event");
		retrieveThread.start();
	}
	
	public void getAllChartPrice()
	{
		clearAllChartPrice();
		ArrayList<String> marketList = new ArrayList<String>();
		for (SymbolChef chef : SymbolChefList)
		{
			int index;
			for (String market : chef.getAllMarket())
			{
				index = Collections.binarySearch(marketList, market);
				if (index < 0)
				{
					marketList.add(~index, market);
				}
			}
		}
		getChartPriceByMarkets(marketList);
	}
	
	public void getChartPriceByMarkets(List<String> marketList)
	{
		for (String market : marketList)
		{
			getChartPriceByMarket(market);
		}
	}
	
	public void getChartPriceByMarket(String market)
	{
		getChartPrice(market, "1");
		getChartPrice(market, "R");
		getChartPrice(market, "A");
		getChartPrice(market, "Q");
		getChartPrice(market, "H");
		getChartPrice(market, "6");
		getChartPrice(market, "T");
		getChartPrice(market, "D");
		getChartPrice(market, "W");
		getChartPrice(market, "M");
		String symbol;
		SymbolData symboldata;
		ArrayList<String> symbolarr = new ArrayList<String>();
		Iterator<Entry<String, HashMap<String, List<HistoricalPrice>>>> itr
			= getRetrieveMap().entrySet().iterator();
//		for (Entry<String, HashMap<String, List<HistoricalPrice>>> entry : getRetrieveMap()
//				.entrySet())
		Entry<String, HashMap<String, List<HistoricalPrice>>> entry;
		while(itr.hasNext())
		{
			entry = itr.next();
			symbolarr.clear();
			symbolarr.add(entry.getKey());
			ArrayList<SymbolInfo> symbolinfos = (ArrayList<SymbolInfo>) getRefSymbolInfo()
					.getBySymbolStrings(symbolarr);
			if (symbolinfos.size() < 1)
			{
				continue;
			}
			if (symbolinfos.get(0).hasRefSymbol() == false
					|| entry.getKey().equals(symbolinfos.get(0).getCode()) == false)
			{
				symbol = symbolinfos.get(0).getCode();
			}
			else
			{
				continue;
			}
			// symbol = entry.getKey();
			symboldata = getChefBySymbol(symbol).getSymbolData(symbol);
			if (symboldata != null)
			{
				symboldata.setMapHistorical(entry.getValue());
				symboldata.set52WHLByMapHistorical();
			}
		}
	}
	
	public void clearAllChartPrice()
	{
		if (getRetrieveMap() == null)
		{
			setRetrieveMap(new ConcurrentHashMap<String, HashMap<String, List<HistoricalPrice>>>());
		}
		getRetrieveMap().clear();
	}
	public void getChartPrice(String market, String strType)
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, (-1) * (getHistoricalDataPeriod().get(strType) + 2));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, List<HistoricalPrice>> historical;
		List<String> symbolList = refSymbolInfo.getSymbolList(market);
		if (strType.equals("D") || strType.equals("W") || strType.equals("M"))
		{
			historical = getDbhnd().getSymbolsValue(market, strType, 
					null, symbolList, historicalDataCount.get(strType));
		}
		else
		{
			historical = getDbhnd().getSymbolsValue(market, strType, 
					sdf.format(cal.getTime()), symbolList, historicalDataCount.get(strType));
		}
		HashMap<String, List<HistoricalPrice>> subMap;
		for (Entry<String, List<HistoricalPrice>> entry : historical.entrySet())
		{
			if (getRetrieveMap().get(entry.getKey()) == null)
			{
				getRetrieveMap().put(entry.getKey(), new HashMap<String, List<HistoricalPrice>>());
			}
			subMap = getRetrieveMap().get(entry.getKey());
			if (subMap.get(strType) == null)
			{
				subMap.put(strType, new ArrayList<HistoricalPrice>());
			}
			subMap.put(strType, entry.getValue());
		}
		return;
	}
	
	public void resetStatement()
	{
		isStartup = true;
		this.clearSymbolChefData();
		this.quoteBuffer.clear();
		getRefSymbolInfo().reset();
		chartCacheProcessor.clear();
		calledRefdata = false;
		setCurHisThread(0);
	}
	
	public void setSessionType(MarketSessionType sessionType, String market) 
	{
		boolean insert = false;
		boolean reset = false;
		
		if (this.sessionType == null)
		{
			reset = true;
			retrieveChart(null);
		}
		else if(this.sessionType != sessionType) {
			if (sessionType == MarketSessionType.CLOSE)
			{
				insert = true;
			}			
			if(this.sessionType == MarketSessionType.CLOSE) {
				reset = true;
			}
		}
		this.sessionType = sessionType;
		for (SymbolChef chef : SymbolChefList)
		{
			for (Entry<String, SymbolData> entry : chef.getMapSymboldata().entrySet())
			{
				entry.getValue().setSessionType(sessionType);
			}
		}
		if (insert)
		{
//			scheduleManager.scheduleTimerEvent(getSQLDelayInterval(), eventProcessor, insertEvent);
			insertSQL();
		}
		if (reset)
		{
			resetStatement() ;
			sendRefDataRequest();
		}
		if (sessionType == MarketSessionType.OPEN || sessionType == MarketSessionType.PREMARKET)
			isProcessQuote = true;
	}
	
	public void setSessionIndex(Map<String, MarketSessionData> sessions) 
	{
		MarketSessionType sessionType, newSessionType;
		List<String> indexList = new ArrayList<String>();
		String indexSessionType;
		SymbolInfo syminfo;
		MarketSessionData session;
		for (Entry<String, MarketSessionData> entry : sessions.entrySet())
		{
			session = entry.getValue();
			log.info(String.format("Process IndexMarketSession N:%s T:%s D:%s S:%s E:%s", 
					entry.getKey(), session.getSessionType().name(), 
					session.getTradeDateByString(), session.getStart(), session.getEnd()));
			ArrayList<SymbolData> symboldatas = new ArrayList<SymbolData>();
			sessionType = getSessionMap().get(entry.getKey()) == null ? 
					null : getSessionMap().get(entry.getKey()).getSessionType();
			newSessionType = session.getSessionType();
			for (SymbolChef chef : SymbolChefList)
			{
				for (Entry<String, SymbolData> symentry : chef.getMapSymboldata().entrySet())
				{
					indexSessionType = symentry.getValue().getSessionIndex();
					syminfo = refSymbolInfo.getbySymbol(symentry.getValue().getStrSymbol());
					if (IndexSessionType.EXCHANGE.name().equals(indexSessionType)
							&& syminfo.getExchange().equals(entry.getKey()) == false)
					{
						continue;
					}
					if (IndexSessionType.SPOT.name().equals(indexSessionType)
							&& syminfo.getCategory().equals(entry.getKey()) == false)
					{
						continue;
					}
					if (IndexSessionType.SETTLEMENT.name().equals(indexSessionType)
							&& syminfo.getCode().equals(entry.getKey()) == false)
					{
						continue;
					}
//					symentry.getValue().setSessionType(entry.getValue().getSessionType());
					symboldatas.add(symentry.getValue());
				}
			}
			try
			{
				for (SymbolData data : symboldatas)
				{
					data.setTradedate(session.getTradeDateByString());
					data.setSessionType(newSessionType);
					data.setSessionEnd(session.getEndDate());
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			if (sessionType != newSessionType && sessionType == MarketSessionType.CLOSE)
				indexList.add(entry.getKey());
			getSessionMap().put(entry.getKey(), entry.getValue());
		}
		if (indexList.isEmpty() == false)
		{
			log.info("IndexSession Send Ready to AppServer");
			CentralDbReadyEvent event = new CentralDbReadyEvent(null, null);
			event.setTickTableList(tickTableManager.getTickTables());
			event.setIndexList(indexList);
			for (String appserv : appServIDList)
			{
				event.setReceiver(appserv);
				log.info("Sending ReadyEvent to: " + appserv);
				sendEvent(event);
			}
		}
	}
	
	public void sendCentralReady()
	{
		if (isStartup)
		{
			log.info("SymbolData is ready, send CDPReadyEvent to all connected appServer");
			for (String appserv : appServIDList)
			{
				respondCentralReady(appserv);
			}
			isStartup = false;
		}
	}
	public void sendRetrieveReady(RetrieveChartEvent event)
	{
		log.info("SymbolData is ready, send Retrieve Notification to all connected appServer");
		for (String appserv : appServIDList)
		{
			event.setReceiver(appserv);
			log.info("Sending RetrieveChartEvent to: " + appserv);
			sendEvent(event);
		}
		isStartup = false;
	}
	public void respondCentralReady(String appserv)
	{
		CentralDbReadyEvent event = new CentralDbReadyEvent(null, appserv);
		event.setTickTableList(tickTableManager.getTickTables());
		event.setIndexList(new ArrayList<String>(this.sessionMap.keySet()));
		log.info("Sending ReadyEvent to: " + appserv);
		sendEvent(event);
	}
	
	public void sendRefDataRequest()
	{
		log.info("Send RefDataRequest");
		RefDataRequestEvent event = new RefDataRequestEvent(null, null);
		event.setReceiver(systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + systemInfoMD.getId());
		log.info("Sending RefDataRequest event ...");
		sendMDEvent(event);
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
	
	public void sendSessionRequest()
	{
//		if (isUsingIndex())
//		{
			requestIndexMarketSession();
			log.info("Send IndexMarketSession request");
//		}
//		else
//		{
//			requestMarketSession();
//			log.info("Send MarketSession request");
//		}
	}
	
	public void requestMarketSession()
	{
		log.info("Send MarketSessionRequest");
		String receiver = String.format("%s.%s.%s", systemInfoMD.getEnv(), systemInfoMD.getCategory(), systemInfoMD.getId()) ;
		sendMDEvent(new MarketSessionRequestEvent(null, receiver)) ;
	}
	public void requestIndexMarketSession()
	{
		log.info("Send IndexSessionRequest");
		String receiver = String.format("%s.%s.%s", systemInfoMD.getEnv(), systemInfoMD.getCategory(), systemInfoMD.getId()) ;
		sendMDEvent(new IndexSessionRequestEvent(null, receiver, null)) ;
	}
	public void requestSymbolList()
	{
		String receiver = String.format("%s.%s.%s", systemInfoMD.getEnv(), systemInfoMD.getCategory(), systemInfoMD.getId()) ;
		sendMDEvent(new SymbolRequestEvent(null, receiver)) ;
	}
	@Override
	public void init() throws Exception {
		log.info("Initialising...");
		setDbhnd(new DBHandler(cpds)) ;
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
		if (getnChefCount() <= 1)
		{
			numOfHisThreads = 1;
			SymbolChefList.add(new SymbolChef("Symbol_Chef_0"));
		}
		else
		{
			numOfHisThreads = getnChefCount();
			for (int ii = 0; ii < getnChefCount(); ii++)
			{
				SymbolChefList.add(new SymbolChef("Symbol_Chef_" + ii));
			}
		}
		chartCacheProcessor = new ChartCacheProc();
		setInsertProc(new DBInsertProc(this));
		mapCentralDbEventProc = new HashMap<String, CentralDbEventProc>();
		for (int ii = 0; ii < numOfHisThreads; ii++)
		{
			mapCentralDbEventProc.put(ProcHistory + ii, new CentralDbEventProc(this, "CDP-Event-His" + ii, true));
		}
		mapCentralDbEventProc.put(ProcRequest, new CentralDbEventProc(this, "CDP-Event-Req", false));
		mapCentralDbEventProc.put(ProcSession, new CentralDbEventProc(this, "CDP-Event-Ses", false));
		if (isUsingIndex())
		{
			this.refSymbolInfo = new FCRefSymbolInfo(serverMarket);
			SymbolData.setSetter(new DefPriceSetter());
		}
		else
		{
			this.refSymbolInfo = new FXRefSymbolInfo(serverMarket);
			SymbolData.setSetter(new FXPriceSetter());
		}
		resetStatement() ;
		sendRefDataRequest();

		scheduleManager.scheduleRepeatTimerEvent(getTimeInterval(), eventProcessor, timerEvent);
		scheduleManager.scheduleRepeatTimerEvent(getCheckSQLInterval(), eventProcessor, checkEvent);
	}
	@Override
	public void uninit() {
		log.info("Uninitialising...");
		eventProcessorMD.uninit();
		eventProcessor.uninit();
		chartCacheProcessor.clear();
		chartCacheProcessor.exit();
		scheduleManager.uninit();
	}
	
	public boolean isUsingIndex()
	{
		switch (serverMarket)
		{
		case "FX":
			return false;
		default:
			return true;
		}
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
		isProcessQuote = false;
//		Thread insertThread = new Thread(new Runnable() 
//		{
//			@Override
//			public void run() 
//			{
//				for (SymbolChef chef : SymbolChefList)
//				{
//					chef.insertSQL();
//				}
//				retrieveChart();
//			}
//		});
//		insertThread.setName("CDP_Insert_SQL");
		if (isRunInsertSQL())
		{
//			insertThread.start();
			for (SymbolChef chef : SymbolChefList)
			{
				chef.insertSymbol();
			}
		}
		else
		{
			scheduleManager.scheduleTimerEvent(10 * 60 * 1000, eventProcessor, retrieveEvent);
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

	public IRefSymbolInfo getRefSymbolInfo() {
		return refSymbolInfo;
	}

	public long getSQLDelayInterval() {
		return SQLDelayInterval;
	}

	public void setSQLDelayInterval(long interval) {
		SQLDelayInterval = (interval > 0) ? interval : 1;
	}

	public long getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(long timeInterval) {
		this.timeInterval = (timeInterval <= 0) ? 600000 : timeInterval;
	}

	public long getCheckSQLInterval() {
		return checkSQLInterval;
	}

	public void setCheckSQLInterval(long checkSQLInterval) {
		this.checkSQLInterval = (checkSQLInterval <= 0) ? 600000 : checkSQLInterval;
	}

	public Date getSessionEnd() {
		return sessionEnd;
	}

	public void setSessionEnd(Date sessionEnd) {

		for (SymbolChef chef : SymbolChefList)
		{
			for (Entry<String, SymbolData> entry : chef.getMapSymboldata().entrySet())
			{
				entry.getValue().setSessionEnd(sessionEnd);
			}
		}
		this.sessionEnd = sessionEnd;
	}

	public DBHandler getDbhnd() {
		return dbhnd;
	}

	public void setDbhnd(DBHandler dbhnd) {
		this.dbhnd = dbhnd;
	}

	public ComboPooledDataSource getCpds() {
		return cpds;
	}

	public void setCpds(ComboPooledDataSource cpds) {
		this.cpds = cpds;
	}

	public HashMap<String, Integer> getHistoricalDataCount() {
		return historicalDataCount;
	}

	public void setHistoricalDataCount(HashMap<String, Integer> historicalDataCount) {
		this.historicalDataCount = historicalDataCount;
	}

	public int getNumOfHisThreads() {
		return numOfHisThreads;
	}

	public void setNumOfHisThreads(int numOfHisThreads) {
		if (numOfHisThreads < 1)
		{
			this.numOfHisThreads = 1;
		}
		else if (numOfHisThreads > 5)
		{
			this.numOfHisThreads = 5;
		}
		else
		{
			this.numOfHisThreads = numOfHisThreads;
		}
	}

	public int getCurHisThread() {
		return curHisThread;
	}

	public void setCurHisThread(int curHisThread) {
		this.curHisThread = curHisThread;
	}

	public HashMap<String, Integer> getHistoricalDataPeriod() {
		return historicalDataPeriod;
	}

	public void setHistoricalDataPeriod(HashMap<String, Integer> historicalDataPeriod) {
		this.historicalDataPeriod = historicalDataPeriod;
	}

	public boolean isRunInsertSQL() {
		return runInsertSQL;
	}

	public void setRunInsertSQL(boolean runInsertSQL) {
		this.runInsertSQL = runInsertSQL;
	}

	public ConcurrentHashMap<String, HashMap<String, List<HistoricalPrice>>> getRetrieveMap()
	{
		return retrieveMap;
	}

	public void setRetrieveMap(ConcurrentHashMap<String, HashMap<String, List<HistoricalPrice>>> retrieveMap)
	{
		this.retrieveMap = retrieveMap;
	}

	public DBInsertProc getInsertProc()
	{
		return insertProc;
	}

	public void setInsertProc(DBInsertProc insertProc)
	{
		this.insertProc = insertProc;
	}

	public Map<String, MarketSessionData> getSessionMap()
	{
		if (sessionMap == null)
		{
			setSessionMap(new HashMap<String, MarketSessionData>());
		}
		return sessionMap;
	}

	public void setSessionMap(Map<String, MarketSessionData> sessionMap)
	{
		this.sessionMap = sessionMap;
	}

	public int[] getRetrieveTime()
	{
		return retrieveTime;
	}

	public void setRetrieveTime(int[] retrieveTime)
	{
		this.retrieveTime = retrieveTime;
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
