package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.adaptor.future.wind.data.WindBaseDBData;
import com.cyanspring.adaptor.future.wind.data.WindDataParser;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.data.JdbcSQLHandler;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.ChineseConvert;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.LogUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WindRefDataAdapter implements IRefDataAdaptor, IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(WindRefDataAdapter.class);

    public static final int REFDATA_RETRY_COUNT = 2;
    private String gatewayIp = "10.0.0.20";
    private int gatewayPort = 10048;
    private boolean msgPack = true;
    private String refDataFile;
    private boolean isAlive = true;
    static volatile boolean isConnected = false;
    static volatile boolean codeTableIsProcessEnd = false;
    static volatile int serverHeartBeatCountAfterCodeTableCome = -1;
    static volatile int serverRetryCount = 0;
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
    private BasicDataSource basicDataSource;

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
                if (marketDataLog) {
                    log.debug("CODETABLE INFO:S=" + codeTableData.getWindCode() + ",C=" + codeTableData.getCnName()
                            + ",CT=" + ChineseConvert.StoT(codeTableData.getCnName()) + ",E="
                            + codeTableData.getSecurityExchange() + ",SN=" + codeTableData.getShortName() + ",T=" + codeTableData.getSecurityType()
                            + ",Sp=" + codeTableData.getSpellName());
                }
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
                if (serverHeartBeatCountAfterCodeTableCome < 0) {
                    serverHeartBeatCountAfterCodeTableCome--;
                    if (serverHeartBeatCountAfterCodeTableCome < -3) {
                        MsgPackRefDataClientHandler.context.close();
                        serverRetryCount++;
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
        serverRetryCount = 0;
        instance = this;
        //connect WindSyn DB

        //connect WindGW
        initReqThread();
        RequestMgr.instance().init();
        addReqData(new Integer(0));
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
        refDataHashMap.clear();
        codeTableDataBySymbolMap.clear();
    }

    @Override
    public void subscribeRefData(IRefDataListener listener) throws Exception {
        init();
        //Wait CodeTable Process
        log.debug("wait codetable process");
        try {
            if (serverRetryCount <= WindRefDataAdapter.REFDATA_RETRY_COUNT) {
                while (!codeTableIsProcessEnd) {
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        } catch (InterruptedException e) {
        }
        //send RefData Listener
        if (serverRetryCount <= WindRefDataAdapter.REFDATA_RETRY_COUNT) {
            log.debug("get RefData from WindGW");
            listener.onRefData(new ArrayList<RefData>(refDataHashMap.values()));
        } else {
            log.debug("get RefData from RefDataFile = " + refDataFile);
            List<RefData> refDataList = getRefDataListFromFile();
            listener.onRefData(refDataList);
        }
    }

    public List<RefData> getRefDataListFromFile() throws Exception {
        XStream xstream = new XStream(new DomDriver());
        File file = new File(refDataFile);
        List<RefData> list;
        if (file.exists()) {
            list = (List<RefData>) xstream.fromXML(file);
        } else {
            throw new Exception("Missing refdata file: " + refDataFile);
        }
        return list;
    }

    public HashMap<String, WindBaseDBData> processDBTask() {
        log.debug("db process start");
        HashMap<String,WindBaseDBData> windBaseDBDataHashMap = new HashMap<>();
        WindBaseDBData windBaseDBData = null;
        Connection conn = null;
        Statement stmt = null;
        try {
//            BasicDataSource basicDataSource = (BasicDataSource) context.getBean("dataSource");
            JdbcSQLHandler jdbcSQLHandler = new JdbcSQLHandler(basicDataSource);
            conn = jdbcSQLHandler.getConnect();
            stmt = conn.createStatement();
            String sql =
                    "SELECT S_INFO_WINDCODE WINDCODE,S_INFO_NAME CNNAME,IFNULL(S_INFO_COMPNAMEENG,'') ENNAME,S_INFO_PINYIN PINYIN,'S' MARKETTYPE\n" +
                    "from WindFileSync.ASHAREDESCRIPTION\n" +
                    "WHERE S_INFO_EXCHMARKET IN ('SSE','SZSE') AND S_INFO_DELISTDATE IS NULL AND S_INFO_NAME NOT LIKE '%ST%'\n" +
                    "UNION ALL\n" +
                    "SELECT S_INFO_WINDCODE,S_INFO_NAME,'' ENG,'' PINYIN,'I' AS MARKETTYPE\n" +
                    "FROM WindFileSync.AINDEXDESCRIPTION\n" +
                    "WHERE S_INFO_EXCHMARKET IN ('SSE','SZSE') \n" +
                    "AND S_INFO_WINDCODE IN ('399001.SZ','399006.SZ','000905.SH','000016.SH','399300.SZ')\n" +
                    "ORDER BY MARKETTYPE,WINDCODE;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String windcode = rs.getString("WINDCODE");
                String cnName = rs.getString("CNNAME");
                String pinyin = rs.getString("PINYIN");
                String twName = ChineseConvert.StoT(cnName);
                windBaseDBData = new WindBaseDBData();
                windBaseDBData.setSymbol(windcode);
                windBaseDBData.setSpellName(pinyin);
                windBaseDBData.setCNDisplayName(cnName);
                windBaseDBData.setTWDisplayName(twName);
                windBaseDBDataHashMap.put(windcode, windBaseDBData);
                log.debug(",C," + windcode + "," + cnName + "," + twName + "," + pinyin);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        log.debug("db process end");
        return windBaseDBDataHashMap;
    }

    private <T> void saveRefDataToFile(String path, List<T> list) {
        File file = new File(path);
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(list, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
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
            if (signal == 0 && !isConnected) connect();
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

    public static void main(String[] args) throws Exception {
        String logConfigFile = "conf/windlog4j.xml";
        String configFile = "conf/windRefData.xml";
        DOMConfigurator.configure(logConfigFile);
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
        // start server

        log.debug("Process RefData Begin");
        WindRefDataAdapter refDataAdaptor = (WindRefDataAdapter) context.getBean("refDataAdapter");
        HashMap<String, WindBaseDBData> windBaseDBDataHashMap = refDataAdaptor.processDBTask();
        List<RefData> refDataList = refDataAdaptor.getRefDataListFromFile();
        for(RefData refData : refDataList){
            WindBaseDBData windBaseDBData = windBaseDBDataHashMap.get(refData.getSymbol());
            if(windBaseDBData==null){
                log.debug("DB Not this symbol:" + refData.getSymbol());
                continue;
            }
            if(windBaseDBData.getTWDisplayName() != null && !"".equals(windBaseDBData.getTWDisplayName()))
                refData.setTWDisplayName(windBaseDBData.getTWDisplayName());
            if(windBaseDBData.getCNDisplayName() != null && !"".equals(windBaseDBData.getCNDisplayName()))
                refData.setCNDisplayName(windBaseDBData.getCNDisplayName());
            if(windBaseDBData.getSpellName() != null && !"".equals(windBaseDBData.getSpellName()))
                refData.setSpellName(windBaseDBData.getSpellName());
        }
        refDataAdaptor.saveRefDataToFile(refDataAdaptor.refDataFile, refDataList);
        log.debug("Process RefData End");
//        refDataAdaptor.init();
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

    public void setRefDataFile(String refDataFile) {
        this.refDataFile = refDataFile;
    }

    public void setBasicDataSource(BasicDataSource basicDataSource) {
        this.basicDataSource = basicDataSource;
    }
}
