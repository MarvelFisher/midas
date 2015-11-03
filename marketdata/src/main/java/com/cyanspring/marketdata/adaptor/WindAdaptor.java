package com.cyanspring.marketdata.adaptor;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.marketdata.data.*;
import com.cyanspring.marketdata.item.FutureItem;
import com.cyanspring.marketdata.item.IndexItem;
import com.cyanspring.marketdata.item.StockItem;
import com.cyanspring.marketdata.item.TransationItem;
import com.cyanspring.marketdata.type.FDTFields;
import com.cyanspring.marketdata.type.WindDef;
import com.cyanspring.marketdata.util.FDTFrameDecoder;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WindAdaptor implements IMarketDataAdaptor, IWindGateWayListener {
	private static final Logger log = LoggerFactory.getLogger(WindAdaptor.class);
	private String gatewayIp = "";
	private int gatewayPort = 0;
	private boolean marketDataLog = false; // log control
	private boolean isSubTrans = false;
	private String id = "W";
	private boolean isAlive = false;
	EventLoopGroup eventLoopGroup = null;
	private QuoteMgr quoteMgr = new QuoteMgr(this);
	private ChannelHandlerContext channelHandlerContext;
	private volatile boolean isConnected = false;
	IMarketDataStateListener marketDataStateListener = null;
	IMarketDataListener marketDataListener = null;

	// Calculate packet use
	private int bufLenMin = 0, bufLenMax = 0, blockCount = 0;
	private long msDiff = 0, msLastTime = 0, throughput = 0;

	public void connect() {
		log.info(String.format(id + " connect to WindGW %s:%d", gatewayIp, gatewayPort));
		log.debug(id + " Run Netty WindGW Adapter");
		eventLoopGroup = new NioEventLoopGroup(2);
		ChannelFuture f;
		Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).handler(new ClientInitializer(this));

		try {
			while (isAlive) {
				try {
					f = bootstrap.connect(gatewayIp, gatewayPort);
					f.awaitUninterruptibly();
					if (f.isCancelled()) {
						log.info(id + " Connection attempt cancelled by user");
					} else if (!f.isSuccess()) {
						log.warn(f.cause().getMessage());
					} else {
						f.channel().closeFuture().sync();
					}
					if (!isAlive)
						return;
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
				log.info(id + " WindGW Adapter disconnect with - " + gatewayIp + " : " + gatewayPort + " , will try again after 3 seconds.");
				Thread.sleep(3000);
			}
		} catch (InterruptedException ie) {
			log.warn(ie.getMessage(), ie);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (eventLoopGroup != null)
				eventLoopGroup.shutdownGracefully();
		}
	}

	public void processGateWayMessage(String[] in_arr, HashMap<Integer, Object> inputMessageHashMap) {
		if (inputMessageHashMap.get(FDTFields.WindSymbolCode) == null)
			return;
		if (inputMessageHashMap == null || inputMessageHashMap.size() == 0)
			return;
		quoteMgr.addRequest(inputMessageHashMap.clone());
	}

	public void updateState(boolean connected) {
		if (isAlive)
			sendState(connected);
	}

	public void sendState(boolean on) {
		if (marketDataStateListener != null)
			marketDataStateListener.onState(on, this);
	}

	public void close() {
		log.info(id + " Wind close client begin");
		if (eventLoopGroup != null) {
			io.netty.util.concurrent.Future<?> f = eventLoopGroup.shutdownGracefully();
			try {
				f.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			eventLoopGroup = null;
		}
		log.info(id + " Wind close client end");
	}

	public void subscribe(String symbol) {
		StringBuffer sb = new StringBuffer("");
		sb.append("API=SUBSCRIBE").append("|Symbol=").append(symbol);
		String subscribeStr = sb.toString();
		subscribeStr = subscribeStr + "|Hash=" + String.valueOf(subscribeStr.hashCode());
		log.info("[Subscribe]%s", subscribeStr);
		sendData(subscribeStr);
		if (isSubTrans) {
			sb = new StringBuffer("");
			sb.append("API=SubsTrans").append("|Symbol=").append(symbol);
			subscribeStr = sb.toString();
			subscribeStr = subscribeStr + "|Hash=" + String.valueOf(subscribeStr.hashCode());
			log.info("[Subscribe]%s", subscribeStr);
			sendData(subscribeStr);
		}
	}

	public void sendClearSubscribe() {
		StringBuffer sb = new StringBuffer("");
		sb.append("API=ClearSubscribe");
		String subscribeStr = sb.toString();
		subscribeStr = subscribeStr + "|Hash=" + String.valueOf(subscribeStr.hashCode());
		log.info("[sendClearSubscribe]%s", subscribeStr);
		sendData(subscribeStr);
	}

	public void sendReqHeartbeat() {
		StringBuffer sb = new StringBuffer("");
		sb.append("API=ReqHeartBeat");
		int fsbhashCode = sb.toString().hashCode();
		sb.append("|Hash").append(String.valueOf(fsbhashCode));
		log.info("[ReqHeartBeat]%s", sb.toString());
		sendData(sb.toString());
	}

	public void sendData(String data) {
		this.channelHandlerContext.channel().writeAndFlush(data);
	}

	public void sendInnerQuote(InnerQuote innerQuote) {
		if (marketDataListener != null) {
			marketDataListener.onQuote(innerQuote);
		}
	}

	public void sendQuoteExtend(DataObject quoteExtend) {
		if (marketDataListener != null) {
			marketDataListener.onQuoteExt(quoteExtend, QuoteSource.WIND_GENERAL);
		}
	}

	public void sendTrade(Trade trade) {
		if (marketDataListener != null) {
			marketDataListener.onTrade(trade);
		}
	}

	@SuppressWarnings("unchecked")
	public void processMsgPackRead(HashMap<Integer, Object> hashMap) {
		int packType = (int) hashMap.get(FDTFields.PacketType);
		if (packType == FDTFields.PacketArray) {
			ArrayList<HashMap<Integer, Object>> arrayList = (ArrayList<HashMap<Integer, Object>>) hashMap.get(FDTFields.ArrayOfPacket);
			for (HashMap<Integer, Object> innerHashMap : arrayList) {
				printMsgPackLog(innerHashMap);
				processGateWayMessage(null, innerHashMap);
			}
		} else {
			printMsgPackLog(hashMap);
			processGateWayMessage(null, hashMap);
		}
	}

	public void printMsgPackLog(HashMap<Integer, Object> hashMap) {
		if (marketDataLog) {
			StringBuffer sb = new StringBuffer();
			for (Object key : hashMap.keySet()) {
				if ((int) key == FDTFields.WindSymbolCode) {
					String symbol = "";
					try {
						symbol = new String((byte[]) hashMap.get(key), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						log.warn("windCode convert X!");
					}
					sb.append(key + "=" + symbol + ",");
				} else {
					sb.append(key + "=" + hashMap.get(key) + ",");
				}
			}
			log.debug(sb.toString());
		}
	}
	
	private boolean calculateMessageFlow(int rBytes, int dataReceived) {
		if (bufLenMin > rBytes) {
			bufLenMin = rBytes;
			log.info("WindC-minimal recv len from wind gateway : " + bufLenMin);
		} else {
			if (bufLenMin == 0) {
				bufLenMin = rBytes;
				log.info("WindC-first time recv len from wind gateway : " + bufLenMin);
			}
		}
		if (bufLenMax < rBytes) {
			bufLenMax = rBytes;
			log.info("WindC-maximal recv len from gateway : " + bufLenMax);
		}

		blockCount += 1;
		msDiff = System.currentTimeMillis() - msLastTime;
		if (msDiff > 1000) {
			msLastTime = System.currentTimeMillis();
			if (throughput < dataReceived * 1000 / msDiff) {
				throughput = dataReceived * 1000 / msDiff;
				if (throughput > 1024) {
					log.info("WindC-maximal throughput : " + throughput / 1024 + " KB/Sec , " + blockCount + " blocks/Sec");
				} else {
					log.info("WindC-maximal throughput : " + throughput + " Bytes/Sec , " + blockCount + " blocks/Sec");
				}
			}
			blockCount = 0;
			return true;
		}
		return false;
	}

	@Override
	public void init() throws Exception {
		isAlive = true;
		quoteMgr.init();
		connect();
	}

	@Override
	public void uninit() {
		log.info(id + " Wind uninit begin");
		isAlive = false;
		close();
		quoteMgr.uninit();
		log.info(id + " Wind uninit end");
	}

	@Override
	public boolean getState() {
		return isConnected;
	}

	@Override
	public void subscribeMarketDataState(IMarketDataStateListener listener) {
		if (marketDataStateListener == null) {
			listener.onState(isConnected, this);
			marketDataStateListener = listener;
		}
	}

	@Override
	public void unsubscribeMarketDataState(IMarketDataStateListener listener) {
		if (marketDataStateListener == listener) {
			marketDataStateListener = null;
		}
	}

	@Override
	public void subscribeMarketData(String symbol, IMarketDataListener listener) throws MarketDataException {
		if (marketDataListener == null)
			marketDataListener = listener;
		if (symbol.isEmpty())
			return;
		log.info(id + " subscribeMarketData Symbol: " + symbol);
		if (!quoteMgr.checkSymbol(symbol)) {
			subscribe(symbol);
		}
	}

	@Override
	public void unsubscribeMarketData(String instrument, IMarketDataListener listener) {
	}

	@Override
	public void subscribeMultiMarketData(List<String> subscribeList, IMarketDataListener listener) throws MarketDataException {
		if (subscribeList == null || subscribeList.size() == 0)
			return;
		StringBuffer sb = new StringBuffer();
		for (String symbol : subscribeList) {
			if (sb.length() + symbol.length() >= WindDef.SUBSCRIBE_MAX_LENGTH) {
				subscribe(sb.toString());
				sb = new StringBuffer();
			}
			if (sb.toString().equals("")) {
				sb.append(symbol);
			} else {
				sb.append(";").append(symbol);
			}
		}
		if (sb.length() > 0) {
			subscribe(sb.toString());
		}
	}

	@Override
	public void unsubscribeMultiMarketData(List<String> unSubscribeList, IMarketDataListener listener) {
		for (String symbol : unSubscribeList) {
			unsubscribeMarketData(symbol, listener);
		}
	}

	@Override
	public void clean() {
		sendClearSubscribe();
	}

	@Override
	public void processChannelActive(ChannelHandlerContext ctx) {
		sendReqHeartbeat();
		msLastTime = System.currentTimeMillis();
		isConnected = true;
		updateState(isConnected);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processChannelRead(Object msg) {
		if (msg instanceof HashMap) {
			processMsgPackRead((HashMap<Integer, Object>) msg);
			if (calculateMessageFlow(FDTFrameDecoder.getPacketLen(), FDTFrameDecoder.getReceivedBytes()))
				FDTFrameDecoder.ResetCounter();
		}
	}

	@Override
	public void processChannelInActive() {
		isConnected = false;
		updateState(isConnected);
	}

	@Override
	public void setChannelHandlerContext(ChannelHandlerContext ctx) {
		this.channelHandlerContext = ctx;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setGatewayIp(String gatewayIp) {
		this.gatewayIp = gatewayIp;
	}

	public void setGatewayPort(int gatewayPort) {
		this.gatewayPort = gatewayPort;
	}

	public void setIsSubTrans(boolean isSubTrans) {
		this.isSubTrans = isSubTrans;
	}

	public void setMarketDataLog(boolean marketDataLog) {
		this.marketDataLog = marketDataLog;
	}

	public static void main(String[] args) throws Exception {
		String logConfigFile = "conf/windlog4j.xml";
		String configFile = "conf/windadaptor.xml";
		DOMConfigurator.configure(logConfigFile);
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		log.debug("WindAdaptor Test begin");
		WindAdaptor windAdaptor = (WindAdaptor) context.getBean("windAdaptor");
		windAdaptor.init();
		log.debug("WindAdaptor Test end");
	}
}
