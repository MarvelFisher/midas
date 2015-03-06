package com.cyanspring.adaptor.future.wind;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.com.wind.td.tdf.DATA_TYPE_FLAG;
import cn.com.wind.td.tdf.TDFClient;
import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_CONNECT_RESULT;
import cn.com.wind.td.tdf.TDF_ERR;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_LOGIN_RESULT;
import cn.com.wind.td.tdf.TDF_MARKET_CLOSE;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_MSG;
import cn.com.wind.td.tdf.TDF_MSG_DATA;
import cn.com.wind.td.tdf.TDF_MSG_ID;
import cn.com.wind.td.tdf.TDF_OPEN_SETTING;
import cn.com.wind.td.tdf.TDF_OPTION_CODE;
import cn.com.wind.td.tdf.TDF_QUOTATIONDATE_CHANGE;

import com.cyanspring.adaptor.future.wind.test.FutureFeed;
import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.ISymbolDataListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.SymbolField;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.id.UserClient;
import com.cyanspring.id.Util;
import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;

public class WindFutureDataAdaptor implements IMarketDataAdaptor,
		IReqThreadCallback {
	String password = "";
	String userName = "";
	String reqIp = "";
	String gatewayIp = "";
	int reqPort = 0;
	int gatewayPort = 0;
	boolean gateway = false;
	boolean showGui = false;
	IRefDataManager refDataManager;
	boolean marketDataLog = false; // log control
	String marketType = "";
	MarketSessionUtil marketSessionUtil;
	
	public MarketSessionUtil getMarketSessionUtil() {
		return marketSessionUtil;
	}

	public void setMarketSessionUtil(MarketSessionUtil marketSessionUtil) {
		this.marketSessionUtil = marketSessionUtil;
	}

	public String getMarketType() {
		return marketType;
	}

	public void setMarketType(String marketType) {
		this.marketType = marketType;
	}

	public IRefDataManager getRefDataManager() {
		return refDataManager;
	}

	public void setRefDataManager(IRefDataManager refDataManager) {
		this.refDataManager = refDataManager;
	}

	public boolean isShowGui() {
		return showGui;
	}

	public void setShowGui(boolean showGui) {
		this.showGui = showGui;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String account) {
		this.userName = account;
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

	public boolean isGateway() {
		return gateway;
	}

	public void setGateway(boolean gateway) {
		this.gateway = gateway;
	}

	public boolean isMarketDataLog() {
		return marketDataLog;
	}

	public void setMarketDataLog(boolean marketDataLog) {
		this.marketDataLog = marketDataLog;
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

	private static final Logger log = LoggerFactory
			.getLogger(WindFutureDataAdaptor.class);

	public static WindFutureDataAdaptor instance = null;

	// private final boolean outputToScreen = true;
	/*********************** configuration ***************************************/
	// private final String openMarket = "CZC;SHF;DCE";
	private final String openMarket = "";
	private final int openData = 0;
	private final int openTime = 0;
	private final String subscription = ""; // 000001.SZ;000002.SZ";
	private final int openTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX; // DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	private static final int doConnect = 0;
	static volatile boolean isConnected = false;
	static volatile boolean isConnecting = false;

	/*********************** configuration ***************************************/
	TDFClient client = new TDFClient();
	TDF_OPEN_SETTING setting = new TDF_OPEN_SETTING();
	static Hashtable<String, TDF_FUTURE_DATA> futuredata = new Hashtable<String, TDF_FUTURE_DATA>(); // future
	static Hashtable<String, TDF_MARKET_DATA> stockdata = new Hashtable<String, TDF_MARKET_DATA>(); // stock
	static Hashtable<String, String> strategyht = new Hashtable<String, String>(); // save
																					// symbol
																					// strategy

	boolean isClosed = false;
	RequestThread thread = null;

	void initReqThread() {
		if (thread == null) {
			thread = new RequestThread(this, "Wind Index Future");
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

	public static void info(String f, Object... args) {
		LogUtil.logInfo(log, f, args);
		FutureFeed.info(f, args);
	}

	public static void error(String f, Object... args) {
		LogUtil.logError(log, f, args);
		FutureFeed.error(f, args);

	}

	public static void exception(Exception e) {
		LogUtil.logException(log, e);
		FutureFeed.error("%s", e.getMessage());
	}

	public static void debug(String f, Object... args) {
		LogUtil.logDebug(log, f, args);
		FutureFeed.debug(f, args);
	}

	public static String convertGBString(String string) {
		String str = null;
		try {
			str = new String(string.getBytes("MS950"), "GB2312"); // "MS936");

		} catch (UnsupportedEncodingException e1) {
		}
		return str;
	}

	static String getErrMsg(int err) {
		switch (err) {

		case TDF_ERR.TDF_ERR_UNKOWN:
			return String.format("(%d)未知错误", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_INITIALIZE_FAILURE:
			return String.format("(%d)初始化 socket 环境失败", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_NETWORK_ERROR:
			return String.format("(%d)网络连接出现问题", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_INVALID_PARAMS:
			return String.format("(%d)输入参数无效", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_VERIFY_FAILURE:
			return String.format("(%d)登陆验证失败：原因为用户名或者密码错误；超出登陆数量", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_NO_AUTHORIZED_MARKET:
			return String.format("(%d)所有请求的市场都没授权", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_NO_CODE_TABLE:
			return String.format("(%d)所有请求的市场该天都没有代码表", err);
			// Field descriptor #5 I
		case TDF_ERR.TDF_ERR_SUCCESS:
			return String.format("(%d)成功", err);
		default:
			return String.format("(%d)NOT DEFINED", err);
		}
	}

	public TDF_OPTION_CODE getOptionCodeInfo(String szWindCode) {
		return client.getOptionCodeInfo(szWindCode);
	}

	public List<SymbolInfo> updateCodeTable(String market) {
		TDF_CODE[] codes = client.getCodeTable(market);
		List<SymbolInfo> list = new ArrayList<SymbolInfo>();
		try {

			for (TDF_CODE code : codes) {
				SymbolInfo info = FutureItem.processCODE(code);
				String s = printSymbolInfo(info);
				LogUtil.logInfo(log, s);
				list.add(info);
			}

		} catch (Exception e) {
			LogUtil.logException(log, e);
		}
		return list;
	}

	void printCodeTable(String market) {
		TDF_CODE[] codes = client.getCodeTable(market);
		try {

			for (TDF_CODE code : codes) {
				FutureItem.processCODE(code);
			}
		} catch (Exception e) {
			LogUtil.logException(log, e);
		}
	}

	void disconnect() {
		isClosed = true;
		client.delete();
	}

	public void reconnect() {
		isClosed = false;
		client.close();
	}

	// Parse Change Data
	public TDF_QUOTATIONDATE_CHANGE convertToChangeData(String[] in_arr) {
		TDF_QUOTATIONDATE_CHANGE changedata = new TDF_QUOTATIONDATE_CHANGE();
		String key = null;
		String value = null;
		for (int i = 0; i < in_arr.length; i++) {
			key = in_arr[i].split("=")[0];
			value = in_arr[i].split("=")[1];
			switch (key) {
			case "Market":
				break;
			case "OldDate":
				changedata.setOldDate(Integer.parseInt(value));
				break;
			case "NewDate":
				changedata.setNewDate(Integer.parseInt(value));
				break;
			default:
				break;
			}
		}
		return changedata;
	}

	// Parse Index Data

	// Parse Stock Data
	public TDF_MARKET_DATA convertToStockData(String[] in_arr) {
		TDF_MARKET_DATA stock = null;
		String key = null;
		String value = null;
		String[] kv_arr = null;

		for (int i = 0; i < in_arr.length; i++) {
			if (in_arr[i] != null && !"".equals(in_arr[i])) {
				kv_arr = in_arr[i].split("=");
				if (kv_arr.length > 1) {
					key = kv_arr[0];
					value = kv_arr[1];
					if (key.equals("Symbol")) {
						if (futuredata.containsKey(value)) {
							stock = stockdata.get(value);
						} else {
							// add future data
							stock = new TDF_MARKET_DATA();
							stock.setWindCode(value);
							stock.setCode(value.split("\\.")[0]);
							stockdata.put(value, stock);
						}
					}
					switch (key) {
					case "ActionDay":
						stock.setActionDay(Integer.parseInt(value));
						break;
					case "AskPrice":
						stock.setAskPrice(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "AskVol":
						stock.setAskVol(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "BidPrice":
						stock.setBidPrice(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "BidVol":
						stock.setBidVol(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "High":
						stock.setHigh(Long.parseLong(value));
						break;
					case "Ceil":
						stock.setHighLimited(Long.parseLong(value));
						break;
					case "Low":
						stock.setLow(Long.parseLong(value));
						break;
					case "Floor":
						stock.setLowLimited(Long.parseLong(value));
						break;
					case "Last":
						stock.setMatch(Long.parseLong(value));
						break;
					case "Open":
						stock.setOpen(Long.parseLong(value));
						break;
					case "IOPV":
						stock.setIOPV(Integer.parseInt(value));
						break;
					case "PreClose":
						stock.setPreClose(Long.parseLong(value));
						break;
					case "Status":
						stock.setStatus(Integer.parseInt(value));
						break;
					case "Time":
						stock.setTime(Integer.parseInt(value));
						break;
					case "TradingDay":
						stock.setTradingDay(Integer.parseInt(value));
						break;
					case "Turnover":
						stock.setTurnover(Long.parseLong(value));
						break;
					case "Volume":
						stock.setVolume(Long.parseLong(value));
						break;
					case "NumTrades":
						stock.setNumTrades(Long.parseLong(value));
						break;
					case "TotalBidVol":
						stock.setTotalBidVol(Long.parseLong(value));
						break;
					case "WgtAvgAskPrice":
						stock.setWeightedAvgAskPrice(Long.parseLong(value));
						break;
					case "WgtAvgBidPrice":
						stock.setWeightedAvgBidPrice(Long.parseLong(value));
						break;
					case "YieldToMaturity":
						stock.setYieldToMaturity(Integer.parseInt(value));
						break;
					case "Prefix":
						stock.setPrefix(value);
						break;
					case "Syl1":
						stock.setSyl1(Integer.parseInt(value));
						break;
					case "Syl2":
						stock.setSyl2(Integer.parseInt(value));
						break;
					case "SD2":
						stock.setSD2(Integer.parseInt(value));
						break;
					default:
						break;
					}
				}
			}
		}
		return stock;
	}

	// Parse Future Data
	public TDF_FUTURE_DATA convertToFutureData(String[] in_arr) {
		TDF_FUTURE_DATA future = null;
		String key = null;
		String value = null;
		String[] kv_arr = null;

		for (int i = 0; i < in_arr.length; i++) {
			if (in_arr[i] != null && !"".equals(in_arr[i])) {
				kv_arr = in_arr[i].split("=");
				if (kv_arr.length > 1) {
					key = kv_arr[0];
					value = kv_arr[1];
					if (key.equals("Symbol")) {
						if (futuredata.containsKey(value)) {
							future = futuredata.get(value);
						} else {
							// add future data
							future = new TDF_FUTURE_DATA();
							future.setWindCode(value);
							future.setCode(value.split("\\.")[0]);
							futuredata.put(value, future);
						}
					}
					switch (key) {
					case "ActionDay":
						future.setActionDay(Integer.parseInt(value));
						break;
					case "AskPrice":
						future.setAskPrice(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "AskVol":
						future.setAskVol(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "BidPrice":
						future.setBidPrice(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "BidVol":
						future.setBidVol(parseStringTolong(value.substring(1,
								value.length() - 1).split("\\s")));
						break;
					case "Close":
						future.setClose(Long.parseLong(value));
						break;
					case "High":
						future.setHigh(Long.parseLong(value));
						break;
					case "Ceil":
						future.setHighLimited(Long.parseLong(value));
						break;
					case "Low":
						future.setLow(Long.parseLong(value));
						break;
					case "Floor":
						future.setLowLimited(Long.parseLong(value));
						break;
					case "Last":
						future.setMatch(Long.parseLong(value));
						break;
					case "Open":
						future.setOpen(Long.parseLong(value));
						break;
					case "OI":
						future.setOpenInterest(Long.parseLong(value));
						break;
					case "PreClose":
						future.setPreClose(Long.parseLong(value));
						break;
					case "SettlePrice":
						future.setSettlePrice(Long.parseLong(value));
						break;
					case "Status":
						future.setStatus(Integer.parseInt(value));
						break;
					case "Time":
						future.setTime(Integer.parseInt(value));
						break;
					case "TradingDay":
						future.setTradingDay(Integer.parseInt(value));
						break;
					case "Turnover":
						future.setTurnover(Long.parseLong(value));
						break;
					case "Volume":
						future.setVolume(Long.parseLong(value));
						break;
					default:
						break;
					}
				}
			}
		}
		return future;
	}

	public static long[] parseStringTolong(String[] arr) {
		long[] long_arr = new long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			long_arr[i] = Long.parseLong(arr[i]);
		}
		return long_arr;
	}

	public void processGateWayMessage(int datatype, String[] in_arr) {

		if (in_arr == null)
			return;

		switch (datatype) {
		// 系统消息
		case TDF_MSG_ID.MSG_SYS_HEART_BEAT:
			// debug("MSG_SYS_HEART_BEAT");
			break;
		case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK:
			break;
		case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT:
			break;
		case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT:
			break;
		case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT:
			debug("MSG_SYS_CODETABLE_RESULT");
			break;
		case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE:
			debug("MSG_SYS_MARKET_CLOSE");
			break;
		case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE:
			debug("MSG_SYS_QUOTATIONDATE_CHANGE");
			break;
		// 资料消息
		case TDF_MSG_ID.MSG_DATA_MARKET:
			TDF_MARKET_DATA stock = convertToStockData(in_arr);
			QuoteMgr.instance.AddRequest(new Object[] {
					TDF_MSG_ID.MSG_DATA_MARKET, stock });
			break;
		case TDF_MSG_ID.MSG_DATA_INDEX:
			debug("MSG_DATA_INDEX");
			break;
		case TDF_MSG_ID.MSG_DATA_FUTURE:
			TDF_FUTURE_DATA future = convertToFutureData(in_arr);
			QuoteMgr.instance.AddRequest(new Object[] {
					TDF_MSG_ID.MSG_DATA_FUTURE, future });
			break;
		case TDF_MSG_ID.MSG_DATA_TRANSACTION:
			// Transaction
			break;
		case TDF_MSG_ID.MSG_DATA_ORDERQUEUE:
			// Order Queue
			break;
		case TDF_MSG_ID.MSG_DATA_ORDER:
			// Order
			break;
		default:
			break;
		}
	}

	void processMessage() {
		while (this.isConnected()) {
			TDF_MSG msg = client.getMessage(10);
			if (msg == null)
				continue;

			int type = msg.getDataType();
			switch (type) {
			// 系统消息
			case TDF_MSG_ID.MSG_SYS_HEART_BEAT:
				info("收到心跳资讯！");
				break;
			case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK:
				error("网路断开！");
				isConnected = false;
				updateState(false);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				if (!isClosed) {
					doConnect();
				}

				break;
			case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				info("网路连接结果：");
				TDF_CONNECT_RESULT connect = data.getConnectResult();
				info("connect %s", connect.getConnResult() != 0 ? "success"
						: "fail");
				break;
			}
			case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_LOGIN_RESULT login = data.getLoginResult();
				info("login %s", login.getLoginResult() != 0 ? "success"
						: "fail");
				break;
			}
			case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT: {
				info("收到代码表！");
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				String[] markets = data.getCodeTableResult().getMarket();
				for (String market : markets) {
					if (!market.isEmpty()) {
						QuoteMgr.instance.AddRequest(new Object[] { type,
								market });
					}
				}
				break;
			}
			case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_MARKET_CLOSE close = data.getMarketClose();
				info("%s, %d, %s", close.getMarket(), close.getTime(),
						convertGBString(close.getInfo()));
				break;
			}
			case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_QUOTATIONDATE_CHANGE change = data.getDateChange();

				QuoteMgr.instance.AddRequest(new Object[] {
						TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE, change });
				break;
			}
			// 资料消息
			case TDF_MSG_ID.MSG_DATA_MARKET:
				// info("MSG_DATA_MARKET");
				for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
					TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
					TDF_MARKET_DATA market = data.getMarketData();
					QuoteMgr.instance.AddRequest(new Object[] {
							TDF_MSG_ID.MSG_DATA_MARKET, market });
					// StockItem.processMarketData(market);
				}
				break;
			case TDF_MSG_ID.MSG_DATA_INDEX:
				// info("MSG_DATA_INDEX");
				/*
				 * for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
				 * TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
				 * TDF_INDEX_DATA index = data.getIndexData();
				 * //FutureItem.processFutureData(data.getFutureData()); }
				 */
				break;
			case TDF_MSG_ID.MSG_DATA_FUTURE:
				for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
					TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
					TDF_FUTURE_DATA future = data.getFutureData();
					QuoteMgr.instance.AddRequest(new Object[] {
							TDF_MSG_ID.MSG_DATA_FUTURE, future });
					// FutureItem.processFutureData(future);
				}
				break;
			case TDF_MSG_ID.MSG_DATA_TRANSACTION:
				info("MSG_DATA_TRANSACTION");
				break;
			case TDF_MSG_ID.MSG_DATA_ORDERQUEUE:
				// Order Queue
				break;
			case TDF_MSG_ID.MSG_DATA_ORDER:
				// Order
				break;
			default:
				break;
			}
		}
		client.close();
	}

	boolean isClose = false;
	static NioEventLoopGroup clientGroup = null;

	/**
	 * Wind Client link Gateway Server
	 * 
	 * @param ip
	 * @param port
	 */
	public void initFuture(String ip, int port) {

		isConnecting = true;
		WindFutureDataAdaptor.instance.closeClient();
		Util.addLog(InfoString.ALert, "Wind initClient enter %s:%d", ip, port);
		LogUtil.logInfo(log, "Wind initClient enter %s:%d", ip, port);

		// Configure the client.
		clientGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap().group(clientGroup)
					.channel(NioSocketChannel.class)
					.handler(new ClientInitializer());

			ChannelFuture fClient = bootstrap.connect(ip, port).sync();
			// Channel channel = bootstrap.connect(ip, port).sync().channel();

			if (fClient.isSuccess()) {
				WindFutureDataAdaptor.instance.onConnected();
				LogUtil.logInfo(log, "client socket connected : %s:%d", ip,
						port);
				Util.addLog("client socket connected : %s:%d", ip, port);

				isConnecting = false;
				isConnected = true;
			} else {
				LogUtil.logInfo(log, "Connect to %s:%d fail.", ip, port);
				Util.addLog(InfoString.ALert, "Connect to %s:%d fail.", ip,
						port);
				isConnecting = true;
				io.netty.util.concurrent.Future<?> f = clientGroup
						.shutdownGracefully();
				f.await();
				clientGroup = null;

				fClient.channel().eventLoop().schedule(new Runnable() {
					@Override
					public void run() {
						try {
							LogUtil.logDebug(log, "Channel EventLoop Schedule!");
							WindFutureDataAdaptor.instance.doConnect();
						} catch (Exception e) {
							LogUtil.logException(log, e);
						}
					}
				}, 10, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			isConnecting = false;
			// Shut down the event loop to terminate all threads.
			WindFutureDataAdaptor.instance.closeClient();
			LogUtil.logException(log, e);
			Util.addLog(InfoString.Error, "Connect to %s:%d fail.[%s]", ip,
					port, e.getMessage());
		}
	}

	public void updateState(boolean connected) {
		sendState(connected);
	}

	/**
	 * Send connection State
	 * 
	 * @param on
	 */
	public void sendState(boolean on) {
		for (IMarketDataStateListener listener : stateList) {
			LogUtil.logDebug(log,
					"IMarketDataStateListener = " + listener.getClass());
			listener.onState(on);
		}
	}

	public void onConnected() {
		ClientHandler.lastCheck = DateUtil.now();
	}

	public void closeClient() {
		if (clientGroup != null) {
			io.netty.util.concurrent.Future<?> f = clientGroup
					.shutdownGracefully();
			try {
				f.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clientGroup = null;
		}

		LogUtil.logInfo(log, "Wind initClient exit");
		Util.addLog(InfoString.ALert, "Wind initClient exit");
	}

	// API initial
	public static void initFuture(String ip, int port, String user,
			String password) {
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		adaptor.connect();
		adaptor.processMessage();
	}

	public boolean isConnected() {
		return isConnected;
	}

	int nId = 0;

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

	public void connect() {

		while (true) {
			setting.setIp(reqIp);
			setting.setPort(Integer.toString(reqPort));
			setting.setUser(userName);
			setting.setPwd(password);

			setting.setReconnectCount(99999999);
			setting.setReconnectGap(10);
			setting.setProtocol(0);
			setting.setMarkets(openMarket);
			setting.setDate(openData);
			setting.setTime(openTime);
			setting.setSubScriptions(subscription);
			setting.setTypeFlags(openTypeFlags);
			setting.setConnectionID(nId);

			nId = (nId + 1) % 2;
			info("connect to %s:%d", reqIp, reqPort);
			int err = client.open(setting);
			if (err != TDF_ERR.TDF_ERR_SUCCESS) {

				isConnecting = true;
				error("Can't connect to %s:%d[%s]", reqIp, reqPort,
						getErrMsg(err));
				try {
					if (err == TDF_ERR.TDF_ERR_VERIFY_FAILURE) {
						disconnect();
						Thread.sleep(10 * 1000);
						client.delete();
						client = new TDFClient();

						addReqData(doConnect);
						return;

					} else {
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			} else {
				isConnecting = false;
				isConnected = true;
				updateState(true);
				break;
			}
		}
	}

	@Override
	public void init() throws Exception {
		WindFutureDataAdaptor.instance = this;
		WindFutureDataAdaptor.instance.getRefDataManager().init(); // init
																	// RefDataManager
		QuoteMgr.instance.init();
		initReqThread();
		doConnect();
	}

	@Override
	public void uninit() {
		isClose = true;
		QuoteMgr.instance.uninit();
		closeReqThread();

		LogUtil.logInfo(log, "WindFutureDataAdaptor exit");
		closeClient();
		isClose = true;
	}

	List<ISymbolDataListener> symbolList = new ArrayList<ISymbolDataListener>();
	List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();

	List<UserClient> clientsList = new ArrayList<UserClient>();
	Hashtable<String, Integer> refTable = new Hashtable<String, Integer>();
	private Object m_lock = new Object();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cyanspring.common.marketdata.IMarketDataAdaptor#getState()
	 */
	@Override
	public boolean getState() {
		return isConnected;
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
			if (WindFutureDataAdaptor.instance.isGateway()) {
				// Future
				if ("F".equals(WindFutureDataAdaptor.instance.getMarketType())) {
					log.info("subscribeMarketData RefSymbol: " + instrument);
					log.debug("Setting refDataManager: "
							+ refDataManager.getClass());
					RefData refData = refDataManager.getRefData(instrument);
					if (refData == null) {
						LogUtil.logError(log, "RefSymbol " + instrument
								+ " is not found in reference data");
						throw new MarketDataException("Ref Symbol:"
								+ instrument
								+ " is not found in reference data");
					} else {
						LogUtil.logDebug(log, "RefSymbol " + instrument
								+ " Exchange=" + refData.getExchange()
								+ ",Symbol=" + refData.getSymbol()
								+ ",Strategy=" + refData.getStrategy());
						instrument = refData.getSymbol();
						strategyht.put(instrument, refData.getStrategy());
						// subscribe
						ClientHandler.subscribe(refData.getSymbol());
						QuoteMgr.instance().addFutureSymbol(
								refData.getSymbol(), refData.getExchange()); // future
					}
				}
				// Stock
				if ("S".equals(WindFutureDataAdaptor.instance.getMarketType())) {
					ClientHandler.subscribe(instrument);
					QuoteMgr.instance().addStockSymbol(instrument, null);
				}
			}
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
			// if(WindFutureDataAdaptor.instance.gateway){
			// ClientHandler.unSubscribe(instrument);
			// if(targetType==1){
			// QuoteMgr.instance().addFutureSymbol(instrument); //future
			// }
			// if(targetType==2){
			// QuoteMgr.instance().addStockSymbol(instrument); //stock
			// }
			// }
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
	 * Send Quote
	 * 
	 * @param quote
	 */
	public void sendQuote(Quote quote) {
		// UserClient[] clients = new UserClient[clientsList.size()];
		// clients = clientsList.toArray(clients);
		List<UserClient> clients = new ArrayList<UserClient>(clientsList);
		for (UserClient client : clients) {
			client.sendQuote(quote);
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

	public void sendSymbolInfo(List<SymbolInfo> list) {
		List<ISymbolDataListener> listeners = new ArrayList<ISymbolDataListener>(
				symbolList);
		for (ISymbolDataListener listener : listeners) {
			listener.onSymbol(list);
		}
	}

	@Override
	public void subscirbeSymbolData(ISymbolDataListener listener) {
		if (!symbolList.contains(listener)) {
			// do Action
			List<SymbolInfo> list = FutureItem.getSymbolInfoList();
			List<SymbolInfo> stock_list = StockItem.getSymbolInfoList();
			list.addAll(stock_list);
			listener.onSymbol(list);
			symbolList.add(listener);
		}
	}

	@Override
	public void unsubscribeSymbolData(ISymbolDataListener listener) {
		if (symbolList.contains(listener)) {
			symbolList.remove(listener);
		}
	}

	@Override
	public void refreshSymbolInfo(String market) {
		if (!isGateway()) {
			// fetch symbol
			List<SymbolInfo> list = updateCodeTable(market);
			sendSymbolInfo(list);
		}
	}

	public void clearSubscribeMarketData() {
		ClientHandler.sendClearSubscribe();
	}

	public static String printSymbolInfo(SymbolInfo info) {
		FixStringBuilder sb = new FixStringBuilder('=', '|');
		// Hashtable<SymbolField, Object> table = info.getData();

		SymbolField field = SymbolField.symbolId;
		sb.append(field.toString());
		sb.append(info.getCode());
		// sb.append(table.get(field).toString());
		field = SymbolField.market;
		sb.append(field.toString());
		sb.append(info.getMarket());
		// sb.append(table.get(field).toString());
		field = SymbolField.cnName;
		sb.append(field.toString());
		sb.append(info.getCnName());
		// sb.append(table.get(field).toString());
		field = SymbolField.enName;
		sb.append(field.toString());
		sb.append(info.getEnName());
		// sb.append(table.get(field).toString());

		return sb.toString();

	}

	@Override
	public void onStartEvent(RequestThread sender) {

	}

	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {

		int type = (int) reqObj;
		if (type == doConnect) {
			if (isConnected == true)
				return;
			if (gateway) {
				LogUtil.logInfo(log, "connect to Wind GW %s:%d",
						getGatewayIp(), getGatewayPort());
				initFuture(gatewayIp, gatewayPort);
			} else {
				initFuture(reqIp, reqPort, userName, password);
			}
		}
		reqObj = null;
	}

	@Override
	public void onStopEvent(RequestThread sender) {

	}
}
