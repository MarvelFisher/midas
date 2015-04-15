package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.TDF_MSG_ID;
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.*;
import com.cyanspring.id.Util;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ClientHandler extends ChannelInboundHandlerAdapter implements
		TimerEventHandler, AutoCloseable {

	private static final Logger log = LoggerFactory
			.getLogger(WindFutureDataAdaptor.class);

	public static Date lastRecv = DateUtil.now();
	public static Date lastCheck = DateUtil.now();
	static TimerThread timer = null;
	static ChannelHandlerContext context; // context deal with server
	private int bufLenMin = 0,bufLenMax = 0,dataReceived = 0,blockCount = 0;
	private long msDiff = 0,msLastTime = 0,throughput = 0;

	public ClientHandler() {
		if (timer == null) {
			timer = new TimerThread();
			timer.setName("Wind ClientHandler.Timer");
			timer.TimerEvent = this;
			timer.start();
		}
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		// Discard the received data silently.
		lastRecv = DateUtil.now();
		String in = (String) msg;
		// System.out.println(in);
		try {
			String strHash = null;
			String strDataType = null;
			int dataType = -1;
			if (in != null) {
				String[] in_arr = in.split("\\|");
				for (String str : in_arr) {
					if (str.contains("API=")) {
						strDataType = str.substring(4);
					}
					if (str.contains("Hash=")) {
						strHash = str.substring(5);
					}
				}
				int endindex = in.indexOf("|Hash=");
				if (endindex > 0) {
					String tempStr = in.substring(0, endindex);
					int hascode = tempStr.hashCode();

					// Compare hash code
					if (hascode == Integer.parseInt(strHash)) {
						if (WindFutureDataAdaptor.instance.isMarketDataLog()) {
							LogUtil.logDebug(log, in);
						}
						if (strDataType.equals("DATA_FUTURE")) {
							dataType = TDF_MSG_ID.MSG_DATA_FUTURE;
						}
						if (strDataType.equals("DATA_MARKET")) {
							dataType = TDF_MSG_ID.MSG_DATA_MARKET;
						}
						if (strDataType.equals("DATA_INDEX")) {
							dataType = TDF_MSG_ID.MSG_DATA_INDEX;
						}
						if (strDataType.equals("Heart Beat")) {
							dataType = TDF_MSG_ID.MSG_SYS_HEART_BEAT;
						}
						if (strDataType.equals("QDateChange")) {
							dataType = TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE;
							LogUtil.logDebug(log, in);
						}
						if (strDataType.equals("MarketClose")) {
							dataType = TDF_MSG_ID.MSG_SYS_MARKET_CLOSE;
							LogUtil.logDebug(log, in);
						}

						WindFutureDataAdaptor.instance.processGateWayMessage(
								dataType, in_arr);

					}
				}
//				System.out.flush();
			}
		} finally {
			if(WindFutureDataAdaptor.instance.isMarketDataLog()) calculateMessageFlow(in.length());
			ReferenceCountUtil.release(msg);
		}

	}

	public void calculateMessageFlow(int rBytes){

		if(bufLenMin > rBytes)
		{
			bufLenMin = rBytes;
			log.info("WindC-minimal recv len from id : " + bufLenMin);
		} else {
			if(bufLenMin == 0) {
				bufLenMin = rBytes;
				log.info("WindC-first time recv len from id : " + bufLenMin);
			}
		}

		if(bufLenMax < rBytes) {
			bufLenMax = rBytes;
			log.info("WindC-maximal recv len from id : " + bufLenMax);
		}

		dataReceived += rBytes;
		blockCount += 1;
		msDiff = System.currentTimeMillis() - msLastTime;
		if(msDiff > 1000) {
			msLastTime = System.currentTimeMillis();
			if(throughput < dataReceived * 1000 / msDiff) {
				throughput = dataReceived * 1000 / msDiff;
				if (throughput < 1024) {
					log.info("WindC-maximal throughput : " + throughput + " Bytes/Sec, " + blockCount + " blocks/Sec, MaxBuf:" + bufLenMax);
				} else {
					log.info("WindC-maximal throughput : " + throughput / 1024 + " KB/Sec, " + blockCount + " blocks/Sec, MaxBuf:" + bufLenMax);

				}
			}
			dataReceived = 0;
			blockCount = 0;
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		LogUtil.logException(log, (Exception) cause);
		// cause.printStackTrace();
		ctx.close();
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		WindFutureDataAdaptor.isConnected = false;
		adaptor.updateState(WindFutureDataAdaptor.isConnected );
		WindFutureDataAdaptor.instance.reconClient();

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.logInfo(log, "Wind channel Active");
		context = ctx;
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		WindFutureDataAdaptor.isConnected = true;
		WindFutureDataAdaptor.isConnecting = false;
		adaptor.updateState(WindFutureDataAdaptor.isConnected);

		String[] arrSymbol = WindFutureDataAdaptor.instance.getRefSymbol();
		if (arrSymbol.length > 0) {
			for (String symbol : arrSymbol) {
				subscribe(symbol);
			}
		}

		msLastTime = System.currentTimeMillis();

		// sendRequestCodeTable("CF");

		// INDEX
		// subscribe("000300.SH");
		//
		// // STOCK
		//
		// subscribe("601318.SH"); //中國平安
		// subscribe("600030.SH"); //中信證券
		// subscribe("601628.SH"); //中國人壽
		// subscribe("601989.SH"); //中國重工
		// subscribe("600000.SH"); //浦發銀行
		// subscribe("000002.SZ"); //万科A
		// subscribe("600016.SH"); //民生银行
		// subscribe("600837.SH"); //海通证券
		// subscribe("300104.SZ"); //乐视网
		// subscribe("002230.SZ"); //科大讯飞
		//
		//
		// // FUTURE
		// subscribe("AG1506.SHF"); // 白銀
		// subscribe("CU1506.SHF"); //滬銅
		// subscribe("AU1506.SHF"); //黃金
		// subscribe("RB1505.SHF"); //螺紋鋼
		// subscribe("RU1505.SHF"); //橡膠
		// subscribe("ZN1503.SHF"); //鋅
		// subscribe("M1505.DCE"); //豆粕
		// subscribe("I1505.DCE"); // 鐵礦石
		// subscribe("L1505.DCE"); //聚乙烯
		// subscribe("Y1505.DCE"); //豆油
		// subscribe("PP1505.DCE"); //聚丙烯
		// subscribe("P1505.DCE"); //棕櫚油
		// subscribe("J1505.DCE"); //焦炭
		// subscribe("JD1505.DCE"); //雞蛋
		// subscribe("FG506.CZC"); // 玻璃
		// subscribe("RM505.CZC"); //菜籽粕
		// subscribe("TA505.CZC"); //PTA //有夜盤
		// subscribe("SR505.CZC"); //白糖 //有夜盤
		// subscribe("MA506.CZC"); //鄭醇
		// subscribe("CF505.CZC"); //棉花
		// subscribe("IF1502.CF"); // 滬深300當月
		// subscribe("IF1503.CF"); //滬深300下月

		sendReqHeartbeat(); // send request heartbeat message

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.logInfo(log, "Wind channel InActive");
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		WindFutureDataAdaptor.isConnected = false;
		adaptor.updateState(WindFutureDataAdaptor.isConnected );
	}

	@Override
	public void onTimer(TimerThread objSender) {
		if (lastCheck.getTime() < lastRecv.getTime()) {
			lastCheck = lastRecv;
		}

		Date now = DateUtil.now();
		TimeSpan ts = TimeSpan.getTimeSpan(now, lastCheck);
		if (!WindFutureDataAdaptor.isConnecting
				&& !WindFutureDataAdaptor.isConnected
				&& lastCheck.getTime() != 0 && ts.getTotalSeconds() > 20) {
			lastCheck = now;
			WindFutureDataAdaptor.instance.reconClient();
		}
	}

	/**
	 * sendData to server
	 * 
	 * @param data
	 */
	public static void sendData(String data) {
		ChannelFuture future = context.channel().writeAndFlush(data);
		// ChannelFuture future = context.writeAndFlush(data);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				LogUtil.logInfo(log, "ChannelFuture operationComplete!");
			}
		});
	}

	/**
	 * get markets
	 */
	public static void sendRequestMarket() {
		FixStringBuilder fsb = new FixStringBuilder('=', '|');

		fsb.append("API");
		fsb.append("GetMarkets");
		int fsbhashCode = fsb.toString().hashCode();
		fsb.append("Hash");
		fsb.append(String.valueOf(fsbhashCode));

		LogUtil.logInfo(log, "[RequestMarket]%s", fsb.toString());
		Util.addLog("[RequestMarket]%s", fsb.toString());
		sendData(fsb.toString() + "\r\n");
	}

	/**
	 * get exchange symbol list
	 * 
	 * @param market
	 */
	public static void sendRequestCodeTable(String market) {
		FixStringBuilder fsb = new FixStringBuilder('=', '|');

		fsb.append("API");
		fsb.append("GetCodeTable");
		fsb.append("Market");
		fsb.append(market);
		int fsbhashCode = fsb.toString().hashCode();
		fsb.append("Hash");
		fsb.append(String.valueOf(fsbhashCode));

		LogUtil.logInfo(log, "[RequestCodeTable]%s", fsb.toString());
		Util.addLog("[RequestCodeTable]%s", fsb.toString());
		sendData(fsb.toString() + "\r\n");
	}

	/**
	 * Send Request HeartBeat Message
	 */
	public static void sendReqHeartbeat() {
		FixStringBuilder fsb = new FixStringBuilder('=', '|');

		fsb.append("API");
		fsb.append("ReqHeartBeat");
		int fsbhashCode = fsb.toString().hashCode();
		fsb.append("Hash");
		fsb.append(String.valueOf(fsbhashCode));

		LogUtil.logInfo(log, "[ReqHeartBeat]%s", fsb.toString());
		Util.addLog("[ReqHeartBeat]%s", fsb.toString());
		sendData(fsb.toString() + "\r\n");

	}

	/**
	 * Send Subscription frame
	 * 
	 * @param symbol
	 *            e.g. IF1502
	 */
	public static void subscribe(String symbol) {
		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append("API");
		sbSymbol.append("SUBSCRIBE");
		sbSymbol.append("Symbol");
		sbSymbol.append(symbol);

		String subscribeStr = sbSymbol.toString();

		// LogUtil.logInfo(log, String.valueOf(subscribeStr.hashCode()));
		subscribeStr = subscribeStr + "|Hash="
				+ String.valueOf(subscribeStr.hashCode());
		LogUtil.logInfo(log, "[Subscribe]%s", subscribeStr);
		Util.addLog("[Subscribe]%s", subscribeStr);

		sendData(subscribeStr + "\r\n");
	}

	/**
	 * Send unSubscription frame
	 * 
	 * @param symbol
	 *            e.g. IF1502
	 */
	public static void unSubscribe(String symbol) {

		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append("API");
		sbSymbol.append("UNSUBSCRIBE");
		sbSymbol.append("Symbol");
		sbSymbol.append(symbol);

		String unsubscribeStr = sbSymbol.toString();

		// LogUtil.logInfo(log, String.valueOf(subscribeStr.hashCode()));
		unsubscribeStr = unsubscribeStr + "|Hash="
				+ String.valueOf(unsubscribeStr.hashCode());
		LogUtil.logInfo(log, "[UnSubscribe]%s", unsubscribeStr);
		Util.addLog("[UnSubscribe]%s", unsubscribeStr);

		sendData(unsubscribeStr + "\r\n");
	}

	/**
	 * send Clear Subscription frame
	 */
	public static void sendClearSubscribe() {
		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append("API");
		sbSymbol.append("ClearSubscribe");

		String subscribeStr = sbSymbol.toString();

		subscribeStr = subscribeStr + "|Hash="
				+ String.valueOf(subscribeStr.hashCode());
		LogUtil.logInfo(log, "[sendClearSubscribe]%s", subscribeStr);
		Util.addLog("[sendClearSubscribe]%s", subscribeStr);

		sendData(subscribeStr + "\r\n");
	}

	@Override
	public void close() throws Exception {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	void uninit() throws Exception {
	}

	// public static void main(String[] args) {
	// sendReqHeartbeat();
	// }

}
