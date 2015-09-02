package com.cyanspring.id;

import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * implement IMarketDataAdaptor
 *
 * @author Hudson Chen
 */
public class IdMarketDataAdaptor implements IMarketDataAdaptor, IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    static volatile boolean isConnected = false;
    static NioEventLoopGroup nioEventLoopGroup = null;
    static RequestThread thread = null;

    public static IdMarketDataAdaptor instance = null;
    public int sendHeartBeat = 0;
    private boolean bAlive = true;

    public int getSendHeartBeat() {
        return sendHeartBeat;
    }

    public void setSendHeartBeat(int sendHeartBeat) {
        this.sendHeartBeat = sendHeartBeat;
    }

    Date time = new Date(0);
    String account = "";
    String password = "";
    boolean gateway = false;
    String reqIp = "";
    int reqPort = 0;
    int exch = 0;
    String path = "";
    volatile boolean isClose = false;
    Object m_lock = new Object();
    MarketSessionType marketSessionType = MarketSessionType.DEFAULT;

    List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();
    List<UserClient> clientsList = new ArrayList<UserClient>();
    Hashtable<String, Integer> refTable = new Hashtable<String, Integer>();
    private Map<String, Integer> nonFX;
    private List<String> contributeList;
    private List<String> unContributeList;
    private Map<String,String> pluginContributeBySymbolMap;
    private long throwQuoteTimeInterval = 0;

    @Override
    public void init() throws Exception {
        log.debug("Id Adapter init begin");
        isClose = false;
        if (thread == null) {
            thread = new RequestThread(this, "IdRequestThread");
        }
        thread.start();
        instance = this;
        config();
        QuoteMgr.instance().init();
        Collections.sort(contributeList);
        Collections.sort(unContributeList);
        connect();
    }

    void config() {
        List<String> list = new ArrayList<String>(nonFX.keySet());
        QuoteMgr.instance().initSymbols(list);
        Path path1 = Paths.get("");
        this.path = path1.toAbsolutePath().toString();
        list.clear();
    }

    @Override
    public void uninit() {
        LogUtil.logInfo(log, "Id Adapter uninit begin");
        isClose = true;
        closeClient();
        Parser.instance().close();
        QuoteMgr.instance().close();
        if (thread != null) {
            thread.close();
            thread = null;
        }
        LogUtil.logInfo(log, "Id Adapter uninit end");
    }

    @Override
    public boolean getState() {
        return isConnected;
    }

    public void updateState() {
        if(!isClose) sendState(getState());
    }

    void connect() {
        thread.addRequest(new Object());
    }

    /**
     * connect to Id GateWay
     *
     * @param host connect Host IP
     * @param port connect Host Port
     * @throws Exception
     */
    public void connectGateWay(String host, int port) throws Exception {

        IdMarketDataAdaptor.instance.closeClient();
        Util.addLog(InfoString.ALert, "Id Adapter connect Id GW begin %s:%d", host, port);
        LogUtil.logInfo(log, "Id Adapter connect Id GW begin %s:%d", host, port);


        nioEventLoopGroup = new NioEventLoopGroup();
        ChannelFuture channelFuture;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    public void initChannel(NioSocketChannel nioSocketChannel)
                            throws Exception {
                        ChannelPipeline channelPipeline = nioSocketChannel.pipeline();
                        channelPipeline.addLast("idleStateHandler", new IdleStateHandler(20, instance.getSendHeartBeat(), 0));
                        channelPipeline.addLast(new ClientHandler());
                    }
                });


        while (instance.bAlive) {
            channelFuture = bootstrap.connect(host, port);
            channelFuture.awaitUninterruptibly();
            try {
                if (channelFuture.isCancelled()) {
                    log.info("Connection attempt cancelled by user");
                } else if (!channelFuture.isSuccess()) {
                    log.warn(channelFuture.cause().getMessage());
                } else {
                    channelFuture.channel().closeFuture().sync();
                }

            } catch (InterruptedException e) {
                log.warn(e.getMessage());                
            }

            if(isClose) return;
            log.info("id Data client can not connect with - " + host + " : " + port + " , will try again after 3 seconds.");

            try {
                Thread.sleep(3000);
                if (IdMarketDataAdaptor.instance.marketSessionType == MarketSessionType.CLOSE) {
                    log.info("Market is Closed , wait for pre-open");
                    while (IdMarketDataAdaptor.instance.marketSessionType == MarketSessionType.CLOSE) {
                        Thread.sleep(1000);
                        continue;
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public void closeClient() {
        log.debug("IdMarketDataAdapter close client begin");
        if (nioEventLoopGroup != null) {
            io.netty.util.concurrent.Future<?> f = nioEventLoopGroup.shutdownGracefully();
            try {
                f.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nioEventLoopGroup = null;
        }
        Parser.instance().clearRingbuffer();
        log.info("IdMarketDataAdapter close client end");
    }

    @Override
    public void subscribeMarketDataState(IMarketDataStateListener listener) {
        if (!stateList.contains(listener)) {
            listener.onState(getState(),this);
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
    public void subscribeMarketData(String instrument,
                                    IMarketDataListener listener) throws MarketDataException {

        if (instrument.isEmpty())
            return;

        if (addSymbol(instrument) == true) {
            int exchId = getExch(instrument);
            ClientHandler.subscribe(exchId, instrument);
            QuoteMgr.instance().addSymbol(instrument);
        }

        boolean bFound = false;
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients)
            if (client.listener == listener) {
                client.addSymbol(instrument);
                bFound = true;
                break;
            }

        if (!bFound) {
            UserClient client = new UserClient(listener);
            client.addSymbol(instrument);
            clientsList.add(client);
        }
    }

    int getExch(String symbol) {
        if (nonFX.containsKey(symbol)) {
            return nonFX.get(symbol);
        }
        return exch;

    }

    @Override
    public void unsubscribeMarketData(String instrument,
                                      IMarketDataListener listener) {

        if (removeSymbol(instrument) == true) {
            int exchId = getExch(instrument);
            ClientHandler.unSubscribe(exchId, instrument);
            QuoteMgr.instance().addSymbol(instrument);
        }

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

    @Override
    public void subscribeMultiMarketData(List<String> subscribeList, IMarketDataListener listener) throws MarketDataException {
        for(String symbol:subscribeList){
            subscribeMarketData(symbol, listener);
        }
    }

    @Override
    public void unsubscribeMultiMarketData(List<String> unSubscribeList, IMarketDataListener listener) {
        for(String symbol: unSubscribeList){
            unsubscribeMarketData(symbol, listener);
        }
    }

    /**
     * Send connection State
     *
     * @param on
     */
    public void sendState(boolean on) {
        for (IMarketDataStateListener listener : stateList) {
            listener.onState(on,this);
        }
    }

    /**
     * Send Quote
     *
     * @param innerQuote
     */
    public void sendInnerQuote(InnerQuote innerQuote) {
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote);
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

    boolean addSymbol(String symbol) {
        if (false == refTable.containsKey(symbol)) {
            synchronized (m_lock) {
                refTable.put(symbol, 1);
                return true;
            }
        } else {
            synchronized (m_lock) {
                Integer refCount = refTable.get(symbol);
                refCount++;
                refTable.put(symbol, refCount);
                return false;
            }
        }
    }

    boolean removeSymbol(String symbol) {
        if (refTable.containsKey(symbol) == false) {
            return false;
        } else {
            synchronized (m_lock) {
                Integer refCount = refTable.get(symbol);
                refCount--;
                if (refCount <= 0) {
                    refTable.remove(symbol);
                    return true;
                } else {
                    refTable.put(symbol, refCount);
                    return false;
                }
            }
        }

    }

    /**
     * @param strSymbolID
     * @return
     */
    public String getDataPath(String strSymbolID) {
        return String.format("%s/ticks/%s", path, strSymbolID);
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
        //MarketSession
        if(object instanceof MarketSessionEvent){
            marketSessionType = ((MarketSessionEvent) object).getSession();
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public void onStartEvent(RequestThread sender) {

    }

    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {
        reqObj = null;
        if(isClose) return;
        try {
            thread.removeAllRequest();
            if(!getState()) connectGateWay(getReqIp(), getReqPort());
        } catch (Exception e) {
            LogUtil.logException(log, e);
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {    	
    	bAlive = false;
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
    public Map<String, Integer> getNonFX() {
        return nonFX;
    }

    public void setNonFX(Map<String, Integer> nonFX) {
        this.nonFX = nonFX;
    }

    public List<String> getContributeList() {
        return contributeList;
    }

    public void setContributeList(List<String> contributeList) {
        this.contributeList = contributeList;
    }

    public List<String> getUnContributeList() {
        return unContributeList;
    }

    public void setUnContributeList(List<String> unContributeList) {
        this.unContributeList = unContributeList;
    }

    public Map<String, String> getPluginContributeBySymbolMap() {
        return pluginContributeBySymbolMap;
    }

    public void setPluginContributeBySymbolMap(Map<String, String> pluginContributeBySymbolMap) {
        this.pluginContributeBySymbolMap = pluginContributeBySymbolMap;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date srcTime) {
        time = srcTime;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }

    public int getExch() {
        return exch;
    }

    public void setExch(int exch) {
        this.exch = exch;
    }

    public long getThrowQuoteTimeInterval() {
        return throwQuoteTimeInterval;
    }

    public void setThrowQuoteTimeInterval(long throwQuoteTimeInterval) {
        this.throwQuoteTimeInterval = throwQuoteTimeInterval;
    }
}
