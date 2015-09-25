package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.adaptor.future.wind.IWindGWListener;
import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableResult;
import com.cyanspring.adaptor.future.wind.data.ExchangeRefData;
import com.cyanspring.adaptor.future.wind.data.FutureData;
import com.cyanspring.common.event.marketdata.WindBaseInfoEvent;
import com.cyanspring.common.filter.IRefDataFilter;
import com.cyanspring.common.staticdata.WindBaseDBData;
import com.cyanspring.adaptor.future.wind.data.WindDataParser;
import com.cyanspring.adaptor.future.wind.filter.IWindFilter;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
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
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WindRefDataAdapter implements IRefDataAdaptor, IReqThreadCallback, IWindGWListener {

    private static final Logger log = LoggerFactory
            .getLogger(WindRefDataAdapter.class);

    public static final int REFDATA_RETRY_COUNT = 2;
    public static final int WINDBASEDB_RETRY_COUNT = 2;
    private String gatewayIp;
    private int gatewayPort;
    private boolean msgPack = true;
    private String refDataFile;
    private String windbaseDataFile;
    private boolean isAlive = true;
    private boolean status = false;
    private volatile boolean connected = false;
    private volatile boolean codeTableIsProcessEnd = false;
    private volatile boolean subscribed = false;
    private volatile boolean needsubscribeDataIsReceive = false;
    private volatile int serverHeartBeatCountAfterCodeTableCome = -1;
    private volatile int serverRetryCount = 0;
    private volatile int dbRetryCount = 0;
    private boolean marketDataLog = false; // log control
    private boolean needsubscribe = false;
    private List<String> marketsList = new ArrayList();
    private List<String> refFilterList;
    protected
    @Autowired(required = false) @Qualifier("refDataStockChinaHashMap")
    HashMap<RefDataField, Object> refDataSCHashMap = new HashMap<>();
    protected
    @Autowired(required = false) @Qualifier("refDataIndexChinaHashMap")
    HashMap<RefDataField, Object> refDataICHashMap = new HashMap<>();
    protected
    @Autowired(required = false) @Qualifier("refDataFutureChinaHashMap")
    HashMap<RefDataField, Object> refDataFCHashMap = new HashMap<>();
    protected
    @Autowired(required = false) @Qualifier("refDataFutureTaiwanHashMap")
    HashMap<RefDataField, Object> refDataFTHashMap = new HashMap<>();
    public static HashMap<String, WindBaseDBData> windBaseDBDataHashMap = new HashMap<>();
    private ConcurrentHashMap<String, CodeTableData> codeTableDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CodeTableResult> codeTableResultByExchangeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, RefData> refDataHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ExchangeRefData> exRefDataHashMap = new ConcurrentHashMap<String, ExchangeRefData>();
    private ConcurrentHashMap<String, ExchangeRefData> exRefDataUpdateHashMap = new ConcurrentHashMap<String, ExchangeRefData>();
    protected WindDataParser windDataParser = new WindDataParser();
    EventLoopGroup eventLoopGroup = null;
    private RequestThread thread = null;
    private ChannelHandlerContext channelHandlerContext;
    private RequestMgr requestMgr = new RequestMgr(this);
    private WindDBHandler windDBHandler;
    private IWindFilter windFilter;
    private IRefDataListener refDataListener;
    private IRefDataFilter refDataFilter;
    private String refDataAdapterName = "NoName";
    protected ScheduleManager scheduleManager = new ScheduleManager();
    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private long timerInterval = 1000 * 1;
    private long lastGetDBTime = System.currentTimeMillis();

    @Autowired
    protected IRemoteEventManager eventManager;

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
        if(subscribed){
            //Check Process DB
            Date tempDate = new Date(System.currentTimeMillis());
            String tempDateStr = TimeUtil.formatDate(tempDate, "HH:mm:ss");
            if(tempDate.getTime() > lastGetDBTime && windDBHandler.getExecuteTime().equals(tempDateStr)){
                lastGetDBTime = tempDate.getTime();
                log.debug("Process DB method - " + tempDate);
                processWindDB();
                WindBaseInfoEvent windBaseInfoEvent = new WindBaseInfoEvent(null,null,windBaseDBDataHashMap);
                eventManager.sendEvent(windBaseInfoEvent);
            }
        }
    }

    private void connect() {
        log.debug("Run Netty RefData Adapter-" + Thread.currentThread().getName());
        eventLoopGroup = new NioEventLoopGroup(2);
        ChannelFuture f;
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new MsgPackRefDataClientInitializer(this));

        try {
            while (isAlive) {
                try {
                    f = bootstrap.connect(gatewayIp, gatewayPort);
                    f.awaitUninterruptibly();
                    if (f.isCancelled()) {
                        log.info("Connection attempt cancelled by user");
                    } else if (!f.isSuccess()) {
                        log.warn(f.cause().getMessage());
                    } else {
                        f.channel().closeFuture().sync();
                    }
                    if (!isAlive) return;
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
                log.info("RefData Adapter disconnect with - " + gatewayIp + " : " + gatewayPort + " , will try again after 3 seconds.");
                serverRetryCount++;
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

    public void process(int datatype, String[] in_arr, HashMap<Integer, Object> inputMessageHashMap) {
        if (msgPack) {
            if (inputMessageHashMap == null || inputMessageHashMap.size() == 0) return;
        } else {
            if (in_arr == null) return;
        }
        switch (datatype) {
            case WindDef.MSG_SYS_SNAPSHOTENDS:
                log.debug("recieve WindGW snapshotends");
                requestMgr.addReqData(new Object[]{WindDef.MSG_SYS_SNAPSHOTENDS, new Integer(0)});
                break;
            case WindDef.MSG_DATA_FUTURE:
                if(needsubscribe) {
                    CodeTableData futureCodeTableData = null;
                    try {
                        futureCodeTableData = windDataParser.convertToCodeTableData(inputMessageHashMap, codeTableDataBySymbolMap);
                    } catch (Exception e) {
                        LogUtil.logException(log, e);
                        return;
                    }
                    if (futureCodeTableData == null) return;
                    codeTableDataBySymbolMap.put(futureCodeTableData.getWindCode(), futureCodeTableData);
                    if (marketDataLog) {
                        log.debug("CODETABLE INFO:S=" + futureCodeTableData.getWindCode() + ",C=" + futureCodeTableData.getCnName()
                                        + ",CT=" + ChineseConvert.StoT(futureCodeTableData.getCnName()) + ",E="
                                        + futureCodeTableData.getSecurityExchange() + ",SN=" + futureCodeTableData.getShortName() + ",T=" + futureCodeTableData.getSecurityType()
                                        + ",Sp=" + futureCodeTableData.getSpellName() + ",EN=" + futureCodeTableData.getEnglishName()
                                        + ",Group=" + futureCodeTableData.getGroup() + ",P=" + futureCodeTableData.getProduct()
                                        + ",PN=" + futureCodeTableData.getProductName() + ",SyN=" + futureCodeTableData.getSymbolName()
                                        + ",Cu=" + futureCodeTableData.getCurrency() + ",SID=" + futureCodeTableData.getShowID()
                        );
                    }
                    requestMgr.addReqData(new Object[]{
                            WindDef.MSG_SYS_CODETABLE, futureCodeTableData});
                    needsubscribeDataIsReceive = true;
                }
                break;
            case WindDef.MSG_SYS_MARKETS:
                log.debug("receive WindGW markets");
                break;
            case WindDef.MSG_WINDGW_CONNECTED:
                log.debug("receive WindGW connected");
                break;
            case WindDef.MSG_SYS_CODETABLE_RESULT:
                log.debug("receive WindGW codetable result");
                CodeTableResult codeTableResult = null;
                try {
                    codeTableResult = windDataParser.convertToCodeTableResult(inputMessageHashMap, codeTableResultByExchangeMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                if(codeTableResult == null || !marketsList.contains(codeTableResult.getSecurityExchange())) return;
                codeTableResultByExchangeMap.put(codeTableResult.getSecurityExchange(),codeTableResult);
                log.debug("codeTable exchange = " + codeTableResult.getSecurityExchange() + ",hashCode=" + codeTableResult.getHashCode());
                requestMgr.addReqData(new Object[]{datatype, codeTableResult});
                break;
            case WindDef.MSG_SYS_CODETABLE:
                if (serverHeartBeatCountAfterCodeTableCome <= -1) serverHeartBeatCountAfterCodeTableCome = 0;
                CodeTableData codeTableData = null;
                WindBaseDBData windBaseDBData = null;

                try {
                    codeTableData = windDataParser.convertToCodeTableData(inputMessageHashMap, codeTableDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }

                if(codeTableData == null) return;

                if(windFilter != null) {
                    if(!windFilter.codeTableFilter(codeTableData)) return;
                }

                //filter List process
                if(refFilterList != null && refFilterList.size() > 0){
                    if(!refFilterList.contains(codeTableData.getWindCode())) return;
                }

                //Check WindBaseDB Data
                if(windDBHandler!=null) {
                    String windCode = codeTableData.getWindCode();
                    if (!windBaseDBDataHashMap.containsKey(windCode)) {
                        log.warn("WindBase DB Not this Symbol," + windCode + ",T=" + codeTableData.getSecurityType());
                        return;
                    } else {
                        windBaseDBData = windBaseDBDataHashMap.get(windCode);
                    }
                    codeTableData.setCnName(windBaseDBData.getCNDisplayName());
                    codeTableData.setSpellName(windBaseDBData.getSpellName());
                    codeTableData.setEnglishName(windBaseDBData.getENDisplayName());
                }
                codeTableDataBySymbolMap.put(codeTableData.getWindCode(), codeTableData);
//                if (marketDataLog) {
                    log.debug("CODETABLE INFO:S=" + codeTableData.getWindCode()
                            + ",CT=" + ChineseConvert.StoT(codeTableData.getCnName()) + ",E="
                            + codeTableData.getSecurityExchange() + ",SN=" + codeTableData.getShortName() + ",T=" + codeTableData.getSecurityType()
                            + ",Sp=" + codeTableData.getSpellName() + ",EN=" + codeTableData.getEnglishName());
//                }
                if(!needsubscribe) requestMgr.addReqData(new Object[]{datatype, codeTableData});
                break;
            case WindDef.MSG_WINDGW_SERVERHEARTBEAT:
                //check CodeTable done.
                if (serverHeartBeatCountAfterCodeTableCome >= 0) {
                    serverHeartBeatCountAfterCodeTableCome++;
                }
                if(!subscribed) {
                    if (serverHeartBeatCountAfterCodeTableCome < 0) {
                        serverHeartBeatCountAfterCodeTableCome--;
                        if (serverHeartBeatCountAfterCodeTableCome < -3) {
                            if(needsubscribe && needsubscribeDataIsReceive){
//                                RefDataParser.saveHashMapToFile("ticks/codetable_ft.xml", new HashMap<>(codeTableDataBySymbolMap));
                                codeTableIsProcessEnd = true;
                                serverHeartBeatCountAfterCodeTableCome = -1;
                            }else {
                                channelHandlerContext.close();
                            }
                        }
                    }
                    if (serverHeartBeatCountAfterCodeTableCome >= 2) {
                        if(needsubscribe) {
                            requestMgr.addReqData(new Object[]{WindDef.MSG_SYS_REQUEST_SNAPSHOT, new Integer(0)});
                            serverHeartBeatCountAfterCodeTableCome = -1;
                        }else{
//                            RefDataParser.saveHashMapToFile("ticks/codetable_fci.xml", new HashMap<>(codeTableDataBySymbolMap));
                            codeTableIsProcessEnd = true;
                            serverHeartBeatCountAfterCodeTableCome = -1;
                        }
                    }
                }else {
                    if (serverHeartBeatCountAfterCodeTableCome == 5) {
                        requestMgr.addReqData(new Object[]{WindDef.MSG_REFDATA_CHECKUPDATE, new Integer(0)});
                        serverHeartBeatCountAfterCodeTableCome = -1;
                    }
                }
                break;
            default:
                break;
        }
    }

    void initReqThread(String refDataAdapterName) {
        if (thread == null) {
            thread = new RequestThread(this, "WindRefDataAdapter-" + refDataAdapterName);
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

    public void closeNetty() {
        log.info("Wind RefData close client begin");
        if (eventLoopGroup != null) {
            io.netty.util.concurrent.Future<?> f = eventLoopGroup
                    .shutdownGracefully();
            try {
                f.await();
            } catch (InterruptedException e) {
            }
            eventLoopGroup = null;
        }
        log.info("Wind RefData close client end");
    }

    @Override
    public boolean getStatus() {
        return this.status;
    }

    @Override
    public void flush() {
        this.status = false;
    }

    @Override
    public void init() throws Exception {
        isAlive = true;
        subscribed = false;
        codeTableIsProcessEnd = false;
        needsubscribeDataIsReceive = false;
        serverRetryCount = 0;
        dbRetryCount = 0;
        for(String exchange: marketsList){
            ExchangeRefData exchangeRefData = new ExchangeRefData(exchange);
            exRefDataHashMap.put(exchange,exchangeRefData);
        }
        processWindDB();
        //connect WindGW
        initReqThread(refDataAdapterName);
        requestMgr.init();
        addReqData(new Integer(0));

        if(windDBHandler != null) {
            eventProcessor.setHandler(this);
            eventProcessor.init();
            if (eventProcessor.getThread() != null)
                eventProcessor.getThread().setName("WRDA eP-" + refDataAdapterName);

            if (!eventProcessor.isSync())
                scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                        eventProcessor, timerEvent);
        }
    }

    public void processWindDB(){
        if (windDBHandler != null) {
            dbRetryCount = 0;
            //connect WindSyn DB
            windBaseDBDataHashMap.clear();
            windBaseDBDataHashMap = windDBHandler.getWindBaseDBData();
            while ((windBaseDBDataHashMap == null || windBaseDBDataHashMap.size() == 0)
                    && dbRetryCount <= WindRefDataAdapter.WINDBASEDB_RETRY_COUNT) {
                windBaseDBDataHashMap = windDBHandler.getWindBaseDBData();
                dbRetryCount++;
            }
            if (windBaseDBDataHashMap == null || windBaseDBDataHashMap.size() == 0) {
                //getData from file
                windBaseDBDataHashMap = RefDataParser.getHashMapFromFile(windbaseDataFile);
            } else {
                //write last ExtendFile
                windDBHandler.saveDBDataToQuoteExtendFile(windBaseDBDataHashMap);
                RefDataParser.saveHashMapToFile(windbaseDataFile, windBaseDBDataHashMap);
            }
        }
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
                process(parsePackTypeToDataType((int) innerHashMap.get(FDTFields.PacketType), innerHashMap), null, innerHashMap);
            }
        } else {
            process(parsePackTypeToDataType(packType, hashMap), null, hashMap);
        }
    }

    public int parsePackTypeToDataType(int packType, HashMap hashMap) {
        int dataType = -1;
        if (packType == FDTFields.WindCodeTable) dataType = WindDef.MSG_SYS_CODETABLE;
        if (packType == FDTFields.WindFutureData) dataType = WindDef.MSG_DATA_FUTURE;
        if (hashMap.get(FDTFields.WindSymbolCode) == null) dataType = -1;
        if (packType == FDTFields.Heartbeat) dataType = WindDef.MSG_WINDGW_SERVERHEARTBEAT;
        if (packType == FDTFields.WindConnected) dataType = WindDef.MSG_WINDGW_CONNECTED;
        if (packType == FDTFields.WindCodeTableResult) dataType = WindDef.MSG_SYS_CODETABLE_RESULT;
        if (packType == FDTFields.WindMarkets) dataType = WindDef.MSG_SYS_MARKETS;
        if (packType == FDTFields.SnapShotEnds) dataType = WindDef.MSG_SYS_SNAPSHOTENDS;
        return dataType;
    }

    /**
     * get exchange symbol list
     *
     * @param market
     */
    public String makeRequestCodeTable(String market) {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');
        fsb.append("API");
        fsb.append("GetCodeTable");
        fsb.append("Market");
        fsb.append(market);
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));
        log.info("[RequestCodeTable]" + fsb.toString());
        return fsb.toString();
    }

    public void sendRquestCodeTable(List<String> marketsList){
        if (marketsList != null && marketsList.size() > 0) {
            for (int i = 0; i < marketsList.size(); i++) {
                this.channelHandlerContext.channel().writeAndFlush(makeRequestCodeTable(marketsList.get(i)));
            }
        }
    }

    public void sendClearSubscribe() {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

        sbSymbol.append("API");
        sbSymbol.append("ClearSubscribe");

        String subscribeStr = sbSymbol.toString();

        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        log.debug("[sendClearSubscribe]" + subscribeStr);
        this.channelHandlerContext.channel().writeAndFlush(subscribeStr);
    }

    public void sendSubscribe(String symbol) {
        FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');
        sbSymbol.append("API");
        sbSymbol.append("SUBSCRIBE");
        sbSymbol.append("Symbol");
        sbSymbol.append(symbol);
        String subscribeStr = sbSymbol.toString();
        subscribeStr = subscribeStr + "|Hash="
                + String.valueOf(subscribeStr.hashCode());
        log.debug("[Subscribe]" + subscribeStr);
        this.channelHandlerContext.channel().writeAndFlush(subscribeStr);
    }

    public void sendRefDataUpdate(List<RefData> refDataList, RefDataUpdateEvent.Action action){
        log.info("send RefDataUpdate Size = " + refDataList.size() + ",Action=" + action.name());
        for(RefData refData: refDataList){
            log.debug("S=" + refData.getSymbol() +",CN=" + refData.getCNDisplayName() + ",TW=" + refData.getTWDisplayName());
        }
        if(refDataFilter != null){
            try {
                refDataList = refDataFilter.filter(refDataList);
            }catch (Exception e){
                log.warn(e.getMessage(), e);
            }
        }
        if(refDataListener != null){
            refDataListener.onRefDataUpdate(refDataList, action);
        }
    }

    @Override
    public void uninit() {
        status = false;
        isAlive = false;
        closeNetty();
        requestMgr.uninit();
        closeReqThread();
    }

    @Override
    public void subscribeRefData(IRefDataListener listener) throws Exception {
        //record refDataListener
        this.refDataListener = listener;
        //Wait CodeTable Process
        log.debug("wait codetable process");
        try {
            while (!codeTableIsProcessEnd && serverRetryCount <= WindRefDataAdapter.REFDATA_RETRY_COUNT) {
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
        }
        log.debug("wait codetable end");
        //send RefData Listener
        status = true;
        List<RefData> refDataList;
        if (serverRetryCount <= WindRefDataAdapter.REFDATA_RETRY_COUNT) {
            log.debug("get RefData from WindGW");
            refDataList = new ArrayList<RefData>(refDataHashMap.values());
            RefDataParser.saveListToFile(refDataFile, refDataList); //Save RefData File
        } else {
            log.debug("get RefData from RefDataFile = " + refDataFile);
            refDataList = RefDataParser.getListFromFile(refDataFile);
        }
        //Save Exchange RefData
        log.info("Save exchange refdata begin");
        ExchangeRefData exchangeRefData;
        for(RefData refData : refDataList){
            if(exRefDataHashMap.containsKey(refData.getExchange())){
                exchangeRefData = exRefDataHashMap.get(refData.getExchange());
            }else{
                exchangeRefData = new ExchangeRefData(refData.getExchange());
            }
            exchangeRefData.getRefDataHashMap().put(refData.getSymbol(),refData);
        }
        log.info("Save exchange refdata end");
        if(refDataFilter != null){
            try {
                refDataList = refDataFilter.filter(refDataList);
            }catch (Exception e){
                log.warn(e.getMessage(), e);
            }
        }
        listener.onRefData(refDataList);
        subscribed = true;
    }

    @Override
    public void unsubscribeRefData(IRefDataListener listener) {
    }

    @Override
    public void onStartEvent(RequestThread sender) {

    }

    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {
        if (reqObj instanceof Integer) {
            int signal = (int) reqObj;
            if (signal == 0 && !connected) connect();
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

    @Override
    public void processChannelActive(ChannelHandlerContext ctx) {
        connected = true;
        serverHeartBeatCountAfterCodeTableCome = -1;
    }

    @Override
    public void processChannelRead(Object msg) {
        processMsgPackRead((HashMap) msg);
    }

    @Override
    public void processChannelInActive() {
        connected = false;
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
    }

    @Override
    public void setStatus(boolean status) {
        this.status = status;
    }

    public static void main(String[] args) throws Exception {
        String logConfigFile = "conf/windlog4j.xml";
        String configFile = "conf/windRefData.xml";
        DOMConfigurator.configure(logConfigFile);
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
        // start server
//        refDataAdaptor.init();
//        TimeUnit.SECONDS.sleep(10);
//        refDataAdaptor.uninit();
        //RefData補拼音,簡繁體股名 使用
        log.debug("Process RefData Begin");
        WindRefDataAdapter refDataAdaptor = (WindRefDataAdapter) context.getBean("refDataAdapterFCI");
        refDataAdaptor.init();

//        refDataAdaptor.windBaseDBDataHashMap = refDataAdaptor.getWindBaseDBData();
//        List<RefData> refDataList = refDataAdaptor.getListFromFile(refDataAdaptor.refDataFile);
//        for (RefData refData : refDataList) {
//            WindBaseDBData windBaseDBData = refDataAdaptor.windBaseDBDataHashMap.get(refData.getSymbol());
//            if (windBaseDBData == null) {
//                log.debug("DB Not this symbol:" + refData.getSymbol());
//                continue;
//            }
//            if (windBaseDBData.getCNDisplayName() != null && !"".equals(windBaseDBData.getCNDisplayName())) {
//                refData.setCNDisplayName(windBaseDBData.getCNDisplayName());
//                refData.setTWDisplayName(ChineseConvert.StoT(windBaseDBData.getCNDisplayName()));
//            }
//            if (windBaseDBData.getSpellName() != null && !"".equals(windBaseDBData.getSpellName()))
//                refData.setSpellName(windBaseDBData.getSpellName());
//        }
//        refDataAdaptor.saveListToFile(refDataAdaptor.refDataFile, refDataList);
//        refDataAdaptor.saveHashMapToFile(refDataAdaptor.windbaseDataFile, refDataAdaptor.windBaseDBDataHashMap);
//        log.debug("Process RefData End");
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

    public boolean isMsgPack() {
        return msgPack;
    }

    public void setMsgPack(boolean msgPack) {
        this.msgPack = msgPack;
    }

    public boolean isMarketDataLog() {
        return marketDataLog;
    }

    public void setMarketDataLog(boolean marketDataLog) {
        this.marketDataLog = marketDataLog;
    }

    public List<String> getMarketsList() {
        return marketsList;
    }

    public void setMarketsList(List<String> marketsList) {
        this.marketsList = marketsList;
    }

    public HashMap<RefDataField, Object> getRefDataSCHashMap() {
        return refDataSCHashMap;
    }

    public void setRefDataSCHashMap(HashMap<RefDataField, Object> refDataSCHashMap) {
        this.refDataSCHashMap = refDataSCHashMap;
    }

    public HashMap<RefDataField, Object> getRefDataICHashMap() {
        return refDataICHashMap;
    }

    public void setRefDataICHashMap(HashMap<RefDataField, Object> refDataICHashMap) {
        this.refDataICHashMap = refDataICHashMap;
    }

    public HashMap<RefDataField, Object> getRefDataFCHashMap() {
        return refDataFCHashMap;
    }

    public void setRefDataFCHashMap(HashMap<RefDataField, Object> refDataFCHashMap) {
        this.refDataFCHashMap = refDataFCHashMap;
    }

    public HashMap<RefDataField, Object> getRefDataFTHashMap() {
        return refDataFTHashMap;
    }

    public void setRefDataFTHashMap(HashMap<RefDataField, Object> refDataFTHashMap) {
        this.refDataFTHashMap = refDataFTHashMap;
    }

    public void setRefDataFile(String refDataFile) {
        this.refDataFile = refDataFile;
    }

    public void setWindbaseDataFile(String windbaseDataFile) {
        this.windbaseDataFile = windbaseDataFile;
    }

    public ConcurrentHashMap<String, RefData> getRefDataHashMap() {
        return refDataHashMap;
    }

    public ConcurrentHashMap<String, ExchangeRefData> getExRefDataHashMap() {
        return exRefDataHashMap;
    }

    public ConcurrentHashMap<String, ExchangeRefData> getExRefDataUpdateHashMap() {
        return exRefDataUpdateHashMap;
    }

    public void setWindDBHandler(WindDBHandler windDBHandler) {
        this.windDBHandler = windDBHandler;
    }

    public ConcurrentHashMap<String, CodeTableData> getCodeTableDataBySymbolMap() {
        return codeTableDataBySymbolMap;
    }

    public void setWindFilter(IWindFilter windFilter) {
        this.windFilter = windFilter;
    }

    public void setRefFilterList(List<String> refFilterList) {
        this.refFilterList = refFilterList;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setRefDataAdapterName(String refDataAdapterName) {
        this.refDataAdapterName = refDataAdapterName;
    }

    public void setNeedsubscribe(boolean needsubscribe) {
        this.needsubscribe = needsubscribe;
    }

    public boolean isNeedsubscribe() {
        return needsubscribe;
    }
    public void setRefDataFilter(IRefDataFilter refDataFilter) {
        this.refDataFilter = refDataFilter;
    }
}
