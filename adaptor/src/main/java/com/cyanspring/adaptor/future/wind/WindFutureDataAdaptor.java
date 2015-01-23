package com.cyanspring.adaptor.future.wind;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.cyanspring.id.Library.Threading.CustomThreadPool;
import com.cyanspring.id.Library.Threading.Delegate;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;

public class WindFutureDataAdaptor implements IMarketDataAdaptor,
		IReqThreadCallback {
	String password = "";
	String userName = "";
	String reqIp = "";
	int reqPort = 0;
	boolean showGui = false;

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

	private static final Logger log = LoggerFactory
			.getLogger(WindFutureDataAdaptor.class);

	public static WindFutureDataAdaptor instance = null;
	static final Method methodFuture = Delegate.getMethod("initFuture",
			WindFutureDataAdaptor.class, new Class[] { String.class, int.class,
					String.class, String.class });

	// private final boolean outputToScreen = true;
	/*********************** configuration ***************************************/
	private final String openMarket = "CF"; //;SH;SZ"; // "CF;SH;SZ;SHF;DCE";
	private final int openData = 0;
	private final int openTime = 0;
	private final String subscription = ""; // 000001.SZ;000002.SZ";
	private final int openTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX; // DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	/*********************** configuration ***************************************/
	TDFClient client = new TDFClient();
	TDF_OPEN_SETTING setting = new TDF_OPEN_SETTING();
	boolean connected = false;
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
				setConnected(false);
				//addReqData(new Object[] {
				//		TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK, null });
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				if (!isClosed) {
					connect();
				}

				break;
			case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				info("网路连接结果：");
				TDF_CONNECT_RESULT connect = data.getConnectResult();
				info("connect %s", connect.getConnResult() != 0 ? "success"
						: "fail");
				// PrintHelper.printConnectResult(data.getConnectResult());
				break;
			}
			case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_LOGIN_RESULT login = data.getLoginResult();
				info("login %s", login.getLoginResult() != 0 ? "success"
						: "fail");

				// PrintHelper.printLoginResult(data.getLoginResult());
				break;
			}
			case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT: {
				info("收到代码表！");
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				String[] markets = data.getCodeTableResult().getMarket();
				for (String market : markets) {
					if (!market.isEmpty()) {
						addReqData(new Object[] { type, market });
						// List<SymbolInfo> list = updateCodeTable(market);
						// sendSymbolInfo(list);
					}
				}
				break;
			}
			case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_MARKET_CLOSE close = data.getMarketClose();
				info("%s, %d, %s", close.getMarket(), close.getTime(),
						convertGBString(close.getInfo()));
				// PrintHelper.printMarketClose(data.getMarketClose());
				break;
			}
			case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				TDF_QUOTATIONDATE_CHANGE change = data.getDateChange();

				addReqData(new Object[] {
						TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE, change });

				// info("%s, quotation change from %d to %d",
				// change.getMarket(),
				// change.getOldDate(), change.getNewDate());

				// printCodeTable(change.getMarket());

				// PrintHelper.printDateChange(data.getDateChange());
				break;
			}
			// 资料消息
			case TDF_MSG_ID.MSG_DATA_MARKET:
				// info("MSG_DATA_MARKET");
				for (int i = 0; i < msg.getAppHead().getItemCount(); i++) {
					TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
					TDF_MARKET_DATA market = data.getMarketData();
					addReqData(new Object[] { TDF_MSG_ID.MSG_DATA_MARKET,
							market });
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
					addReqData(new Object[] { TDF_MSG_ID.MSG_DATA_FUTURE,
							future });
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

	public static void initFuture(String ip, int port, String user,
			String password) {
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		adaptor.connect();
		adaptor.processMessage();
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		boolean isChanged = this.connected != connected;
		this.connected = connected;
		if (isChanged) {
			SendState(connected);
		}
	}

	int nId = 0;

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
				setConnected(false);
				error("Can't connect to %s:%d[%s]", reqIp, reqPort,
						getErrMsg(err));
				try {
					if (err == TDF_ERR.TDF_ERR_VERIFY_FAILURE) {
						disconnect();
						Thread.sleep(10 * 1000);
						client.delete();
						client = new TDFClient();

						CustomThreadPool.asyncMethod(methodFuture, reqIp,
								reqPort, userName, password);
						return;

					} else {
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			} else {
				setConnected(true);
				break;
			}
		}
	}

	@Override
	public void init() throws Exception {
		WindFutureDataAdaptor.instance = this;
		initReqThread();
		CustomThreadPool.asyncMethod(methodFuture, reqIp, reqPort, userName,
				password);

	}

	@Override
	public void uninit() {
		closeReqThread();

	}

	List<ISymbolDataListener> symbolList = new ArrayList<ISymbolDataListener>();
	List<IMarketDataStateListener> stateList = new ArrayList<IMarketDataStateListener>();

	List<UserClient> clientsList = new ArrayList<UserClient>();
	Hashtable<String, Integer> refTable = new Hashtable<String, Integer>();
	private Object m_lock = new Object();

	@Override
	public boolean getState() {
		return false;
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
			listener.onState(connected);
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

			// ClientHandler.subscribe(exch, instrument);
			// QuoteMgr.instance().addSymbol(instrument);
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
			// ClientHandler.unSubscribe(exch, instrument);
			// QuoteMgr.instance().addSymbol(instrument);
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
	 * 
	 * @param on
	 */
	public void SendState(boolean on) {
		for (IMarketDataStateListener listener : stateList) {
			listener.onState(on);
		}
	}

	/**
	 * Send Quote
	 * 
	 * @param quote
	 */
	public void sendQuote(Quote quote) {
		UserClient[] clients = new UserClient[clientsList.size()];
		clients = clientsList.toArray(clients);
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
			listener.onSymbol(list);
			// listener.onSymbol(FutureItem.);
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
		// fetch symbol
		List<SymbolInfo> list = updateCodeTable(market);
		sendSymbolInfo(list);

	}

	public static String printSymbolInfo(SymbolInfo info) {
		FixStringBuilder sb = new FixStringBuilder('=', '|');
		//Hashtable<SymbolField, Object> table = info.getData();

		SymbolField field = SymbolField.symbolId;
		sb.append(field.toString());
		sb.append(info.getCode());
		//sb.append(table.get(field).toString());
		field = SymbolField.market;		
		sb.append(field.toString());
		sb.append(info.getMarket());
		//sb.append(table.get(field).toString());
		field = SymbolField.cnName;
		sb.append(field.toString());
		sb.append(info.getCnName());
		//sb.append(table.get(field).toString());
		field = SymbolField.enName;
		sb.append(field.toString());
		sb.append(info.getEnName());
		//sb.append(table.get(field).toString());

		return sb.toString();

	}

	void process(int type, Object objMsg) {
		switch (type) {
		case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT: {

			List<SymbolInfo> list = updateCodeTable((String) objMsg);
			sendSymbolInfo(list);
		}
			break;
		/*
		 * case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK: { try {
		 * Thread.sleep(1000); } catch (InterruptedException e) { }
		 * 
		 * if (!isClosed) { connect(); } } break;
		 */
		case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {
			TDF_QUOTATIONDATE_CHANGE change = (TDF_QUOTATIONDATE_CHANGE) objMsg;
			info("%s, quotation change from %d to %d", change.getMarket(),
					change.getOldDate(), change.getNewDate());

			updateCodeTable(change.getMarket());
		}
			break;
		case TDF_MSG_ID.MSG_DATA_FUTURE: {
			TDF_FUTURE_DATA future = (TDF_FUTURE_DATA) objMsg;
			FutureItem.processFutureData(future);
		}
			break;
		case TDF_MSG_ID.MSG_DATA_MARKET: {
			TDF_MARKET_DATA market = (TDF_MARKET_DATA) objMsg;
			StockItem.processMarketData(market);
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void onStartEvent(RequestThread sender) {

	}

	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {

		Object[] arr = (Object[]) reqObj;
		if (arr == null || arr.length != 2) {
			return;
		}
		int type = (int) arr[0];
		process(type, arr[1]);
	}

	@Override
	public void onStopEvent(RequestThread sender) {

	}
}
