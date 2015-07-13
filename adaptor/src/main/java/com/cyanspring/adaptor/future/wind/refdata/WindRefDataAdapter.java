package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.adaptor.future.wind.data.WindDataParser;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
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

public class WindRefDataAdapter implements IRefDataAdaptor, IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(WindRefDataAdapter.class);

    private String gatewayIp = "10.0.0.20";
    private int gatewayPort = 10048;
    private boolean msgPack = true;
    private boolean isAlive = true;
    static volatile boolean isConnected = false;
    static volatile boolean codeTableIsProcessEnd = false;
    static volatile int serverHeartBeatCountAfterCodeTableCome = -1;
    private boolean marketDataLog = false; // log control
    private List<String> marketsList = new ArrayList();
    protected
    @Resource(name = "refDataStockChinaHashMap")
    HashMap<RefDataField, Object> refDataSCHashMap = new HashMap<>();
    protected
    @Resource(name = "refDataIndexChinaHashMap")
    HashMap<RefDataField, Object> refDataICHashMap = new HashMap<>();
    protected
    @Resource(name = "refDataFutureChinaHashMap")
    HashMap<RefDataField, Object> refDataFCHashMap = new HashMap<>();
    static ConcurrentHashMap<String, CodeTableData> codeTableDataBySymbolMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, RefData> refDataHashMap = new ConcurrentHashMap<>();
    protected WindDataParser windDataParser = new WindDataParser();
    protected static WindRefDataAdapter instance = null;
    EventLoopGroup eventLoopGroup = null;
    RequestThread thread = null;

    private void connect() {
        log.debug("Run Netty RefData Adapter");
        eventLoopGroup = new NioEventLoopGroup(2);
        ChannelFuture f;
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new MsgPackRefDataClientInitializer());

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
                Thread.sleep(3000);
            }
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
            case WindDef.MSG_SYS_CODETABLE_RESULT:
                if (serverHeartBeatCountAfterCodeTableCome <= -1) serverHeartBeatCountAfterCodeTableCome = 0;
                CodeTableData codeTableData = null;
                try {
                    codeTableData = msgPack
                            ? windDataParser.convertToCodeTableData(inputMessageHashMap, codeTableDataBySymbolMap)
                            : windDataParser.convertToCodeTableData(in_arr, codeTableDataBySymbolMap);
                } catch (Exception e) {
                    LogUtil.logException(log, e);
                    return;
                }
                //filter data
                if (codeTableData == null || codeTableData.getCnName().contains("ST") || codeTableData.getSecurityType() > 32) {
                    return;
                }
                codeTableDataBySymbolMap.put(codeTableData.getWindCode(), codeTableData);
                log.debug("CODETABLE INFO:S=" + codeTableData.getWindCode() + ",C=" + codeTableData.getCnName()
                        + ",CT=" + ChineseConvert.StoT(codeTableData.getCnName()) + ",E="
                        + codeTableData.getSecurityExchange() + ",SN=" + codeTableData.getShortName() + ",T=" + codeTableData.getSecurityType()
                        + ",Sp=" + codeTableData.getSpellName());
                RequestMgr.instance().addReqData(new Object[]{datatype, codeTableData});
                break;
            case WindDef.MSG_WINDGW_SERVERHEARTBEAT:
                //check CodeTable done.
                if (serverHeartBeatCountAfterCodeTableCome >= 0) {
                    serverHeartBeatCountAfterCodeTableCome++;
                }
                if (serverHeartBeatCountAfterCodeTableCome >= 2) {
                    codeTableIsProcessEnd = true;
                }
                if (serverHeartBeatCountAfterCodeTableCome < 0){
                    serverHeartBeatCountAfterCodeTableCome--;
                    if(serverHeartBeatCountAfterCodeTableCome < -3){
                        MsgPackRefDataClientHandler.context.close();
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


    @Override
    public void init() {
        isAlive = true;
        instance = this;
        initReqThread();
        RequestMgr.instance().init();
        addReqData(0);
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
    public void uninit() {
        isAlive = false;
        closeNetty();
        RequestMgr.instance().uninit();
        closeReqThread();
        codeTableIsProcessEnd = false;
    }

    @Override
    public void subscribeRefData(IRefDataListener listener) {
        init();
        //Wait CodeTable Process
        log.debug("wait codetable process");
        try {
            while (!codeTableIsProcessEnd) {
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
        }
        //send RefData Listener
        listener.onRefData(new ArrayList<RefData>(refDataHashMap.values()));
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
        if (reqObj == 0 && !isConnected) connect();
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

    public static void main(String[] args) throws InterruptedException {
        String logConfigFile = "conf/windlog4j.xml";
        String configFile = "conf/windRefData.xml";
        DOMConfigurator.configure(logConfigFile);
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
        // start server
        IRefDataAdaptor refDataAdaptor = (WindRefDataAdapter) context.getBean("refDataAdapter");
        refDataAdaptor.init();
//        TimeUnit.SECONDS.sleep(10);
//        refDataAdaptor.uninit();
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

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
