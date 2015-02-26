package com.cyanspring.id.gateway; 

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.CustomThreadPool;
import com.cyanspring.id.Library.Threading.Delegate;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.gateway.netty.ClientHandler;
import com.cyanspring.id.gateway.netty.HttpServerInitializer;
import com.cyanspring.id.gateway.netty.ServerHandler;

public class IdGateway implements IFrameClose, IReqThreadCallback {
	private static final Logger log = LoggerFactory
			.getLogger(IdGateway.class);
	
	static final int KB = 1024;
	static final int MB = 1024 * KB;
	static final double GB = (double)KB * MB;
	static final double MaxSize = 10 * GB;
	
	public static IdGateway instance = new IdGateway();
	public static String version = "1.00R01";
	public static String lastUpdated = "2014-12-26";
	public long inNo = 0, outNo = 0;
	public double inSize = 0, outSize = 0;
	static RequestThread thread = null;
	
	boolean showGui = false;
	boolean gateway = false;

	int open;
	int preOpen;
	int close;
	
	String account = "";
	String password = "";
	
	String reqIp = "";
	int reqPort = 0;

	
	int exch = 687;
	
	int hostPort = 0;
	int httpPort = 0;
	
	private List<String> preSubscriptionList;
	
	private Map<String, Integer> nonFX;
	
	public Map<String, Integer> getNonFX() {
		return nonFX;
	}


	public void setNonFX(Map<String, Integer> nonFX) {
		this.nonFX = nonFX;
	}


	public boolean isShowGui() {
		return showGui;
	}


	public void setShowGui(boolean showGui) {
		this.showGui = showGui;
	}


	public boolean isGateway() {
		return gateway;
	}


	public void setGateway(boolean gateway) {
		this.gateway = gateway;
	}


	public int getOpen() {
		return open;
	}


	public void setOpen(int open) {
		this.open = open;
	}


	public int getPreOpen() {
		return preOpen;
	}


	public void setPreOpen(int preopen) {
		this.preOpen = preopen;
	}


	public int getClose() {
		return close;
	}


	public void setClose(int close) {
		this.close = close;
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


	public int getExch() {
		return exch;
	}


	public void setExch(int exch) {
		this.exch = exch;
	}


	public int getHostPort() {
		return hostPort;
	}


	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}


	public int getHttpPort() {
		return httpPort;
	}


	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}


	public List<String> getPreSubscriptionList() {
		return preSubscriptionList;
	}


	public void setPreSubscriptionList(List<String> preSubscriptionList) {
		this.preSubscriptionList = preSubscriptionList;
	}

	
	public void addSize(int nID, long lSize) {
		double dValue = 0;
		switch (nID) {
		case IDGateWayDialog.TXT_InSize: {
			inSize += lSize;
			if (inSize > MaxSize) {
				inSize %= MaxSize;
				inNo ++;
			}
			dValue = inSize;
		}
			break;
		case IDGateWayDialog.TXT_OutSize: {
			outSize += lSize;
			if (outSize > MaxSize) {
				outSize %= MaxSize;
				outNo ++;
			}
			dValue = outSize;
		}
		break;
		default:
			break;
		}
	
		if (mainFrame != null) {
			mainFrame.setSize(nID, dValue);
		}
		
	}
	

	public void updateClient(ArrayList<String> list) {
		if (mainFrame != null) {
			mainFrame.updateClient(list);
		}
	}
	
	public void addLog(int nLevel, String f, Object... args) {
		if (mainFrame != null) {
			mainFrame.addLog(nLevel, f, args);
		}
	}
	
	public void addLog(String f, Object... args) {
		if (mainFrame != null) {
			mainFrame.addLog(InfoString.Info, f, args);
		}
	}
	
	public static IdGateway instance() {
		return instance;
	}

	static final Method methodServer = Delegate.getMethod("initServer", IdGateway.class, new Class[] { int.class });

	static final Method methodHttpServer = Delegate.getMethod("initHttpServer", IdGateway.class, new Class[] { int.class });

	public IDGateWayDialog mainFrame = null;
	boolean isClose = false;
	//static ChannelFuture fClient = null;
	static ChannelFuture fServer = null;
	static ChannelFuture fHttpServer = null;
	
	public static boolean isSSL = false;

	private void init() {
		
		if (thread == null) {
			thread = new RequestThread(this, "initClient");
		}
		thread.start();
		setSession(); 
		QuoteMgr.Instance().addSymbols(getPreSubscriptionList());
		List<String> list = new ArrayList<String>(nonFX.keySet());
		//list.addAll(nonFX.keySet());
		
		QuoteMgr.Instance().addSymbols(list);

	}
	
	public void addRefreshButton() {
		if (mainFrame == null)
			return;
		
		JButton button = new JButton("ReConnect");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				closeClient();
				LogUtil.logInfo(log, "reconnect ....");
				addLog(InfoString.Info, "reconnect ....");
			}
		});
		mainFrame.addButton(button);
		JButton buttonSymbol = new JButton("dump symbols");
		buttonSymbol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				QuoteMgr.Instance().dumpSymbols();
				LogUtil.logInfo(log, "dump symbols");
				addLog(InfoString.Info, "dump symbols");
			}
		});
		mainFrame.addButton(buttonSymbol);	
	}

	public int getExch(String symbol){
		if (nonFX.containsKey(symbol)) {
			return nonFX.get(symbol);
		}
		return exch;
			
	}
	
	public void onCloseAction() {

		if (thread != null) {			
			thread.close();
			thread = null;
		}
		
		closeHttpServer();
		
		closeServer();

		closeClient();

		isClose = true;
	}

	
	/**
	 * 
	 * @param PORT
	 * @param SSL
	 * @throws Exception
	 */
	public static void initHttpServer(final int PORT) throws Exception {
		// Configure the server.
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap _serverBootstrap = new ServerBootstrap();
			_serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new HttpServerInitializer());

			// Start the server.
			fHttpServer = _serverBootstrap.bind(PORT);
			fHttpServer.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					LogUtil.logInfo(log, "HttpServer listen : %d", PORT); 
					IdGateway.instance().addLog("HttpServer listen : %d", PORT);
				}
			});

		} catch (Exception e) {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			IdGateway.instance().mainFrame.addLog(InfoString.Error, "HttpServer listen : %d fail! [%s]", PORT,
					e.getMessage());
			LogUtil.logException(log, e);
		}
	}
		
	public void closeHttpServer() {
		if (fHttpServer != null) {
			fHttpServer.channel().close();
			fHttpServer = null;
		}

		LogUtil.logInfo(log, "initHttpServer exit");
		IdGateway.instance().addLog(InfoString.ALert, "initHttpServer exit");
	}	
	
	/**
	 * 
	 * @param PORT
	 * @param SSL
	 * @throws Exception
	 */
	public static void initServer(final int PORT) throws Exception {
		// Configure the server.
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap _serverBootstrap = new ServerBootstrap();
			_serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new ServerHandler());
						}
					});

			// Start the server.
			fServer = _serverBootstrap.bind(PORT);
			fServer.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					LogUtil.logInfo(log, "Server listen : %d", PORT);
					IdGateway.instance().addLog("Server listen : %d", PORT);
				}
			});

		} catch (Exception e) {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			IdGateway.instance().addLog(InfoString.Error, "Server listen : %d fail! [%s]", PORT,
					e.getMessage());
			LogUtil.logException(log, e);
		}
	}

	public void onConnected() {
		ClientHandler.lastCheck = DateUtil.now();
	}
	
	void connect() {
		if (isConnected == true)
			return;
		
		thread.addRequest(new Object());
	}
	
	public static boolean isConnected = false;  	
	static NioEventLoopGroup clientGroup = null;
	public static boolean isConnecting = false; 
	/**
	 * 
	 * @param HOST
	 * @param PORT
	 * @param SSL
	 * @throws Exception
	 */
	public static void onInitClient(final String HOST, final int PORT) throws Exception {		
		IdGateway.instance().closeClient();
		IdGateway.instance().addLog(InfoString.ALert, "initClient enter");
		LogUtil.logInfo(log, "initClient enter");
		// Configure the client.
		clientGroup = new NioEventLoopGroup();
		try {
			Bootstrap _clientBootstrap = new Bootstrap();
			_clientBootstrap.group(clientGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							 
							 p.addLast("idleStateHandler", new IdleStateHandler(20, 0, 0));
							 //p.addLast("idleHandler", new IdleHandler());		
							 p.addLast(new ClientHandler());
						}
					});

			isConnected = false;
			// Start the client.
			ChannelFuture fClient = _clientBootstrap.connect(HOST, PORT).sync();
			if (fClient.isSuccess()) {
				IdGateway.instance().onConnected();
				LogUtil.logInfo(log, "client socket connected : %s:%d", HOST, PORT);
				IdGateway.instance().addLog("%s:%d connected", HOST, PORT);
				isConnecting = false;
				isConnected = true;
			} else {
				LogUtil.logInfo(log, "Connect to %s:%d fail.", HOST, PORT);
				instance.addLog(InfoString.ALert, "Connect to %s:%d fail.", HOST, PORT);
				isConnecting = true;
				io.netty.util.concurrent.Future<?> f = clientGroup.shutdownGracefully();
				f.await();  			    
				clientGroup = null;
			
				
				fClient.channel().eventLoop().schedule(new Runnable() {							
					@Override
					public void run() {
						try {							
							IdGateway.instance().connect();
						} catch (Exception e) {									
							LogUtil.logException(log, e);
						}
					}
				}, 10, TimeUnit.SECONDS);				
			}			
		} catch (Exception e) {
			isConnecting = false;
			// Shut down the event loop to terminate all threads.
			IdGateway.instance().closeClient();
			instance.addLog(InfoString.Error, "Connect to %s:%d fail.[%s]", HOST, PORT, e.getMessage());
			LogUtil.logException(log, e);
		}
	}

	public void closeServer() {
		if (fServer != null) {
			fServer.channel().close();
			fServer = null;
		}

		LogUtil.logInfo(log, "initServer exit");
		IdGateway.instance().addLog(InfoString.ALert, "initServer exit");
	}

	public void closeClient() {
		
		if (clientGroup != null) {
			io.netty.util.concurrent.Future<?> f  = clientGroup.shutdownGracefully();
			try {
				f.await();
			} catch (InterruptedException e) {

			}
		}
		
		Parser.Instance().clearRingbuffer();
		LogUtil.logInfo(log, "initClient exit");
		IdGateway.instance().addLog(InfoString.ALert, "initClient exit");
	}

	public void reconClient() {		
		if (isClose || isConnecting)
			return;
	
		try {
			Thread.sleep(1000);
			//CustomThreadPool.asyncMethod(methodClient, getReqIp(), getReqPort());
			connect();
		} catch (Exception e) {
			LogUtil.logError(log, e.getMessage());
			LogUtil.logException(log, e);
		}
	}
	
	int m_nPreOpen;
	int m_nOpen;
	int m_nClose;
	boolean isOverNight;
	
	void setSession() {
		m_nPreOpen = DateUtil.HHMMSS2Time(preOpen * 100);
		m_nOpen = DateUtil.HHMMSS2Time(open * 100);
		m_nClose = DateUtil.HHMMSS2Time(close * 100);
		isOverNight = m_nClose < m_nPreOpen;
	}

	public boolean isValidTime(Date time) {
		int nTime = DateUtil.dateTime2Time(time);

		if (isOverNight) {
			return nTime >= m_nPreOpen || nTime <= m_nClose;
		} else {
			return nTime >= m_nPreOpen && nTime <= m_nClose;
		}
	}

	public int getStatus() {
		return getStatus(new Date());
	}

	public int getStatus(Date time) {
		int nDow = DateUtil.getDayofWeek(time);
		int nTime = DateUtil.dateTime2Time(time);
		if (Calendar.SUNDAY == nDow) // Sunday
		{
			return MarketStatus.CLOSE;
		}

		if (isOverNight) {
			if (nTime >= m_nOpen || nTime <= m_nClose) {
				if (nTime <= m_nClose && Calendar.MONDAY == nDow) // Monday
				{
					return MarketStatus.CLOSE;
				} else if (nTime >= m_nOpen && Calendar.SATURDAY == nDow) // Saturday
				{
					return MarketStatus.CLOSE;
				} else {
					return MarketStatus.OPEN;
				}
			} else if (nTime >= m_nPreOpen && nTime < m_nOpen) {
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

			if (nTime >= m_nOpen && nTime <= m_nClose) {
				return MarketStatus.OPEN;
			} else if (nTime >= m_nPreOpen || nTime < m_nOpen) {
				return MarketStatus.PREOPEN;
			} else {
				return MarketStatus.CLOSE;
			}
		}
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		DOMConfigurator.configure("conf/gwlog4j.xml");
		String configFile = "conf/idgateway.xml";
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		IdGateway bean = (IdGateway) context
				.getBean("IdGateway");
		

		bean.init();

		IdGateway.instance = bean;
		
		// GUI Mode
		if (bean.isShowGui()){
			instance.mainFrame = IDGateWayDialog.Instance(instance, String.format("%s:%s", "IDGateWay", version));
			instance.addRefreshButton();
		}

		CustomThreadPool.asyncMethod(methodServer, bean.getHostPort());
		IdGateway.instance().connect();
		CustomThreadPool.asyncMethod(methodHttpServer, bean.getHttpPort());
	}


	@Override
	public void onStartEvent(RequestThread sender) {
		
	}


	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {
		reqObj = null;
		try {
			thread.removeAllRequest();
			if (IdGateway.isConnected)
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
