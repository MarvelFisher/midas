package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.*;
import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WindFutureDataAdaptor implements IMarketDataAdaptor,
        IReqThreadCallback {


    private static final Logger log = LoggerFactory
            .getLogger(WindFutureDataAdaptor.class);


    private String gatewayIp = "";
    private int gatewayPort = 0;
    private boolean gateway = false;
    private boolean showGui = false;
    private boolean marketDataLog = false; // log control
    private String marketType = "";
    protected long timerInterval = 5000;
    static final long SmallSessionTimeInterval = 30 * 60 * 1000;
    static final int AM10 = 100000000;
    static volatile boolean bigSessionIsClose = false;
    static volatile int tradeDateForWindFormat = 0;
    static volatile Date bigSessionCloseDate = Clock.getInstance().now();
    static final int ReceiveQuoteTimeInterval = 30 * 60 * 1000;
    private boolean closeOverTimeControlIsOpen = true;
    private boolean tradeDateCheckIsOpen = true;
    private final String TITLE_FUTURE = "FUTURE";
    private final String TITLE_STOCK = "STOCK";
    private final String ERR_LAST_LESS_THAN_ZERO = "QUOTE ERROR : Last less than Zero";
    private final String ERR_TRADEDATE_NOT_MATCH = "QUOTE ERROR : Trade NOT match";
    private final String ERR_TIME_FORMAT_ERROR = "QUOTE ERROR : Time format error";
    private final String ERR_CLOSE_OVER_TIME = "QUOTE ERROR : Close Over "
            + ReceiveQuoteTimeInterval / 60 / 1000 + " Time";


    boolean isClose = false;
    static NioEventLoopGroup nioEventLoopGroup = null;

    @Autowired
    protected IRemoteEventManager eventManager;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();

    protected ScheduleManager scheduleManager = new ScheduleManager();

    public static WindFutureDataAdaptor instance = null;

    static ConcurrentHashMap<String, TDF_FUTURE_DATA> futureDataBySymbolMap = new ConcurrentHashMap<String, TDF_FUTURE_DATA>(); // future
    static ConcurrentHashMap<String, TDF_MARKET_DATA> stockDataBySymbolMap = new ConcurrentHashMap<String, TDF_MARKET_DATA>(); // stock
    static ConcurrentHashMap<String, Quote> lastQuoteBySymbolMap = new ConcurrentHashMap<String, Quote>(); // LastQuoteData
    static ConcurrentHashMap<String, DataObject> lastQuoteExtendBySymbolMap = new ConcurrentHashMap<String, DataObject>(); // LastQuoteExt
    static ConcurrentHashMap<String, MarketSessionData> marketSessionByIndexMap = new ConcurrentHashMap<String, MarketSessionData>(); //SaveIndexMarketSession
    static ConcurrentHashMap<String, String> marketRuleBySymbolMap = new ConcurrentHashMap<String, String>(); // SaveSymbolRule


    boolean isClosed = false;
    RequestThread thread = null;

    /*
    API Use Variable
     */

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


    public void setTradeDateCheckIsOpen(boolean tradeDateCheckIsOpen) {
        this.tradeDateCheckIsOpen = tradeDateCheckIsOpen;
    }

    public void setCloseOverTimeControlIsOpen(boolean closeOverTimeControlIsOpen) {
        this.closeOverTimeControlIsOpen = closeOverTimeControlIsOpen;
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

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public int getGatewayPort() {
        return gatewayPort;
    }

    public void setGatewayPort(int gatewayPort) {
        this.gatewayPort = gatewayPort;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }

    public boolean isMarketDataLog() {
        return marketDataLog;
    }

    public void setMarketDataLog(boolean marketDataLog) {
        this.marketDataLog = marketDataLog;
    }

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

//    public void processMarketSession(MarketSessionEvent event) {
//
//    }

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

    public List<SymbolInfo> updateCodeTable(String market) {
        TDF_CODE[] codes = tdfClient.getCodeTable(market);
        List<SymbolInfo> list = new ArrayList<SymbolInfo>();
        try {

            for (TDF_CODE code : codes) {
                SymbolInfo info = FutureItem.processCODE(code);
                String s = printSymbolInfo(info);
                LogUtil.logInfo(log, s);
                list.add(info);
            }

        } catch (Exception e) {
            LogUtil.logException(log, e);
        }
        return list;
    }

    void printCodeTable(String market) {
        TDF_CODE[] codes = tdfClient.getCodeTable(market);
        try {

            for (TDF_CODE code : codes) {
                FutureItem.processCODE(code);
            }
        } catch (Exception e) {
            LogUtil.logException(log, e);
        }
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
                    sendInnerQuote(new InnerQuote(101, lastQuote), lastQuoteExtend);
                }
            }
        }
    }

    public void processGateWayMessage(int datatype, String[] in_arr) {

        if (in_arr == null)
            return;
        switch (datatype) {
            case TDF_MSG_ID.MSG_SYS_HEART_BEAT:
            case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK:
            case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT:
            case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT:
            case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT:
            case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE:
            case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE:
                break;
            case TDF_MSG_ID.MSG_DATA_MARKET:
                TDF_MARKET_DATA stock = WindParser.convertToStockData(in_arr, stockDataBySymbolMap);
                if (stock.getTime() >= 240000000) {
                    log.debug(String.format("%s %s", this.TITLE_STOCK,
                            this.ERR_TIME_FORMAT_ERROR));
                    return;
                }
                if (stock.getMatch() <= 0) {
                    log.debug(String.format("%s %s", this.TITLE_STOCK,
                            this.ERR_LAST_LESS_THAN_ZERO));
                    return;
                }
                if (this.tradeDateCheckIsOpen
                        && stock.getTradingDay() != tradeDateForWindFormat) {
                    log.debug(String.format("%s %s", this.TITLE_STOCK,
                            this.ERR_TRADEDATE_NOT_MATCH));
                    return;
                }
                if (this.closeOverTimeControlIsOpen
                        && bigSessionIsClose
                        && TimeUtil.getTimePass(bigSessionCloseDate) > ReceiveQuoteTimeInterval) {
                    log.debug(String.format("%s %s,Session Close Time=%s",
                            this.TITLE_STOCK, this.ERR_CLOSE_OVER_TIME,
                            bigSessionCloseDate.toString()));
                    return;
                }
                QuoteMgr.instance.AddRequest(new Object[]{
                        TDF_MSG_ID.MSG_DATA_MARKET, stock});
                break;
            case TDF_MSG_ID.MSG_DATA_INDEX:
                break;
            case TDF_MSG_ID.MSG_DATA_FUTURE:
                TDF_FUTURE_DATA future = WindParser.convertToFutureData(in_arr, futureDataBySymbolMap);
                if (future.getTime() >= 240000000) {
                    log.debug(String.format("%s %s", this.TITLE_FUTURE,
                            this.ERR_TIME_FORMAT_ERROR));
                    return;
                }
                if (future.getMatch() <= 0) {
                    log.debug(String.format("%s %s", this.TITLE_FUTURE,
                            this.ERR_LAST_LESS_THAN_ZERO));
                    return;
                }
                if (this.tradeDateCheckIsOpen
                        && future.getTradingDay() != tradeDateForWindFormat) {
                    log.debug(String.format("%s %s", this.TITLE_FUTURE,
                            this.ERR_TRADEDATE_NOT_MATCH));
                    return;
                }
                if (this.closeOverTimeControlIsOpen
                        && bigSessionIsClose
                        && TimeUtil.getTimePass(bigSessionCloseDate) > ReceiveQuoteTimeInterval) {
                    log.debug(String.format("%s %s,Session Close Time=%s",
                            this.TITLE_FUTURE, this.ERR_CLOSE_OVER_TIME,
                            bigSessionCloseDate.toString()));
                    return;
                }
                QuoteMgr.instance.AddRequest(new Object[]{
                        TDF_MSG_ID.MSG_DATA_FUTURE, future});
                break;
            case TDF_MSG_ID.MSG_DATA_TRANSACTION:
            case TDF_MSG_ID.MSG_DATA_ORDERQUEUE:
            case TDF_MSG_ID.MSG_DATA_ORDER:
                break;
            default:
                break;
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

    /**
     * Wind Client link Gateway Server
     *
     * @param ip
     * @param port
     */
    public void connectGateWay(String ip, int port) {

        isConnecting = true;
        WindFutureDataAdaptor.instance.closeClient();
//        Util.addLog(InfoString.ALert, "Wind initClient enter %s:%d", ip, port);
        LogUtil.logInfo(log, "Wind initClient enter %s:%d", ip, port);

        // Configure the client.
        nioEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            ChannelFuture fClient = bootstrap.connect(ip, port).sync();

            if (fClient.isSuccess()) {
                LogUtil.logInfo(log, "client socket connected : %s:%d", ip,
                        port);
//                Util.addLog("client socket connected : %s:%d", ip, port);
            } else {
                LogUtil.logInfo(log, "Connect to %s:%d fail.", ip, port);
//                Util.addLog(InfoString.ALert, "Connect to %s:%d fail.", ip,port);
                isConnecting = true;
                io.netty.util.concurrent.Future<?> f = nioEventLoopGroup
                        .shutdownGracefully();
                f.await();
                nioEventLoopGroup = null;

                fClient.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LogUtil.logDebug(log, "Channel EventLoop Schedule!");
                            WindFutureDataAdaptor.instance.doConnect();
                        } catch (Exception e) {
                            LogUtil.logException(log, e);
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isConnecting = false;
            WindFutureDataAdaptor.instance.closeClient();
            LogUtil.logException(log, e);
//            Util.addLog(InfoString.Error, "Connect to %s:%d fail.[%s]", ip,port, e.getMessage());
        }
    }

    public void updateState(boolean connected) {
        sendState(connected);
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

        log.info("Wind Close Client exit");
//        Util.addLog(InfoString.ALert, "Wind Close Client exit");
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

        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("WFDA eventProcessor");

        WindFutureDataAdaptor.instance = this;

        QuoteMgr.instance.init();
        initReqThread();
        doConnect();

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    @Override
    public void uninit() {
        isClose = true;
        QuoteMgr.instance.uninit();
        closeReqThread();
        if (!eventProcessor.isSync())
            scheduleManager.cancelTimerEvent(timerEvent);

        LogUtil.logInfo(log, "WindFutureDataAdaptor exit");
        closeClient();
        isClose = true;
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

        if (gateway) {
            log.info("subscribeMarketData Symbol: " + symbol);
            // Future
            if ("F".equals(marketType)) {
                if (!QuoteMgr.instance().checkFutureSymbol(symbol)) {
                    ClientHandler.subscribe(symbol);
                }
                QuoteMgr.instance().addFutureSymbol(symbol, null);
            }
            // Stock
            if ("S".equals(marketType)) {
                if (!QuoteMgr.instance().checkStockSymbol(symbol)) {
                    ClientHandler.subscribe(symbol);
                }
                QuoteMgr.instance().addStockSymbol(symbol, null);
            }
        }

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

    /**
     * Send Quote
     *
     * @param innerQuote  inner Quote Data
     * @param quoteExtend Quote Extend Data
     */
    public void sendInnerQuote(InnerQuote innerQuote, DataObject quoteExtend) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote, quoteExtend);
        }
    }

    /**
     * Save Last Quote Data
     *
     * @param quote
     */
    public void saveLastQuote(Quote quote, DataObject quoteExt) {
        lastQuoteBySymbolMap.put(quote.getSymbol(), quote);
        if (quoteExt != null) {
            lastQuoteExtendBySymbolMap.put(quoteExt.get(String.class,
                    QuoteExtDataField.SYMBOL.value()), quoteExt);
        }
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
        if (!symbolList.contains(listener)) {
            // do Action
            List<SymbolInfo> list = FutureItem.getSymbolInfoList();
            List<SymbolInfo> stock_list = StockItem.getSymbolInfoList();
            list.addAll(stock_list);
            listener.onSymbol(list);
            symbolList.add(listener);
        }
    }

    @Override
    public void unsubscribeSymbolData(ISymbolDataListener listener) {
        if (symbolList.contains(listener)) {
            symbolList.remove(listener);
        }
    }

    @Override
    public void refreshSymbolInfo(String market) {
        if (!gateway) {
            List<SymbolInfo> list = updateCodeTable(market);
            sendSymbolInfo(list);
        }
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
            if (gateway) {
                log.info(String.format("connect to Wind GW %s:%d",
                        gatewayIp, gatewayPort));
                connectGateWay(gatewayIp, gatewayPort);
            } else {
                connectUseAPI(reqIp, reqPort, userName, password);
            }
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }
}

