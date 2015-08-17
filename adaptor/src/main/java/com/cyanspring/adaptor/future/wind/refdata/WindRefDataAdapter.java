package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.adaptor.future.wind.IWindGWListener;
import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.adaptor.future.wind.data.WindBaseDBData;
import com.cyanspring.adaptor.future.wind.data.WindDataParser;
import com.cyanspring.adaptor.future.wind.filter.IWindFilter;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    private volatile int serverHeartBeatCountAfterCodeTableCome = -1;
    private volatile int serverRetryCount = 0;
    private volatile int dbRetryCount = 0;
    private boolean marketDataLog = false; // log control
    private List<String> marketsList = new ArrayList();
    private List<String> refFilterList;
    protected
    @Resource(name = "refDataStockChinaHashMap")
    HashMap<RefDataField, Object> refDataSCHashMap = new HashMap<>();
    protected
    @Resource(name = "refDataIndexChinaHashMap")
    HashMap<RefDataField, Object> refDataICHashMap = new HashMap<>();
    protected
    @Resource(name = "refDataFutureChinaHashMap")
    HashMap<RefDataField, Object> refDataFCHashMap = new HashMap<>();
    public static HashMap<String, WindBaseDBData> windBaseDBDataHashMap = new HashMap<>();
    private ConcurrentHashMap<String, CodeTableData> codeTableDataBySymbolMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, RefData> refDataHashMap = new ConcurrentHashMap<>();
    protected WindDataParser windDataParser = new WindDataParser();
    EventLoopGroup eventLoopGroup = null;
    RequestThread thread = null;
    private ChannelHandlerContext channelHandlerContext;
    private RequestMgr requestMgr = new RequestMgr(this);
    private WindDBHandler windDBHandler;
    private IWindFilter windFilter;
    private IRefDataListener refDataListener;

    private void connect() {
        log.debug("Run Netty RefData Adapter");
        eventLoopGroup = new NioEventLoopGroup(2);
        ChannelFuture f;
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
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
            case WindDef.MSG_WINDGW_CONNECTED:
                log.debug("get WindGW connected" + inputMessageHashMap);
                if(null != inputMessageHashMap.get(FDTFields.ArrayOfString)){
                    List<String> marketList = (ArrayList<String>)inputMessageHashMap.get(FDTFields.ArrayOfString);
                    requestMgr.addReqData(new Object[]{datatype, marketList});
                }
                break;
            case WindDef.MSG_SYS_CODETABLE_RESULT:
                if (serverHeartBeatCountAfterCodeTableCome <= -1) serverHeartBeatCountAfterCodeTableCome = 0;
                CodeTableData codeTableData = null;
                WindBaseDBData windBaseDBData = null;

                try {
                    codeTableData = windDataParser.convertToCodeTableData(inputMessageHashMap, codeTableDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }

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
                        //only Stock record log
                        if (codeTableData.getSecurityType() >= 16)
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
                if (marketDataLog) {
                    log.debug("CODETABLE INFO:S=" + codeTableData.getWindCode()
                            + ",CT=" + ChineseConvert.StoT(codeTableData.getCnName()) + ",E="
                            + codeTableData.getSecurityExchange() + ",SN=" + codeTableData.getShortName() + ",T=" + codeTableData.getSecurityType()
                            + ",Sp=" + codeTableData.getSpellName());
                }
                requestMgr.addReqData(new Object[]{datatype, codeTableData});
                break;
            case WindDef.MSG_WINDGW_SERVERHEARTBEAT:
                //check CodeTable done.
                if (serverHeartBeatCountAfterCodeTableCome >= 0) {
                    serverHeartBeatCountAfterCodeTableCome++;
                }
                if (serverHeartBeatCountAfterCodeTableCome >= 2) {
                    codeTableIsProcessEnd = true;
                }
                if (serverHeartBeatCountAfterCodeTableCome < 0) {
                    serverHeartBeatCountAfterCodeTableCome--;
                    if (serverHeartBeatCountAfterCodeTableCome < -3) {
                        channelHandlerContext.close();
                    }
                }
                break;
            default:
                break;
        }
    }

    void initReqThread() {
        if (thread == null) {
            thread = new RequestThread(this, "Wind RefData Adapter");
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
        serverRetryCount = 0;
        dbRetryCount = 0;

        if (windDBHandler != null) {
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
        //connect WindGW
        initReqThread();
        requestMgr.init();
        addReqData(new Integer(0));
    }

    public void processMsgPackRead(HashMap hashMap) {
        StringBuffer sb = new StringBuffer();
        for (Object key : hashMap.keySet()) {
            sb.append(key + "=" + hashMap.get(key) + ",");
        }
        if (marketDataLog) log.debug(sb.toString());
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
        if (packType == FDTFields.WindCodeTable) dataType = WindDef.MSG_SYS_CODETABLE_RESULT;
        if (hashMap.get(FDTFields.WindSymbolCode) == null) dataType = -1;
        if (packType == FDTFields.Heartbeat) dataType = WindDef.MSG_WINDGW_SERVERHEARTBEAT;
        if (packType == FDTFields.WindConnected) dataType = WindDef.MSG_WINDGW_CONNECTED;
        return dataType;
    }

    /**
     * get exchange symbol list
     *
     * @param market
     */
    public String sendRequestCodeTable(String market) {
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

    @Override
    public void uninit() {
        isAlive = false;
        closeNetty();
        requestMgr.uninit();
        closeReqThread();
        codeTableIsProcessEnd = false;
        refDataHashMap.clear();
        codeTableDataBySymbolMap.clear();
    }

    @Override
    public void subscribeRefData(IRefDataListener listener) throws Exception {
        init();
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
        if (serverRetryCount <= WindRefDataAdapter.REFDATA_RETRY_COUNT) {
            log.debug("get RefData from WindGW");
            List<RefData> refDataList = new ArrayList<RefData>(refDataHashMap.values());
            RefDataParser.saveListToFile(refDataFile, refDataList); //Save RefData File
            listener.onRefData(refDataList);
        } else {
            log.debug("get RefData from RefDataFile = " + refDataFile);
            List<RefData> refDataList = RefDataParser.getListFromFile(refDataFile);
            listener.onRefData(refDataList);
        }
    }

    @Override
    public void unsubscribeRefData(IRefDataListener listener) {
        refDataHashMap.clear();
        codeTableDataBySymbolMap.clear();
        uninit();
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
        codeTableIsProcessEnd = false;
        serverHeartBeatCountAfterCodeTableCome = -1;
        //Request CodeTable
        log.debug("request codetable");
        if (marketsList != null && marketsList.size() > 0) {
            for (int i = 0; i < marketsList.size(); i++) {
                ctx.channel().writeAndFlush(sendRequestCodeTable(marketsList.get(i)));
            }
        }
    }

    @Override
    public void processChannelRead(Object msg) {
        processMsgPackRead((HashMap) msg);
    }

    @Override
    public void processChannelInActive() {
        connected = false;
        codeTableIsProcessEnd = false;
        serverHeartBeatCountAfterCodeTableCome = -1;
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
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
        WindRefDataAdapter refDataAdaptor = (WindRefDataAdapter) context.getBean("refDataAdapterSC");
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

    public void setRefDataFile(String refDataFile) {
        this.refDataFile = refDataFile;
    }

    public void setWindbaseDataFile(String windbaseDataFile) {
        this.windbaseDataFile = windbaseDataFile;
    }

    public ConcurrentHashMap<String, RefData> getRefDataHashMap() {
        return refDataHashMap;
    }

    public void setWindDBHandler(WindDBHandler windDBHandler) {
        this.windDBHandler = windDBHandler;
    }

    public void setWindFilter(IWindFilter windFilter) {
        this.windFilter = windFilter;
    }
    public void setRefFilterList(List<String> refFilterList) {
        this.refFilterList = refFilterList;
    }
}
