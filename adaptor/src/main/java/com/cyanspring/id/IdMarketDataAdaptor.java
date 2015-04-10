package com.cyanspring.id;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cyanspring.common.marketdata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FileMgr;
import com.cyanspring.id.Library.Util.LogUtil;

/**
 * implement IMarketDataAdaptor
 *
 * @author Hudson Chen
 */
public class IdMarketDataAdaptor implements IMarketDataAdaptor, IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    public static boolean isConnected = false;
    static ChannelFuture fClient = null;
    static RequestThread thread = null;

    public static IdMarketDataAdaptor instance = null;
    public int sendHeartBeat = 0;

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

    int reqPort = 0;
    int preOpen = 0;
    int open = 0;
    int close = 0;
    int exch = 0;

    boolean overNight = false;
    String path = "";
    boolean isClose = false;
    Object m_lock = new Object();

    List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();

    List<UserClient> clientsList = new ArrayList<UserClient>();
    Hashtable<String, Integer> refTable = new Hashtable<String, Integer>();


    private Map<String, Integer> nonFX;

    public Map<String, Integer> getNonFX() {
        return nonFX;
    }


    public void setNonFX(Map<String, Integer> nonFX) {
        this.nonFX = nonFX;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date srcTime) {
        time = srcTime;
    }

    static boolean m_bClose = false;

    public void setIsClose(boolean bClose) {
        m_bClose = bClose;
    }

    public boolean getIsClose() {
        return m_bClose;
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

    public int getPreOpen() {
        return preOpen;
    }

    public void setPreOpen(int preOpen) {
        this.preOpen = preOpen;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
    }

    public int getExch() {
        return exch;
    }

    public void setExch(int exch) {
        this.exch = exch;
    }

    List<String> preSubscriptionList = new ArrayList<String>();

    public List<String> getPreSubscriptionList() {
        return preSubscriptionList;
    }

    public void setPreSubscriptionList(List<String> preSubscriptionList) {
        this.preSubscriptionList = preSubscriptionList;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#init()
     */
    @Override
    public void init() throws Exception {

        if (thread == null) {
            thread = new RequestThread(this, "initClient");
        }
        thread.start();

        instance = this;
        config();

        QuoteMgr.instance().init();

        FileMgr.instance().init();

        connect();

    }

    void config() {

        QuoteMgr.instance().initSymbols(getPreSubscriptionList());
        List<String> list = new ArrayList<String>(nonFX.keySet());
        //list.addAll(nonFX.keySet());
        QuoteMgr.instance().initSymbols(list);

        setSession(getPreOpen(), getOpen(), getClose());

        Path path1 = Paths.get("");

        this.path = path1.toAbsolutePath().toString();
        list.clear();
        list = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#uninit()
     */
    @Override
    public void uninit() {
        isClose = true;
        if (thread != null) {
            thread.close();
            thread = null;
        }
        LogUtil.logInfo(log, "IdMarketDataAdaptor exit");
        closeClient();

        QuoteMgr.instance().close();
        FileMgr.instance().close();

        isClose = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#getState()
     */
    @Override
    public boolean getState() {
        return isConnected;
    }

    public void updateState(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            sendState(connected);
        }
    }

    public void onConnected() {
        ClientHandler.lastCheck = DateUtil.now();
    }

    void connect() {
        thread.addRequest(new Object());
    }

    static boolean isConnecting = false;
    static NioEventLoopGroup clientGroup = null;

    /**
     * initClient
     *
     * @param HOST connect Host IP
     * @param PORT connect Host Port
     * @throws Exception
     */
    public static void onInitClient(final String HOST, final int PORT) throws Exception {

        IdMarketDataAdaptor.instance.closeClient();
        Util.addLog(InfoString.ALert, "Id Adapter initClient enter %s:%d", HOST, PORT);
        LogUtil.logInfo(log, "Id Adapter initClient enter %s:%d", HOST, PORT);

        // Configure the client.
        clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap _clientBootstrap = new Bootstrap();
            _clientBootstrap.group(clientGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("idleStateHandler", new IdleStateHandler(20, instance.getSendHeartBeat(), 0));
                            p.addLast(new ClientHandler());
                        }
                    });

            // connect
            ChannelFuture fClient = _clientBootstrap.connect(HOST, PORT).sync();
            if (fClient.isSuccess()) {
                IdMarketDataAdaptor.instance.onConnected();
                LogUtil.logInfo(log, "Id client socket connected : %s:%d", HOST, PORT);
                Util.addLog("Id client socket connected : %s:%d", HOST, PORT);

                isConnecting = false;
                //isConnected = true;
//                IdMarketDataAdaptor.instance.updateState(true);
            } else {
                LogUtil.logInfo(log, "Id Connect to %s:%d fail.", HOST, PORT);
                Util.addLog(InfoString.ALert, "Id Connect to %s:%d fail.", HOST, PORT);
                isConnecting = true;
                io.netty.util.concurrent.Future<?> f = clientGroup.shutdownGracefully();
                f.await();
                clientGroup = null;

                fClient.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IdMarketDataAdaptor.instance.connect();
                        } catch (Exception e) {
                            LogUtil.logException(log, e);
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isConnecting = false;
            // Shut down the event loop to terminate all threads.
            IdMarketDataAdaptor.instance.closeClient();
            LogUtil.logException(log, e);
            Util.addLog(InfoString.Error, "Id Connect to %s:%d fail.[%s]", HOST,
                    PORT, e.getMessage());
        }
    }

    /**
     *
     */
    public void closeClient() {
        if (clientGroup != null) {
            io.netty.util.concurrent.Future<?> f = clientGroup.shutdownGracefully();
            try {
                f.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clientGroup = null;
        }

        Parser.instance().clearRingbuffer();
        LogUtil.logInfo(log, "Id Adapter initClient exit");
        Util.addLog(InfoString.ALert, "Id Adapter initClient exit");
    }

    /**
     *
     */
    public void reconClient() {

        if (isClose || isConnecting)
            return;

        try {
            Thread.sleep(1000);
            LogUtil.logInfo(log, "Id reconnect %s:%d", getReqIp(), getReqPort());
            connect();
        } catch (Exception e) {
            LogUtil.logException(log, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.common.marketdata.IMarketDataAdaptor#subscribeMarketDataState
     * (com.cyanspring.common.marketdata.IMarketDataStateListener)
     */
    @Override
    public void subscribeMarketDataState(IMarketDataStateListener listener) {
        if (!stateList.contains(listener)) {
            listener.onState(isConnected);
            stateList.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#
     * unsubscribeMarketDataState
     * (com.cyanspring.common.marketdata.IMarketDataStateListener)
     */
    @Override
    public void unsubscribeMarketDataState(IMarketDataStateListener listener) {
        if (stateList.contains(listener)) {
            stateList.remove(listener);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.common.marketdata.IMarketDataAdaptor#subscribeMarketData
     * (java.lang.String, com.cyanspring.common.marketdata.IMarketDataListener)
     */
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

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cyanspring.common.marketdata.IMarketDataAdaptor#unsubscribeMarketData
     * (java.lang.String, com.cyanspring.common.marketdata.IMarketDataListener)
     */
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

    /**
     * Send connection State
     *
     * @param on
     */
    public void sendState(boolean on) {
        for (IMarketDataStateListener listener : stateList) {
            listener.onState(on);
        }
    }

    /**
     * Send Quote
     *
     * @param innerQuote
     */
    public void sendInnerQuote(InnerQuote innerQuote) {
        //UserClient[] clients = new UserClient[clientsList.size()];
        //clients = clientsList.toArray(clients);
        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        for (UserClient client : clients) {
            client.sendInnerQuote(innerQuote);
        }
    }

    public String[] getRefSymbol() {
        List<String> list = new ArrayList<String>();

        List<UserClient> clients = new ArrayList<UserClient>(clientsList);
        //clients = clientsList.toArray(clients);
        for (UserClient client : clients) {
            List<String> listClient = client.getList();
            list.addAll(listClient);
        }

        String[] arr = new String[list.size()];
        list.toArray(arr);
        list.clear();
        return arr;
    }

    /**
     * @param nPreOpen
     * @param nOpen
     * @param nClose
     */
    void setSession(int nPreOpen, int nOpen, int nClose) {
        preOpen = DateUtil.HHMMSS2Time(nPreOpen * 100);
        open = DateUtil.HHMMSS2Time(nOpen * 100);
        close = DateUtil.HHMMSS2Time(nClose * 100);
        overNight = close < preOpen;
    }

    /**
     * @param time
     * @return
     */
    public boolean isValidTime(Date time) {
        int nTime = DateUtil.dateTime2Time(time);

        if (overNight) {
            return nTime >= preOpen || nTime <= close;
        } else {
            return nTime >= preOpen && nTime <= close;
        }
    }

    /**
     * @return
     */
    public int getStatus() {
        return getStatus(new Date());
    }

    /**
     * @param time
     * @return
     */
    public int getStatus(Date time) {
        int nDow = DateUtil.getDayofWeek(time);
        int nTime = DateUtil.dateTime2Time(time);
        if (Calendar.SUNDAY == nDow) // Sunday
        {
            return MarketStatus.CLOSE;
        }

        if (overNight) {
            if (nTime >= open || nTime <= close) {
                if (nTime <= close && Calendar.MONDAY == nDow) // Monday
                {
                    return MarketStatus.CLOSE;
                } else if (nTime >= open && Calendar.SATURDAY == nDow) // Saturday
                {
                    return MarketStatus.CLOSE;
                } else {
                    return MarketStatus.OPEN;
                }
            } else if (nTime >= preOpen && nTime < open) {
                if (Calendar.SATURDAY == nDow) // Saturday
                {
                    return MarketStatus.CLOSE;
                }
                return MarketStatus.PREOPEN;
            } else {
                return MarketStatus.CLOSE;
            }
        } else {
            if (Calendar.SATURDAY == nDow) // Saturday
            {
                return MarketStatus.CLOSE;
            }

            if (nTime >= open && nTime <= close) {
                return MarketStatus.OPEN;
            } else if (nTime >= preOpen || nTime < open) {
                return MarketStatus.PREOPEN;
            } else {
                return MarketStatus.CLOSE;
            }
        }
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
    public void onStartEvent(RequestThread sender) {

    }

    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {
        reqObj = null;
        try {
            thread.removeAllRequest();
            if (isConnected == true)
                return;

            onInitClient(getReqIp(), getReqPort());
        } catch (Exception e) {
            LogUtil.logException(log, e);
        }
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }
}
