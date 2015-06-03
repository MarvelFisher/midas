package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.FutureData;
import com.cyanspring.adaptor.future.wind.data.IndexData;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.adaptor.future.wind.data.WindDataParser;
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
import com.cyanspring.common.util.TimeUtil;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WindGateWayAdapter implements IMarketDataAdaptor,
        IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(WindGateWayAdapter.class);

    private String gatewayIp = "";
    private int gatewayPort = 0;
    private boolean showGui = false;
    private boolean marketDataLog = false; // log control
    protected long timerInterval = 5000;
    static volatile boolean bigSessionIsClose = false;
    static volatile int tradeDateForWindFormat = 0;
    static volatile Date bigSessionCloseDate = Clock.getInstance().now();
    private boolean closeOverTimeControlIsOpen = true;
    private boolean tradeDateCheckIsOpen = true;
    private boolean isMsgPack = false;
    private boolean isSubTrans = false;
    private boolean modifyTickTime = true;

    boolean isClose = false;
    static NioEventLoopGroup nioEventLoopGroup = null;

    @Autowired
    protected IRemoteEventManager eventManager;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected ScheduleManager scheduleManager = new ScheduleManager();
    protected WindDataParser windDataParser = new WindDataParser();
    public static WindGateWayAdapter instance = null;

    static ConcurrentHashMap<String, FutureData> futureDataBySymbolMap = new ConcurrentHashMap<String, FutureData>();
    static ConcurrentHashMap<String, StockData> stockDataBySymbolMap = new ConcurrentHashMap<String, StockData>();
    static ConcurrentHashMap<String, IndexData> indexDataBySymbolMap = new ConcurrentHashMap<String, IndexData>();
    static ConcurrentHashMap<String, Quote> lastQuoteBySymbolMap = new ConcurrentHashMap<String, Quote>(); // LastQuoteData
    static ConcurrentHashMap<String, DataObject> lastQuoteExtendBySymbolMap = new ConcurrentHashMap<String, DataObject>(); // LastQuoteExt
    static ConcurrentHashMap<String, MarketSessionData> marketSessionByIndexMap = new ConcurrentHashMap<String, MarketSessionData>(); //SaveIndexMarketSession
    static ConcurrentHashMap<String, String> marketRuleBySymbolMap = new ConcurrentHashMap<String, String>(); // SaveSymbolRule
    static ConcurrentHashMap<String, String> commodityBySymbolMap = new ConcurrentHashMap<String, String>(); //Save Commodity

    RequestThread thread = null;
    private static final int doConnect = 0;
    static volatile boolean isConnected = false;
    static volatile boolean isConnecting = false;

    List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();
    List<UserClient> clientsList = new ArrayList<UserClient>();

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

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


    public void processGateWayMessage(int datatype, String[] in_arr, HashMap<Integer, Object> inputMessageHashMap) {
        if (isMsgPack) {
            if (inputMessageHashMap == null || inputMessageHashMap.size() == 0) return;
        } else {
            if (in_arr == null) return;
        }
        switch (datatype) {
            case WindDef.MSG_SYS_HEART_BEAT:
            case WindDef.MSG_SYS_DISCONNECT_NETWORK:
            case WindDef.MSG_SYS_CONNECT_RESULT:
            case WindDef.MSG_SYS_LOGIN_RESULT:
            case WindDef.MSG_SYS_CODETABLE_RESULT:
            case WindDef.MSG_SYS_MARKET_CLOSE:
            case WindDef.MSG_SYS_QUOTATIONDATE_CHANGE:
                break;
            case WindDef.MSG_DATA_MARKET:
                StockData stockData = windDataParser.convertToStockData(in_arr, stockDataBySymbolMap);
                if (!dataCheck("S", stockData.getWindCode(), stockData.getTime(), stockData.getTradingDay())) return;
                QuoteMgr.instance.AddRequest(new Object[]{
                        WindDef.MSG_DATA_MARKET, stockData});
                break;
            case WindDef.MSG_DATA_INDEX:
                IndexData indexData = isMsgPack
                        ? windDataParser.convertToIndexData(inputMessageHashMap, indexDataBySymbolMap)
                        : windDataParser.convertToIndexData(in_arr, indexDataBySymbolMap);
                if (!dataCheck("I", indexData.getWindCode(), indexData.getTime(), indexData.getTradingDay())) return;
                QuoteMgr.instance.AddRequest(new Object[]{
                        WindDef.MSG_DATA_INDEX, indexData});
                break;
            case WindDef.MSG_DATA_FUTURE:
                FutureData futureData = isMsgPack
                        ? windDataParser.convertToFutureData(inputMessageHashMap, futureDataBySymbolMap)
                        : windDataParser.convertToFutureData(in_arr, futureDataBySymbolMap);
                if (!dataCheck("F", futureData.getWindCode(), futureData.getTime(), futureData.getTradingDay())) return;
                QuoteMgr.instance.AddRequest(new Object[]{
                        WindDef.MSG_DATA_FUTURE, futureData});
                break;
            case WindDef.MSG_DATA_TRANSACTION:
            case WindDef.MSG_DATA_ORDERQUEUE:
            case WindDef.MSG_DATA_ORDER:
                break;
            default:
                break;
        }
    }

    private boolean dataCheck(String type, String symbol, long time, int tradingDay) {
        boolean isCorrect = true;
        String title = "";
        if ("F".equals(type)) title = WindDef.TITLE_FUTURE;
        if ("S".equals(type)) title = WindDef.TITLE_STOCK;
        if ("I".equals(type)) title = WindDef.TITLE_INDEX;
        if (time >= 240000000) {
            log.debug(String.format("%s %s,%s", title,
                    WindDef.WARN_TIME_FORMAT_ERROR, symbol));
            return false;
        }
        if (this.tradeDateCheckIsOpen
                && tradingDay != tradeDateForWindFormat) {
            log.debug(String.format("%s %s,%s", title,
                    WindDef.WARN_TRADEDATE_NOT_MATCH, symbol));
            return false;
        }
        if (this.closeOverTimeControlIsOpen
                && bigSessionIsClose
                && TimeUtil.getTimePass(bigSessionCloseDate) > WindDef.ReceiveQuoteTimeInterval) {
            log.debug(String.format("%s %s,Session Close Time=%s,%s",
                    title, WindDef.WARN_CLOSE_OVER_TIME,
                    bigSessionCloseDate.toString(), symbol));
            return false;
        }
        return isCorrect;
    }

    /**
     * Wind Client link Gateway Server
     *
     * @param ip
     * @param port
     */
    public void connectGateWay(String ip, int port) {

        isConnecting = true;
        WindGateWayAdapter.instance.closeClient();
        LogUtil.logInfo(log, "Wind initClient enter %s:%d", ip, port);

        // Configure the client.
        nioEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(isMsgPack ? new MsgPackClientInitializer() : new ClientInitializer());

            ChannelFuture fClient = bootstrap.connect(ip, port).sync();

            if (fClient.isSuccess()) {
                LogUtil.logInfo(log, "client socket connected : %s:%d", ip,
                        port);
            } else {
                LogUtil.logInfo(log, "Connect to %s:%d fail.", ip, port);
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
                            WindGateWayAdapter.instance.doConnect();
                        } catch (Exception e) {
                            LogUtil.logException(log, e);
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isConnecting = false;
            WindGateWayAdapter.instance.closeClient();
            LogUtil.logException(log, e);
        }
    }

    public void updateState(boolean connected) {
        if (!isClose) sendState(connected);
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

    @Override
    public void init() throws Exception {
        isClose = false;
        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("WFDA eventProcessor");

        WindGateWayAdapter.instance = this;

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

        log.info("subscribeMarketData Symbol: " + symbol);

        String commodity = commodityBySymbolMap.get(symbol);
        // Future
        if ("F".equals(commodity)) {
            if (!QuoteMgr.instance().checkFutureSymbol(symbol)) {
                ClientHandler.subscribe(symbol);
            }
            QuoteMgr.instance().addFutureSymbol(symbol);
        }
        // Stock
        if ("S".equals(commodity)) {
            if (!QuoteMgr.instance().checkStockSymbol(symbol)) {
                ClientHandler.subscribe(symbol);
            }
            QuoteMgr.instance().addStockSymbol(symbol);
        }
        //Index
        if ("I".equals(commodity)) {
            if (!QuoteMgr.instance().checkIndexSymbol(symbol)) {
                ClientHandler.subscribe(symbol);
            }
            QuoteMgr.instance().addIndexSymbol(symbol);
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

    public void sendInnerQuote(InnerQuote innerQuote) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote);
        }
    }

    public void sendQuoteExtend(DataObject quoteExtend) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendQuoteExtend(quoteExtend);
        }
    }

    public void saveLastQuote(Quote quote) {
        lastQuoteBySymbolMap.put(quote.getSymbol(), quote);
    }

    public void saveLastQuoteExtend(DataObject quoteExtend) {
        lastQuoteExtendBySymbolMap.put(quoteExtend.get(String.class, QuoteExtDataField.SYMBOL.value()), quoteExtend);
    }

    @Override
    public void subscirbeSymbolData(ISymbolDataListener listener) {
    }

    @Override
    public void unsubscribeSymbolData(ISymbolDataListener listener) {
    }

    @Override
    public void refreshSymbolInfo(String market) {

    }

    @Override
    public void processEvent(Object object) {

        //RefDataEvent
        if (object instanceof RefDataEvent) {
            log.debug("Wind Adapter Receive RefDataEvent");
            String mainMarket = "S"; //default main market
            RefDataEvent refDataEvent = (RefDataEvent) object;
            for (RefData refData : refDataEvent.getRefDataList()) {
                commodityBySymbolMap.put(refData.getSymbol(), refData.getCommodity());
                if ("S".equals(refData.getCommodity())) {
                    marketRuleBySymbolMap.put(refData.getSymbol(), refData.getStrategy());
                    mainMarket = "S";
                }
                if ("F".equals(refData.getCommodity())) {
                    marketRuleBySymbolMap.put(refData.getSymbol(), refData.getSymbol());
                    mainMarket = "F";
                }
                if ("I".equals(refData.getCommodity())) {
                    if ("S".equals(mainMarket)) marketRuleBySymbolMap.put(refData.getSymbol(), refData.getStrategy());
                    if ("F".equals(mainMarket)) marketRuleBySymbolMap.put(refData.getSymbol(), refData.getSymbol());
                }
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

    public static String printSymbolInfo(SymbolInfo info) {
        FixStringBuilder sb = new FixStringBuilder('=', '|');
        SymbolField field = SymbolField.symbolId;
        sb.append(field.toString());
        sb.append(info.getCode());
        return sb.toString();
    }

    @Override
    public void clean() {
        lastQuoteBySymbolMap.clear();
        lastQuoteExtendBySymbolMap.clear();
        futureDataBySymbolMap.clear();
        stockDataBySymbolMap.clear();
        indexDataBySymbolMap.clear();
        FutureItem.futureItemBySymbolMap.clear();
        StockItem.stockItemBySymbolMap.clear();
        IndexItem.indexItemBySymbolMap.clear();
        ClientHandler.sendClearSubscribe();
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
            log.info(String.format("connect to Wind GW %s:%d",
                    gatewayIp, gatewayPort));
            connectGateWay(gatewayIp, gatewayPort);
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

    public void setTradeDateCheckIsOpen(boolean tradeDateCheckIsOpen) {
        this.tradeDateCheckIsOpen = tradeDateCheckIsOpen;
    }

    public void setCloseOverTimeControlIsOpen(boolean closeOverTimeControlIsOpen) {
        this.closeOverTimeControlIsOpen = closeOverTimeControlIsOpen;
    }

    public boolean isShowGui() {
        return showGui;
    }

    public void setShowGui(boolean showGui) {
        this.showGui = showGui;
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

    public boolean isMarketDataLog() {
        return marketDataLog;
    }

    public void setMarketDataLog(boolean marketDataLog) {
        this.marketDataLog = marketDataLog;
    }

    public boolean isMsgPack() {
        return isMsgPack;
    }

    public void setIsMsgPack(boolean isMsgPack) {
        this.isMsgPack = isMsgPack;
    }

    public void setIsSubTrans(boolean isSubTrans) {
        this.isSubTrans = isSubTrans;
    }

    public boolean isSubTrans() {
        return isSubTrans;
    }
}

