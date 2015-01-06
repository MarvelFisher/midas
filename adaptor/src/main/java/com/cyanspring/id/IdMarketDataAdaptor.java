package com.cyanspring.id;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.CustomThreadPool;
import com.cyanspring.id.Library.Threading.Delegate;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FileMgr;
import com.cyanspring.id.Library.Util.LogUtil;

/**
 * implement IMarketDataAdaptor
 * 
 * @author Hudson Chen
 * 
 */
public class IdMarketDataAdaptor implements IMarketDataAdaptor {

	private static final Logger log = LoggerFactory
			.getLogger(IdMarketDataAdaptor.class);

	public static boolean isConnected = false;
	static ChannelFuture fClient = null;
	static final Method methodClient = Delegate.getMethod("initClient",
			IdMarketDataAdaptor.class, new Class[] { String.class, int.class });

	public static IdMarketDataAdaptor instance = null;

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

		instance = this;
		config();

		QuoteMgr.instance().init();

		FileMgr.instance().init();

		CustomThreadPool.asyncMethod(methodClient, getReqIp(), getReqPort());

	}

	void config() {

		QuoteMgr.instance().initSymbols(getPreSubscriptionList());

		setSession(getPreOpen(), getOpen(), getClose());

		Path path1 = Paths.get("");

		this.path = path1.toAbsolutePath().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#uninit()
	 */
	@Override
	public void uninit() {
		isClose = true;
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
			SendState(connected);
		}
	}

	/**
	 * initClient
	 * 
	 * @param HOST
	 *            connect Host IP
	 * @param PORT
	 *            connect Host Port
	 * @throws Exception
	 */
	public static void initClient(final String HOST, final int PORT) throws Exception {

		Util.addLog(InfoString.ALert, "initClient enter %s:%d", HOST, PORT);
		LogUtil.logInfo(log, "initClient enter%s:%d", HOST, PORT);

		// Configure the client.
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap _clientBootstrap = new Bootstrap();
			_clientBootstrap.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new ClientHandler());
						}
					});

			// connect
			fClient = _clientBootstrap.connect(HOST, PORT);
			fClient.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture f) throws Exception {

					if (f.isSuccess()) {
						LogUtil.logInfo(log, 
								"client socket connected : %s:%d",
								HOST, PORT);
						Util.addLog("client socket connected : %s:%d",
								HOST, PORT);
					} else {
						Util.addLog(InfoString.ALert, "Connect to %s:%d fail.",
								HOST, PORT);
					}
				}
			});

		} catch (Exception e) {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
			LogUtil.logException(log, e);
			Util.addLog(InfoString.Error, "Connect to %s:%d fail.[%s]", HOST,
					PORT, e.getMessage());
		}

	}

	/**
	 * 
	 */
	public void closeClient() {
		if (fClient != null) {
			fClient.channel().close();
			fClient = null;
		}
		Parser.instance().clearRingbuffer();
		LogUtil.logInfo(log, "initClient exit");
		Util.addLog(InfoString.ALert, "initClient exit");
	}

	/**
	 * 
	 */
	public void reconClient() {

		if (isClose)
			return;

		try {
			Thread.sleep(1000);
			LogUtil.logInfo(log, "reconnect %s:%d", getReqIp(), getReqPort());
			CustomThreadPool.asyncMethod(methodClient, getReqIp(), getReqPort());
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
			
			ClientHandler.subscribe(exch, instrument);
			QuoteMgr.instance().addSymbol(instrument);
		}

		boolean bFound = false;
		for (UserClient client : clientsList.toArray(new UserClient[] {}))
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
			ClientHandler.unSubscribe(exch, instrument);
			QuoteMgr.instance().addSymbol(instrument);
		}

		boolean bFound = false;
		for (UserClient client : clientsList.toArray(new UserClient[] {}))
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
	 * @param on
	 */
	public void SendState(boolean on) {
		for (IMarketDataStateListener listener : stateList) {
			listener.onState(on);
		}
	}

	/**
	 * Send Quote 
	 * @param quote
	 */
	public void sendQuote(Quote quote) {
		UserClient[] clients = new UserClient[clientsList.size()];
		clients = clientsList.toArray(clients);
		for (UserClient client : clients) {
			client.sendQuote(quote);
		}
	}
	
	public String[] getRefSymbol() {
		List<String> list = new ArrayList<String>();
		
		UserClient[] clients = new UserClient[clientsList.size()];
		clients = clientsList.toArray(clients);
		for (UserClient client : clients) {
			List<String> listClient = client.getList();
			list.addAll(listClient);
		}
		
		String[] arr = new String[list.size()];
		list.toArray(arr);
		return arr;
	}

	/**
	 * 
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
	 * 
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
	 * 
	 * @return
	 */
	public int getStatus() {
		return getStatus(new Date());
	}

	/**
	 * 
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
		if (refTable.containsKey(symbol)== false) {
			return false;
		}
		else {
			synchronized (m_lock) {
				Integer refCount = refTable.get(symbol);
				refCount--;				
				if (refCount <= 0) {
					refTable.remove(symbol);
					return true;
				}
				else { 
					refTable.put(symbol, refCount);
					return false;
				}
			}
		}
			
	}

	/**
	 * 
	 * @param strSymbolID
	 * @return
	 */
	public String getDataPath(String strSymbolID) {
		return String.format("%s\\ticks\\%s", path, strSymbolID);
	}
}
