package com.cyanspring.adaptor.future.wind;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.Network.Transport.FDTFrameDecoder;
import com.cyanspring.adaptor.future.wind.data.*;
import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IndexSessionType;
import com.cyanspring.common.util.DualMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WindGateWayAdapter implements IMarketDataAdaptor, IReqThreadCallback
        , IWindGWListener, IAsyncEventListener {

    private static final Logger log = LoggerFactory
            .getLogger(WindGateWayAdapter.class);

    private String gatewayIp = "";
    private int gatewayPort = 0;
    private boolean showGui = false;
    private boolean marketDataLog = false; // log control
    protected long timerInterval = 5000;
    private volatile Date bigSessionCloseDate = Clock.getInstance().now();
    private boolean closeOverTimeControlIsOpen = true;
    private boolean tradeDateCheckIsOpen = true;
    private boolean msgPack = false;
    private boolean isSubTrans = false;
    private boolean modifyTickTime = true;
    private boolean useRefDataCodeSubscribe = false;
    private List<String> marketsList;
    private String id = "W";

    private boolean isAlive = false;
    EventLoopGroup eventLoopGroup = null;

    @Autowired
    protected IRemoteEventManager eventManager;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected ScheduleManager scheduleManager = new ScheduleManager();
    protected WindDataParser windDataParser = new WindDataParser();

    private ConcurrentHashMap<String, FutureData> futureDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, StockData> stockDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IndexData> indexDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, TransationData> transationDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Quote> lastQuoteBySymbolMap = new ConcurrentHashMap<>(); // LastQuoteData
    private ConcurrentHashMap<String, DataObject> lastQuoteExtendBySymbolMap = new ConcurrentHashMap<>(); // LastQuoteExt
    private ConcurrentHashMap<String, MarketSessionData> marketSessionByIndexMap = new ConcurrentHashMap<>(); //SaveIndexMarketSession
    private ConcurrentHashMap<String, String> marketRuleBySymbolMap = new ConcurrentHashMap<>(); // SaveSymbolRule
    private ConcurrentHashMap<String, WindIndexSessionCheckData> indexSessionCheckDataByIndexMap = new ConcurrentHashMap<>();
    protected HashMap<String, DataTimeStat> recordReceiveQuoteInfoBySymbolMap = new HashMap<>(); //calculate dataTimeStat
    private HashMap<String, String> exchangeBySymbols = new HashMap<String,String>();
    private DualMap<String, String> codeBySymbols = new DualMap<String, String>();

    RequestThread thread = null;
    private QuoteMgr quoteMgr = new QuoteMgr(this);
    private ChannelHandlerContext channelHandlerContext;
    private final int doConnect = 0;
    private volatile boolean isConnected = false;

    //Calculate packet use
    private int bufLenMin = 0, bufLenMax = 0, dataReceived = 0, blockCount = 0;
    private long msDiff = 0, msLastTime = 0, throughput = 0;

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
            if (marketSessionData != null && (marketSessionData.getSessionType() == MarketSessionType.CLOSE
                    || marketSessionData.getSessionType() == MarketSessionType.BREAK)) {
                Quote lastQuote = lastQuoteBySymbolMap.get(symbol);
                DataObject lastQuoteExtend = lastQuoteExtendBySymbolMap.get(symbol);
                if (lastQuote != null && !lastQuote.isStale()) {
                    log.debug("Process Symbol Session & Send Stale Final Quote : Symbol=" + symbol);
                    lastQuote.setStale(true);
                    sendInnerQuote(new InnerQuote(QuoteSource.WIND_GENERAL, lastQuote));
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
        if (msgPack) {
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
                break;
            case WindDef.MSG_SYS_MARKET_CLOSE:
            case WindDef.MSG_SYS_QUOTATIONDATE_CHANGE:
                break;
            case WindDef.MSG_DATA_MARKET: {
                StockData stockData = null;
                try {
                    stockData = msgPack
                            ? windDataParser.convertToStockData(inputMessageHashMap, stockDataBySymbolMap)
                            : windDataParser.convertToStockData(in_arr, stockDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                String index = marketRuleBySymbolMap.get(stockData.getWindCode());
                if (stockData.getActionDay() > stockData.getTradingDay()
                        && stockData.getActionDay() == indexSessionCheckDataByIndexMap.get(index).getTradeDateForWindFormat())
                    stockData.setTradingDay(stockData.getActionDay());
                if (!dataCheck("S", stockData.getWindCode(), stockData.getTime(), stockData.getTradingDay(), stockData.getStatus()))
                    return;
                quoteMgr.AddRequest(new Object[]{
                        WindDef.MSG_DATA_MARKET, stockData});
            }
            break;
            case WindDef.MSG_DATA_INDEX: {
                IndexData indexData = null;
                try {
                    indexData = msgPack
                            ? windDataParser.convertToIndexData(inputMessageHashMap, indexDataBySymbolMap)
                            : windDataParser.convertToIndexData(in_arr, indexDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                String index = marketRuleBySymbolMap.get(indexData.getWindCode());
                if (indexData.getActionDay() > indexData.getTradingDay()
                        && indexData.getActionDay() == indexSessionCheckDataByIndexMap.get(index).getTradeDateForWindFormat())
                    indexData.setTradingDay(indexData.getActionDay());
                if (!dataCheck("I", indexData.getWindCode(), indexData.getTime(), indexData.getTradingDay(), -1))
                    return;
                quoteMgr.AddRequest(new Object[]{
                        WindDef.MSG_DATA_INDEX, indexData});
            }
            break;
            case WindDef.MSG_DATA_FUTURE: {
                FutureData futureData = null;
                try {
                    futureData = msgPack
                            ? windDataParser.convertToFutureData(inputMessageHashMap, futureDataBySymbolMap)
                            : windDataParser.convertToFutureData(in_arr, futureDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                if (useRefDataCodeSubscribe) futureData.setPreSettlePrice(futureData.getPreClose());
                if (!dataCheck("F", futureData.getWindCode(), futureData.getTime(), futureData.getTradingDay(), -1))
                    return;
                quoteMgr.AddRequest(new Object[]{
                        WindDef.MSG_DATA_FUTURE, futureData});
            }
            break;
            case WindDef.MSG_DATA_TRANSACTION: {
                TransationData transationData = null;
                try {
                    transationData = msgPack
                            ? windDataParser.convertToTransationData(inputMessageHashMap, transationDataBySymbolMap)
                            : windDataParser.convertToTransationData(in_arr, transationDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                if (!dataCheck("T", transationData.getWindCode(), transationData.getTime(), transationData.getActionDay(), -1))
                    return;
                quoteMgr.AddRequest(new Object[]{
                        WindDef.MSG_DATA_TRANSACTION, transationData});
            }
            break;
            case WindDef.MSG_DATA_ORDERQUEUE:
            case WindDef.MSG_DATA_ORDER:
                break;
            default:
                break;
        }
    }

    private boolean dataCheck(String type, String symbol, long time, int tradingDay, int status) {
        boolean isCorrect = true;
        try {
            String title = "";
            if ("F".equals(type)) title = WindDef.TITLE_FUTURE;
            if ("S".equals(type)) title = WindDef.TITLE_STOCK;
            if ("I".equals(type)) title = WindDef.TITLE_INDEX;
            if ("T".equals(type)) title = WindDef.TITLE_TRANSATION;
            if ("S".equals(type)) {
                switch (status) {
                    case WindDef.STOCK_STATUS_STOP_SYMBOL:
                    case WindDef.STOCK_STATUS_STOP_SYMBOL_2:
                        return true;
                    default:
                        break;
                }
            }
            if (time >= 240000000) {
                log.debug(String.format("%s %s,%s", title,
                        WindDef.WARN_TIME_FORMAT_ERROR, symbol));
                return false;
            }
            if (tradeDateCheckIsOpen) {
                String index = marketRuleBySymbolMap.get(symbol);
                if (indexSessionCheckDataByIndexMap.get(index) == null) {
                    log.error(String.format("%s %s,%s", title,
                            WindDef.ERROR_NO_INDEXSESSION, symbol));
                    return false;
                }
                if (tradingDay != indexSessionCheckDataByIndexMap.get(index).getTradeDateForWindFormat()) {
                    log.debug(String.format("%s %s,%s", title,
                            WindDef.WARN_TRADEDATE_NOT_MATCH, symbol));
                    return false;
                }
            }
            if (closeOverTimeControlIsOpen) {
                String index = marketRuleBySymbolMap.get(symbol);
                WindIndexSessionCheckData windIndexSessionCheckData = indexSessionCheckDataByIndexMap.get(index);
                if (windIndexSessionCheckData.isSessionClose()
                        && TimeUtil.getTimePass(windIndexSessionCheckData.getSessionCloseDate()) > WindDef.ReceiveQuoteTimeInterval) {
                    log.debug(String.format("%s %s,Session Close Time=%s,%s",
                            title, WindDef.WARN_CLOSE_OVER_TIME,
                            bigSessionCloseDate.toString(), symbol));
                    return false;
                }
            }
        }catch (Exception e){
            log.error("data Check:" + e.getMessage() ,e);
            return false;
        }
        return isCorrect;
    }

    public void connect(){
        log.debug(id + " Run Netty WindGW Adapter");
        eventLoopGroup = new NioEventLoopGroup(2);
        ChannelFuture f;
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(msgPack ? new MsgPackClientInitializer(this) : new ClientInitializer(this));

        try {
            while (isAlive) {
                try {
                    f = bootstrap.connect(gatewayIp, gatewayPort);
                    f.awaitUninterruptibly();
                    if (f.isCancelled()) {
                        log.info(id + " Connection attempt cancelled by user");
                    } else if (!f.isSuccess()) {
                        log.warn(f.cause().getMessage());
                    } else {
                        f.channel().closeFuture().sync();
                    }
                    if (!isAlive) return;
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
                log.info(id + " WindGW Adapter disconnect with - " + gatewayIp + " : " + gatewayPort + " , will try again after 3 seconds.");
                Thread.sleep(3000);
            }
        } catch (InterruptedException ie) {
            log.warn(ie.getMessage(), ie);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (eventLoopGroup != null) eventLoopGroup.shutdownGracefully();
        }
    }

    public void updateState(boolean connected) {
        if (isAlive) sendState(connected);
    }

    /**
     * Send connection State
     *
     * @param on
     */
    public void sendState(boolean on) {
        for (IMarketDataStateListener listener : stateList) {
            log.debug("IMarketDataStateListener = " + listener.getClass());
            listener.onState(on, this);
        }
    }

    public void closeClient() {
        log.info(id + " Wind close client begin");
        if (eventLoopGroup != null) {
            io.netty.util.concurrent.Future<?> f = eventLoopGroup
                    .shutdownGracefully();
            try {
                f.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            eventLoopGroup = null;
        }
        log.info(id + " Wind close client end");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void doConnect() {
        this.addReqData(doConnect);
    }

    @Override
    public void init() throws Exception {
        isAlive = true;

        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName(id + " WFDA eventProcessor");

        quoteMgr.init();
        quoteMgr.setModifyTickTime(modifyTickTime);
        initReqThread();
        doConnect();

        if(marketsList != null) Collections.sort(marketsList);

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    @Override
    public void uninit() {
        log.info(id + " Wind uninit begin");
        isAlive = false;
        printDataTimeStat();
        closeClient();
        quoteMgr.uninit();
        closeReqThread();
        if (!eventProcessor.isSync())
            scheduleManager.uninit();
        log.info(id + " Wind uninit end");
    }

    @Override
    public synchronized boolean getState() {
        return isConnected;
    }

    @Override
    public void subscribeMarketDataState(IMarketDataStateListener listener) {
        if (!stateList.contains(listener)) {
            listener.onState(isConnected, this);
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
        checkUserClient(symbol, listener, true);
        if(useRefDataCodeSubscribe){
            if(codeBySymbols != null){
                try {
                    symbol = codeBySymbols.get(symbol);
                }catch (Exception e){
                    log.error(e.getMessage());
                    return;
                }
            }
        }
        //Check Exchange
        if(marketsList!= null){
            if(exchangeBySymbols.get(symbol) != null) {
                int index = Collections.binarySearch(marketsList, exchangeBySymbols.get(symbol));
                if (index < 0) return;
            }else {
                log.debug(id + " Symbol No exchange in RefData," + symbol);
                return;
            }
        }
        log.info(id + " subscribeMarketData Symbol: " + symbol);
        if (!quoteMgr.checkSymbol(symbol)) {
            subscribe(symbol);
        }
    }

    /**
     * Check UserClient Symbol & Process
     * @param symbol
     * @param listener
     * @param controlFlag add:true,remove:false
     */
    public void checkUserClient(String symbol, IMarketDataListener listener, boolean controlFlag){
        boolean bFound = false;
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients)
            if (client.listener == listener) {
                if(controlFlag){
                    client.addSymbol(symbol);
                }else{
                    client.removeSymbol(symbol);
                }
                bFound = true;
                break;
            }

        if (!bFound) {
            UserClient client = new UserClient(listener);
            if(controlFlag) {
                client.addSymbol(symbol);
            }else{
                client.removeSymbol(symbol);
            }
            clientsList.add(client);
        }
    }

    @Override
    public void unsubscribeMarketData(String instrument,
                                      IMarketDataListener listener) {
        checkUserClient(instrument, listener, false);
    }

    @Override
    public void subscribeMultiMarketData(List<String> subscribeList, IMarketDataListener listener) throws MarketDataException {
        if (subscribeList == null || subscribeList.size() == 0) return;
        StringBuffer sb = new StringBuffer();
        for(String symbol : subscribeList){
            checkUserClient(symbol, listener, true);
            if(useRefDataCodeSubscribe){
                if(codeBySymbols != null){
                    try {
                        symbol = codeBySymbols.get(symbol);
                    }catch (Exception e){
                        log.error(e.getMessage());
                        continue;
                    }
                }
            }
            //Check Exchange
            if(marketsList!= null && exchangeBySymbols.get(symbol) != null){
                int index = Collections.binarySearch(marketsList, exchangeBySymbols.get(symbol));
                if(index < 0) continue;
            }
            if(sb.length()+symbol.length() >= WindDef.SUBSCRIBE_MAX_LENGTH){
                subscribe(sb.toString());
                sb = new StringBuffer();
            }
            if(sb.toString().equals("")){
                sb.append(symbol);
            }else{
                sb.append(";").append(symbol);
            }
        }
        if(sb.length() > 0){
            subscribe(sb.toString());
        }
    }

    static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                            list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    @Override
    public void unsubscribeMultiMarketData(List<String> unSubscribeList, IMarketDataListener listener) {
        for (String symbol : unSubscribeList) {
            unsubscribeMarketData(symbol, listener);
        }
    }

    public void sendTrade(Trade trade) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendTrade(trade);
        }
    }

    public void sendInnerQuote(InnerQuote innerQuote) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        if(useRefDataCodeSubscribe){
            if(codeBySymbols != null){
                try {
                    String subscribeSymbol = innerQuote.getSymbol();
                    innerQuote.getQuote().setSymbol(codeBySymbols.getKeyByValue(subscribeSymbol));
                }catch (Exception e){
                    log.error(e.getMessage());
                    return;
                }
            }
        }
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote);
        }
    }

    public void sendQuoteExtend(DataObject quoteExtend) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        if(useRefDataCodeSubscribe){
            if(codeBySymbols != null){
                try {
                    String subscribeSymbol = quoteExtend.get(String.class, QuoteExtDataField.SYMBOL.value());
                    quoteExtend.put(QuoteExtDataField.SYMBOL.value(), codeBySymbols.getKeyByValue(subscribeSymbol));
                }catch (Exception e){
                    log.error(e.getMessage());
                    return;
                }
            }
        }
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

    public void inputRefDataList(List<RefData> refDataList){
        for (RefData refData : refDataList) {
            if(refData.getIndexSessionType() == null || "".equals(refData.getIndexSessionType()))
                continue;

            String symbol = refData.getSymbol();
            if(useRefDataCodeSubscribe){
                String code = refData.getCode();
                if(code != null) {
                    codeBySymbols.put(refData.getSymbol(), refData.getCode());
                    symbol = code;
                }else{
                    log.error("Symbol:" + refData.getSymbol() + " refData code is null");
                    continue;
                }
            }
            String exchange = refData.getExchange();
            if(exchange != null ) exchangeBySymbols.put(symbol, exchange);
            String indexSessionType = refData.getIndexSessionType();
            if(IndexSessionType.EXCHANGE.name().equals(indexSessionType)){
                marketRuleBySymbolMap.put(symbol, refData.getExchange());
            }
            if(IndexSessionType.SPOT.name().equals(indexSessionType)){
                marketRuleBySymbolMap.put(symbol, refData.getCategory());
            }
            if(IndexSessionType.SETTLEMENT.name().equals(indexSessionType)){
                marketRuleBySymbolMap.put(symbol, refData.getSymbol());
            }
        }
    }

    public void printDataTimeStat() {
        //print time stat log
        if (recordReceiveQuoteInfoBySymbolMap != null && recordReceiveQuoteInfoBySymbolMap.size() > 0) {
            for (DataTimeStat dataTimeStat : recordReceiveQuoteInfoBySymbolMap.values()) {
                dataTimeStat.printStat();
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
        transationDataBySymbolMap.clear();
        FutureItem.futureItemBySymbolMap.clear();
        StockItem.stockItemBySymbolMap.clear();
        IndexItem.indexItemBySymbolMap.clear();
        TransationItem.transationItemBySymbolMap.clear();
        sendClearSubscribe();
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
            log.info(String.format(id + " connect to WindGW %s:%d",
                    gatewayIp, gatewayPort));
            connect();
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

    @Override
    public void processChannelActive(ChannelHandlerContext ctx) {
        sendReqHeartbeat(); // send request heartbeat message
        msLastTime = System.currentTimeMillis();
        isConnected = true;
        updateState(isConnected);
    }

    @Override
    public void processChannelRead(Object msg) {
        if (msgPack) {
            if (msg instanceof HashMap) {
                processMsgPackRead((HashMap) msg);
                if (calculateMessageFlow(FDTFrameDecoder.getPacketLen(), FDTFrameDecoder.getReceivedBytes()))
                    FDTFrameDecoder.ResetCounter();
            }
        } else {
            if (msg instanceof String) {
                String msgStr = (String) msg;
                processNoMsgPackRead(msgStr);
                if (calculateMessageFlow(msgStr.length(), dataReceived)) dataReceived = 0;
            }
        }
    }

    /* AsyncEventListener */

    @Override
    public void onEvent(AsyncEvent event) {
        log.debug(id + " Receive event = " + event.getClass().getSimpleName());

        if (event instanceof RefDataEvent) {
            RefDataEvent refDataEvent = (RefDataEvent) event;
            inputRefDataList(refDataEvent.getRefDataList());
        }

        if(event instanceof RefDataUpdateEvent){
            RefDataUpdateEvent refDataUpdateEvent = (RefDataUpdateEvent) event;
            if(refDataUpdateEvent.getAction() == RefDataUpdateEvent.Action.ADD)
                inputRefDataList(refDataUpdateEvent.getRefDataList());
            if(refDataUpdateEvent.getAction() == RefDataUpdateEvent.Action.MOD){
                for(RefData refData : refDataUpdateEvent.getRefDataList()){
                    DataObject quoteExtend = new DataObject();
                    quoteExtend.put(QuoteExtDataField.SYMBOL.value(), refData.getSymbol());
                    quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(), Clock.getInstance().now());
                    quoteExtend.put(QuoteExtDataField.CNNAME.value(), refData.getCNDisplayName());
                    quoteExtend.put(QuoteExtDataField.TWNAME.value(), refData.getTWDisplayName());
                    sendQuoteExtend(quoteExtend);
                }
            }

        }

        if (event instanceof IndexSessionEvent) {
            IndexSessionEvent indexSessionEvent = (IndexSessionEvent) event;
            for (String index : indexSessionEvent.getDataMap().keySet()) {
                MarketSessionData marketSessionData = indexSessionEvent.getDataMap().get(index);
                marketSessionByIndexMap.put(index, marketSessionData);
                int tradeDateForWindFormat = Integer.parseInt(marketSessionData.getTradeDateByString().replace(
                        "-", ""));
                LogUtil.logInfo(
                        log,
                        "ProcessIndexMarketSession:" + index + "," + marketSessionData.getTradeDateByString() + ","
                                + marketSessionData.getSessionType() + ",Windformat="
                                + tradeDateForWindFormat + "," + marketSessionData.getStart() + ","
                                + marketSessionData.getEnd());
                WindIndexSessionCheckData windIndexSessionCheckData = new WindIndexSessionCheckData(index);
                windIndexSessionCheckData.setTradeDateForWindFormat(tradeDateForWindFormat);
                MarketSessionType marketSessionType = marketSessionData.getSessionType();
                if (marketSessionType == MarketSessionType.CLOSE) {
                    windIndexSessionCheckData.setSessionClose(true);
                    try {
                        windIndexSessionCheckData.setSessionCloseDate(marketSessionData.getStartDate());
                    } catch (ParseException e) {
                        log.error(e.getMessage());
                    }
                } else {
                    windIndexSessionCheckData.setSessionClose(false);
                }
                indexSessionCheckDataByIndexMap.put(index, windIndexSessionCheckData);
            }
        }
    }

    /* netty process method*/

    public void sendData(String data) {
        if (!msgPack) data = data + "\r\n";
        this.channelHandlerContext.channel().writeAndFlush(data);
    }

    public void sendClearSubscribe() {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append("API");
        sbSymbol.append("ClearSubscribe");

        String subscribeStr = sbSymbol.toString();

        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        LogUtil.logInfo(log, "[sendClearSubscribe]%s", subscribeStr);

        sendData(subscribeStr);
    }

    public void subscribe(String symbol) {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');
        sbSymbol.append("API");
        sbSymbol.append("SUBSCRIBE");
        sbSymbol.append("Symbol");
        sbSymbol.append(symbol);
        String subscribeStr = sbSymbol.toString();
        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        LogUtil.logInfo(log, "[Subscribe]%s", subscribeStr);
        sendData(subscribeStr);

        if (isSubTrans) {
            sbSymbol = new FixStringBuilder('=', '|');
            sbSymbol.append("API");
            sbSymbol.append("SubsTrans");
            sbSymbol.append("Symbol");
            sbSymbol.append(symbol);
            subscribeStr = sbSymbol.toString();
            subscribeStr = subscribeStr + "|Hash="
                    + String.valueOf(subscribeStr.hashCode());
            LogUtil.logInfo(log, "[Subscribe]%s", subscribeStr);
            sendData(subscribeStr);
        }

    }

    public void unSubscribe(String symbol) {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');
        sbSymbol.append("API");
        sbSymbol.append("UNSUBSCRIBE");
        sbSymbol.append("Symbol");
        sbSymbol.append(symbol);

        String unsubscribeStr = sbSymbol.toString();
        unsubscribeStr = unsubscribeStr + "|Hash="
                + String.valueOf(unsubscribeStr.hashCode());
        LogUtil.logInfo(log, "[UnSubscribe]%s", unsubscribeStr);
        sendData(unsubscribeStr);
    }
    public void sendRequestCodeTable(String market) {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("GetCodeTable");
        fsb.append("Market");
        fsb.append(market);
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[RequestCodeTable]%s", fsb.toString());
        sendData(fsb.toString());
    }

    public void sendRequestMarket() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("GetMarkets");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[RequestMarket]%s", fsb.toString());
        sendData(fsb.toString());
    }

    public void sendReqHeartbeat() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');
        fsb.append("API");
        fsb.append("ReqHeartBeat");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));

        LogUtil.logInfo(log, "[ReqHeartBeat]%s", fsb.toString());
        sendData(fsb.toString());
    }

    public void processMsgPackRead(HashMap hashMap) {
        if (marketDataLog){
            StringBuffer sb = new StringBuffer();
            for (Object key : hashMap.keySet()) {
                if((int)key == FDTFields.WindSymbolCode) {
                    String symbol = "";
                    try {
                        symbol = new String((byte[]) hashMap.get(key), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.warn("windCode convert X!");
                    }
                    sb.append(key + "=" + symbol + ",");
                }else{
                    sb.append(key + "=" + hashMap.get(key) + ",");
                }
            }
            log.debug(sb.toString());
        }
        int packType = (int) hashMap.get(FDTFields.PacketType);
        if (packType == FDTFields.PacketArray) {
            ArrayList<HashMap> arrayList = (ArrayList<HashMap>) hashMap.get(FDTFields.ArrayOfPacket);
            for (HashMap innerHashMap : arrayList) {
                processGateWayMessage(parsePackTypeToDataType((int) innerHashMap.get(FDTFields.PacketType), innerHashMap), null, innerHashMap);
            }
        } else {
            processGateWayMessage(parsePackTypeToDataType(packType, hashMap), null, hashMap);
        }
    }

    public int parsePackTypeToDataType(int packType, HashMap hashMap) {
        int dataType = -1;
        if (packType == FDTFields.WindFutureData) dataType = WindDef.MSG_DATA_FUTURE;
        if (packType == FDTFields.WindMarketData) dataType = WindDef.MSG_DATA_MARKET;
        if (packType == FDTFields.WindIndexData) dataType = WindDef.MSG_DATA_INDEX;
        if (packType == FDTFields.WindTransaction) dataType = WindDef.MSG_DATA_TRANSACTION;
        if (hashMap.get(FDTFields.WindSymbolCode) == null) dataType = -1;
        return dataType;
    }

    public void processNoMsgPackRead(String in) {
        String strHash = null;
        String strDataType = null;
        int dataType = -1;
        if (in != null) {
            String[] in_arr = in.split("\\|");
            for (String str : in_arr) {
                if (str.contains("API=")) {
                    strDataType = str.substring(4);
                }
                if (str.contains("Hash=")) {
                    strHash = str.substring(5);
                }
            }
            int endindex = in.indexOf("|Hash=");
            if (endindex > 0) {
                String tempStr = in.substring(0, endindex);
                int hascode = tempStr.hashCode();

                // Compare hash code
                if (hascode == Integer.parseInt(strHash)) {
                    if (marketDataLog) {
                        LogUtil.logDebug(log, in);
                    }
                    if (strDataType.equals("DATA_FUTURE")) {
                        dataType = WindDef.MSG_DATA_FUTURE;
                    }
                    if (strDataType.equals("DATA_MARKET")) {
                        dataType = WindDef.MSG_DATA_MARKET;
                    }
                    if (strDataType.equals("DATA_INDEX")) {
                        dataType = WindDef.MSG_DATA_INDEX;
                    }
                    if (strDataType.equals("Heart Beat")) {
                        dataType = WindDef.MSG_SYS_HEART_BEAT;
                    }
                    if (strDataType.equals("QDateChange")) {
                        dataType = WindDef.MSG_SYS_QUOTATIONDATE_CHANGE;
                        LogUtil.logDebug(log, in);
                    }
                    if (strDataType.equals("MarketClose")) {
                        dataType = WindDef.MSG_SYS_MARKET_CLOSE;
                        LogUtil.logDebug(log, in);
                    }
                    processGateWayMessage(
                            dataType, in_arr, null);
                }
            }
        }
    }

    private boolean calculateMessageFlow(int rBytes, int dataReceived) {
        if (bufLenMin > rBytes) {
            bufLenMin = rBytes;
            log.info("WindC-minimal recv len from wind gateway : " + bufLenMin);
        } else {
            if (bufLenMin == 0) {
                bufLenMin = rBytes;
                log.info("WindC-first time recv len from wind gateway : " + bufLenMin);
            }
        }
        if (bufLenMax < rBytes) {
            bufLenMax = rBytes;
            log.info("WindC-maximal recv len from gateway : " + bufLenMax);
        }

        blockCount += 1;
        msDiff = System.currentTimeMillis() - msLastTime;
        if (msDiff > 1000) {
            msLastTime = System.currentTimeMillis();
            if (throughput < dataReceived * 1000 / msDiff) {
                throughput = dataReceived * 1000 / msDiff;
                if (throughput > 1024) {
                    log.info("WindC-maximal throughput : " + throughput / 1024 + " KB/Sec , " + blockCount + " blocks/Sec");
                } else {
                    log.info("WindC-maximal throughput : " + throughput + " Bytes/Sec , " + blockCount + " blocks/Sec");
                }
            }
            blockCount = 0;
            return true;
        }
        return false;
    }
    /***********************/

    @Override
    public void processChannelInActive() {
        isConnected = false;
        updateState(isConnected);
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
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
        return msgPack;
    }

    public void setMsgPack(boolean msgPack) {
        this.msgPack = msgPack;
    }

    public void setIsSubTrans(boolean isSubTrans) {
        this.isSubTrans = isSubTrans;
    }

    public ConcurrentHashMap<String, String> getMarketRuleBySymbolMap() {
        return marketRuleBySymbolMap;
    }

    public ConcurrentHashMap<String, MarketSessionData> getMarketSessionByIndexMap() {
        return marketSessionByIndexMap;
    }

    public void setMarketsList(List<String> marketsList) {
        this.marketsList = marketsList;
    }

    public void setUseRefDataCodeSubscribe(boolean useRefDataCodeSubscribe) {
        this.useRefDataCodeSubscribe = useRefDataCodeSubscribe;
    }

    public void setId(String id) {
        this.id = id;
    }
}

