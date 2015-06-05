package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.*;
import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class WindAPIAdapter implements IMarketDataAdaptor,
        IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(WindAPIAdapter.class);

    private boolean showGui = false;
    private boolean marketDataLog = false; // log control
    private String marketType = "";
    protected long timerInterval = 5000;
    static volatile boolean bigSessionIsClose = false;
    static volatile int tradeDateForWindFormat = 0;
    static volatile Date bigSessionCloseDate = Clock.getInstance().now();
	private boolean modifyTickTime = true;

    boolean isClose = false;
    static NioEventLoopGroup nioEventLoopGroup = null;

    @Autowired
    protected IRemoteEventManager eventManager;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected ScheduleManager scheduleManager = new ScheduleManager();
    public static WindAPIAdapter instance = null;

    static ConcurrentHashMap<String, TDF_FUTURE_DATA> futureDataBySymbolMap = new ConcurrentHashMap<String, TDF_FUTURE_DATA>(); // future
    static ConcurrentHashMap<String, TDF_MARKET_DATA> stockDataBySymbolMap = new ConcurrentHashMap<String, TDF_MARKET_DATA>(); // stock
    static ConcurrentHashMap<String, Quote> lastQuoteBySymbolMap = new ConcurrentHashMap<String, Quote>(); // LastQuoteData
    static ConcurrentHashMap<String, DataObject> lastQuoteExtendBySymbolMap = new ConcurrentHashMap<String, DataObject>(); // LastQuoteExt
    static ConcurrentHashMap<String, MarketSessionData> marketSessionByIndexMap = new ConcurrentHashMap<String, MarketSessionData>(); //SaveIndexMarketSession
    static ConcurrentHashMap<String, String> marketRuleBySymbolMap = new ConcurrentHashMap<String, String>(); // SaveSymbolRule


    boolean isClosed = false;
    RequestThread thread = null;

    int reqPort = 0;
    String password = "";
    String userName = "";
    String reqIp = "";
    int nId = 0;
    private final String apiOpenMarket = "";
    private final int apiOpenData = 0;
    private final int apiOpenTime = 0;
    private final String apiSubscription = ""; // 000001.SZ;000002.SZ";
    private final int apiOpenTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX; // DATA_TYPE_FLAG.DATA_TYPE_INDEX;
    private static final int doConnect = 0;
    static volatile boolean isConnected = false;
    static volatile boolean isConnecting = false;

    TDFClient tdfClient = new TDFClient();
    TDF_OPEN_SETTING tdf_open_setting = new TDF_OPEN_SETTING();

    public String[] getRefSymbol() {
        List<String> list = new ArrayList<String>();

        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            List<String> listClient = client.getList();
            list.addAll(listClient);
        }

        String[] arr = new String[list.size()];
        list.toArray(arr);
        list.clear();
        return arr;
    }

    void initReqThread() {
        if (thread == null) {
            thread = new RequestThread(this, "Wind Adapter");
        }
        thread.start();
    }

    void closeReqThread() {
        if (thread != null) {
            thread.close();
            thread = null;
        }
    }

    void addReqData(Object objReq) {
        if (thread != null) {
            thread.addRequest(objReq);
        }
    }

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public static String convertGBString(String string) {
        String str = null;
        try {
            str = new String(string.getBytes("MS950"), "GB2312"); // "MS936");

        } catch (UnsupportedEncodingException e1) {
        }
        return str;
    }

    static String getErrMsg(int err) {
        switch (err) {

            case TDF_ERR.TDF_ERR_UNKOWN:
                return String.format("(%d)未知错误", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_INITIALIZE_FAILURE:
                return String.format("(%d)初始化 socket 环境失败", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_NETWORK_ERROR:
                return String.format("(%d)网络连接出现问题", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_INVALID_PARAMS:
                return String.format("(%d)输入参数无效", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_VERIFY_FAILURE:
                return String.format("(%d)登陆验证失败：原因为用户名或者密码错误；超出登陆数量", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_NO_AUTHORIZED_MARKET:
                return String.format("(%d)所有请求的市场都没授权", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_NO_CODE_TABLE:
                return String.format("(%d)所有请求的市场该天都没有代码表", err);
            // Field descriptor #5 I
            case TDF_ERR.TDF_ERR_SUCCESS:
                return String.format("(%d)成功", err);
            default:
                return String.format("(%d)NOT DEFINED", err);
        }
    }

    public TDF_OPTION_CODE getOptionCodeInfo(String szWindCode) {
        return tdfClient.getOptionCodeInfo(szWindCode);
    }

    void disconnect() {
        isClosed = true;
        tdfClient.delete();
    }

    public void reconnect() {
        isClosed = false;
        tdfClient.close();
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {

        // process symbol Market Session
        for (String symbol : marketRuleBySymbolMap.keySet()) {
            MarketSessionData marketSessionData = marketSessionByIndexMap.get(marketRuleBySymbolMap.get(symbol));
            if (marketSessionData != null && marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
                Quote lastQuote = lastQuoteBySymbolMap.get(symbol);
                DataObject lastQuoteExtend = lastQuoteExtendBySymbolMap.get(symbol);
                if (lastQuote != null && !lastQuote.isStale()) {
                    log.debug("Process Symbol Session & Send Stale Final Quote : Symbol=" + symbol);
                    lastQuote.setStale(true);
                    sendInnerQuote(new InnerQuote(101, lastQuote));
                }
            }
        }
    }

    void processAPIMessage() {
        while (this.isConnected()) {
            TDF_MSG msg = tdfClient.getMessage(10);
            if (msg == null)
                continue;

            int type = msg.getDataType();
            switch (type) {
                case TDF_MSG_ID.MSG_SYS_HEART_BEAT:
                    log.debug("MSG_SYS_HEART_BEAT");
                    break;
                case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK:
                    log.error("MSG_SYS_DISCONNECT_NETWORK");
                    isConnected = false;
                    updateState(isConnected);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (!isClosed) {
                        doConnect();
                    }

                    break;
                case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT: {
                    TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
                    log.debug("MSG_SYS_CONNECT_RESULT");
                    TDF_CONNECT_RESULT connect = data.getConnectResult();
                    log.info("connect %s", connect.getConnResult() != 0 ? "success"
                            : "fail");
                    break;
                }
                case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT: {
                    TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
                    TDF_LOGIN_RESULT login = data.getLoginResult();
                    log.info(String.format("login %s", login.getLoginResult() != 0 ? "success"
                            : "fail"));
                    break;
                }
                case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT: {
                    log.info("MSG_SYS_CODETABLE_RESULT");
                    TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
                    String[] markets = data.getCodeTableResult().getMarket();
                    for (String market : markets) {
                        if (!market.isEmpty()) {
                            QuoteMgr.instance.AddRequest(new Object[]{type,
                                    market});
                        }
                    }
                    break;
                }
                case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE: {
                    TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
                    TDF_MARKET_CLOSE close = data.getMarketClose();
                    log.debug("MSG_SYS_MARKET_CLOSE");
                    break;
                }
                case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {
                    TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
                    TDF_QUOTATIONDATE_CHANGE change = data.getDateChange();

                    QuoteMgr.instance.AddRequest(new Object[]{
                            TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE, change});
                    break;
                }
                case TDF_MSG_ID.MSG_DATA_MARKET:
                    // info("MSG_DATA_MARKET");
                    for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
                        TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
                        TDF_MARKET_DATA market = data.getMarketData();
                        QuoteMgr.instance.AddRequest(new Object[]{
                                TDF_MSG_ID.MSG_DATA_MARKET, market});
                    }
                    break;
                case TDF_MSG_ID.MSG_DATA_INDEX:
                    break;
                case TDF_MSG_ID.MSG_DATA_FUTURE:
                    for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
                        TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
                        TDF_FUTURE_DATA future = data.getFutureData();
                        QuoteMgr.instance.AddRequest(new Object[]{
                                TDF_MSG_ID.MSG_DATA_FUTURE, future});
                    }
                    break;
                case TDF_MSG_ID.MSG_DATA_TRANSACTION:
                    log.info("MSG_DATA_TRANSACTION");
                    break;
                case TDF_MSG_ID.MSG_DATA_ORDERQUEUE:
                    break;
                case TDF_MSG_ID.MSG_DATA_ORDER:
                    break;
                default:
                    break;
            }
        }
        tdfClient.close();
    }

    public void updateState(boolean connected) {
        if(!isClose) sendState(connected);
    }

    /**
     * Send connection State
     *
     * @param on
     */
    public void sendState(boolean on) {
        for (IMarketDataStateListener listener : stateList) {
            log.debug("IMarketDataStateListener = " + listener.getClass());
            listener.onState(on);
        }
    }

    public void closeClient() {
        log.info("Wind close client begin");
        if (nioEventLoopGroup != null) {
            io.netty.util.concurrent.Future<?> f = nioEventLoopGroup
                    .shutdownGracefully();
            try {
                f.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nioEventLoopGroup = null;
        }
        log.info("Wind close client end");
    }

    public void connectUseAPI(String ip, int port, String user,
                              String password) {
        connectUseAPI();
        processAPIMessage();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void reconClient() {
        if (isClose || isConnecting)
            return;
        try {
            Thread.sleep(1000);
            doConnect();
        } catch (Exception e) {
            LogUtil.logException(log, e);
        }

    }

    public void doConnect() {
        this.addReqData(doConnect);
    }

    public void connectUseAPI() {

        while (true) {
            tdf_open_setting.setIp(reqIp);
            tdf_open_setting.setPort(Integer.toString(reqPort));
            tdf_open_setting.setUser(userName);
            tdf_open_setting.setPwd(password);
            tdf_open_setting.setReconnectCount(99999999);
            tdf_open_setting.setReconnectGap(10);
            tdf_open_setting.setProtocol(0);
            tdf_open_setting.setMarkets(apiOpenMarket);
            tdf_open_setting.setDate(apiOpenData);
            tdf_open_setting.setTime(apiOpenTime);
            tdf_open_setting.setSubScriptions(apiSubscription);
            tdf_open_setting.setTypeFlags(apiOpenTypeFlags);
            tdf_open_setting.setConnectionID(nId);

            nId = (nId + 1) % 2;
            log.info(String.format("connect to %s:%d", reqIp, reqPort));
            int err = tdfClient.open(tdf_open_setting);
            if (err != TDF_ERR.TDF_ERR_SUCCESS) {

                isConnecting = true;
                log.error(String.format("Can't connect to %s:%d[%s]", reqIp, reqPort,
                        getErrMsg(err)));
                try {
                    if (err == TDF_ERR.TDF_ERR_VERIFY_FAILURE) {
                        disconnect();
                        Thread.sleep(10 * 1000);
                        tdfClient.delete();
                        tdfClient = new TDFClient();

                        addReqData(doConnect);
                        return;

                    } else {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                }
            } else {
                isConnecting = false;
                isConnected = true;
                updateState(isConnected);
                break;
            }
        }
    }

    @Override
    public void init() throws Exception {
        isClose = false;
        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("WFDA eventProcessor");

        WindAPIAdapter.instance = this;

        QuoteMgr.instance.init();
        QuoteMgr.setModifyTickTime(modifyTickTime);
        initReqThread();
        doConnect();

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    @Override
    public void uninit() {
        log.info("Wind uninit begin");
        isClose = true;
        closeClient();
        QuoteMgr.instance.uninit();
        closeReqThread();
        if (!eventProcessor.isSync())
            scheduleManager.uninit();
        log.info("Wind uninit end");
    }

    List<ISymbolDataListener> symbolList = new ArrayList<ISymbolDataListener>();
    List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();

    List<UserClient> clientsList = new ArrayList<UserClient>();

    @Override
    public synchronized boolean getState() {
        return isConnected;
    }

    @Override
    public void subscribeMarketDataState(IMarketDataStateListener listener) {
        if (!stateList.contains(listener)) {
            listener.onState(isConnected);
            stateList.add(listener);
        }
    }

    @Override
    public void unsubscribeMarketDataState(IMarketDataStateListener listener) {
        if (stateList.contains(listener)) {
            stateList.remove(listener);
        }
    }

    @Override
    public void subscribeMarketData(String symbol,
                                    IMarketDataListener listener) throws MarketDataException {

        if (symbol.isEmpty())
            return;

        boolean bFound = false;
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients)
            if (client.listener == listener) {
                client.addSymbol(symbol);
                bFound = true;
                break;
            }

        if (!bFound) {
            UserClient client = new UserClient(listener);
            client.addSymbol(symbol);
            clientsList.add(client);
        }
    }

    @Override
    public void unsubscribeMarketData(String instrument,
                                      IMarketDataListener listener) {

        boolean bFound = false;
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients)
            if (client.listener == listener) {
                client.removeSymbol(instrument);
                bFound = true;
                break;
            }

        if (!bFound) {
            UserClient client = new UserClient(listener);
            client.removeSymbol(instrument);
            clientsList.add(client);
        }
    }

    public void sendInnerQuote(InnerQuote innerQuote){
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote);
        }
    }

    public void sendQuoteExtend(DataObject quoteExtend){
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendQuoteExtend(quoteExtend);
        }
    }

    public void saveLastQuote(Quote quote){
        lastQuoteBySymbolMap.put(quote.getSymbol(),quote);
    }

    public void saveLastQuoteExtend(DataObject quoteExtend){
        lastQuoteExtendBySymbolMap.put(quoteExtend.get(String.class, QuoteExtDataField.SYMBOL.value()), quoteExtend);
    }

    public void sendSymbolInfo(List<SymbolInfo> list) {
        List<ISymbolDataListener> listeners = new ArrayList<ISymbolDataListener>(
                symbolList);
        for (ISymbolDataListener listener : listeners) {
            listener.onSymbol(list);
        }
    }

    @Override
    public void subscirbeSymbolData(ISymbolDataListener listener) {
//        if (!symbolList.contains(listener)) {
//            // do Action
//            List<SymbolInfo> list = FutureItem.getSymbolInfoList();
//            List<SymbolInfo> stock_list = StockItem.getSymbolInfoList();
//            list.addAll(stock_list);
//            listener.onSymbol(list);
//            symbolList.add(listener);
//        }
    }

    @Override
    public void unsubscribeSymbolData(ISymbolDataListener listener) {
//        if (symbolList.contains(listener)) {
//            symbolList.remove(listener);
//        }
    }

    @Override
    public void refreshSymbolInfo(String market) {
//        if (!gateway) {
//            List<SymbolInfo> list = updateCodeTable(market);
//            sendSymbolInfo(list);
//        }
    }

    @Override
    public void processEvent(Object object) {

        //RefDataEvent
        if (object instanceof RefDataEvent)
        {
            log.debug("Wind Adapter Receive RefDataEvent");
            RefDataEvent refDataEvent = (RefDataEvent) object;
            for(RefData refData : refDataEvent.getRefDataList()){
                if("S".equals(marketType))
                    marketRuleBySymbolMap.put(refData.getSymbol(),refData.getExchange());
                if("F".equals(marketType))
                    marketRuleBySymbolMap.put(refData.getSymbol(),refData.getSymbol());
            }
        }

        //IndexSessionEvent
        if (object instanceof IndexSessionEvent) {
            log.debug("Wind Adapter Receive IndexSessionEvent");
            IndexSessionEvent indexSessionEvent = (IndexSessionEvent) object;
            for (String index : indexSessionEvent.getDataMap().keySet()) {
                marketSessionByIndexMap.put(index, indexSessionEvent.getDataMap().get(index));
            }
        }
        //MarketSessionEvent
        if (object instanceof MarketSessionEvent) {
            log.debug("Wind Adapter Receive MarketSessionEvent");
            MarketSessionEvent marketSessionEvent = (MarketSessionEvent) object;
            tradeDateForWindFormat = Integer.parseInt(marketSessionEvent.getTradeDate().replace(
                    "-", ""));
            LogUtil.logInfo(
                    log,
                    "ProcessMarketSession:" + marketSessionEvent.getTradeDate() + ","
                            + marketSessionEvent.getSession() + ",Windformat="
                            + tradeDateForWindFormat + "," + marketSessionEvent.getStart() + ","
                            + marketSessionEvent.getEnd());
            MarketSessionType marketSessionType = marketSessionEvent.getSession();

            if (marketSessionType == MarketSessionType.OPEN || marketSessionType == MarketSessionType.PREOPEN) {
                bigSessionIsClose = false;
            }
            if (marketSessionType == MarketSessionType.CLOSE) {
                bigSessionIsClose = true;
                bigSessionCloseDate = marketSessionEvent.getStart();
            }
        }
    }

    @Override
    public void clean() {
        lastQuoteBySymbolMap.clear();
        lastQuoteExtendBySymbolMap.clear();
        futureDataBySymbolMap.clear();
        stockDataBySymbolMap.clear();
        FutureItem.futureItemBySymbolMap.clear();
        StockItem.stockItemBySymbolMap.clear();
        ClientHandler.sendClearSubscribe();
    }

    public static String printSymbolInfo(SymbolInfo info) {
        FixStringBuilder sb = new FixStringBuilder('=', '|');

        SymbolField field = SymbolField.symbolId;
        sb.append(field.toString());
        sb.append(info.getCode());
        field = SymbolField.market;
        sb.append(field.toString());
        sb.append(info.getMarket());
        field = SymbolField.cnName;
        sb.append(field.toString());
        sb.append(info.getCnName());
        field = SymbolField.enName;
        sb.append(field.toString());
        sb.append(info.getEnName());

        return sb.toString();

    }

    @Override
    public void onStartEvent(RequestThread sender) {

    }

    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {

        int type = (int) reqObj;
        if (type == doConnect) {
            if (isConnected)
                return;
                connectUseAPI(reqIp, reqPort, userName, password);
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

	public boolean isModifyTickTime() {
		return modifyTickTime;
	}

	public void setModifyTickTime(boolean modifyTickTime) {
		this.modifyTickTime = modifyTickTime;
	}

    public String getMarketType() {
        return marketType;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public boolean isShowGui() {
        return showGui;
    }

    public void setShowGui(boolean showGui) {
        this.showGui = showGui;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String account) {
        this.userName = account;
    }

    public String getReqIp() {
        return reqIp;
    }

    public void setReqIp(String reqIp) {
        this.reqIp = reqIp;
    }

    public int getReqPort() {
        return reqPort;
    }

    public void setReqPort(int reqPort) {
        this.reqPort = reqPort;
    }

    public boolean isMarketDataLog() {
        return marketDataLog;
    }

    public void setMarketDataLog(boolean marketDataLog) {
        this.marketDataLog = marketDataLog;
    }
}
