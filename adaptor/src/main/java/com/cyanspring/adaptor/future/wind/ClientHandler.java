package com.cyanspring.adaptor.future.wind;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.wind.td.tdf.TDF_MSG_ID;

import com.cyanspring.id.Util;
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.TimeSpan;

public class ClientHandler extends ChannelInboundHandlerAdapter implements
		TimerEventHandler, AutoCloseable {

	private static final Logger log = LoggerFactory
			.getLogger(WindFutureDataAdaptor.class);

	public static Date lastRecv = DateUtil.now();
	public static Date lastCheck = DateUtil.now();
	static TimerThread timer = null;
	static ChannelHandlerContext context; // context deal with server

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
		System.out.println(in);
		try {
			String strHash = null;
			String strDataType = null;
			int datatype = -1;
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
						if (strDataType.equals("DATA_FUTURE"))
							datatype = TDF_MSG_ID.MSG_DATA_FUTURE;
						// if (strDataType.equals("DATA_MARKET"))
						// datatype = TDF_MSG_ID.MSG_DATA_MARKET;
						if (strDataType.equals("Heart Beat"))
							datatype = TDF_MSG_ID.MSG_SYS_HEART_BEAT;
						if (strDataType.equals("QDateChange")) {
							datatype = TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE;
							LogUtil.logDebug(log, in);
						}
						if (strDataType.equals("MarketClose")) {
							datatype = TDF_MSG_ID.MSG_SYS_MARKET_CLOSE;
							LogUtil.logDebug(log, in);
						}
						WindFutureDataAdaptor.instance.processGateWayMessage(
								datatype, in_arr);
					}
				}
				System.out.flush();
			}
		} finally {
			ReferenceCountUtil.release(msg);
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
		adaptor.updateState(false);
		WindFutureDataAdaptor.isConnected = false;
		WindFutureDataAdaptor.instance.reconClient();

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.logInfo(log, "Wind channel Active");
		context = ctx;
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		adaptor.updateState(true);
		String[] arrSymbol = WindFutureDataAdaptor.instance.getRefSymbol();
		String exchange = WindFutureDataAdaptor.instance.getExchange();
		if (arrSymbol.length > 0) {
			for (String symbol : arrSymbol) {
				subscribe(symbol + "." + exchange);
			}
		}
		sendReqHeartbeat(); // send request heartbeat message
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.logInfo(log, "Wind channel InActive");
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		adaptor.updateState(false);
	}

	@Override
	public void onTimer(TimerThread objSender) {
		if (lastCheck.getTime() < lastRecv.getTime()) {
			lastCheck = lastRecv;
		}

		Date now = DateUtil.now();
		TimeSpan ts = TimeSpan.getTimeSpan(now, lastCheck);
		if (WindFutureDataAdaptor.isConnecting == false
				&& lastCheck.getTime() != 0 && ts.getTotalSeconds() > 20) {
			// WindFutureDataAdaptor.isConnected = false;
			// WindFutureDataAdaptor.instance.sendState(false);
			lastCheck = now;
			// if (WindFutureDataAdaptor.instance.getStatus() !=
			// MarketStatus.CLOSE) {
			// System.out.println("reconnect!");
			WindFutureDataAdaptor.instance.reconClient();
			// }
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
		Util.addLog("[UnSubscribe]%s", fsb.toString());
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

	@Override
	public void close() throws Exception {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	void uninit() throws Exception {
	}

//	public static void main(String[] args) {
//		sendReqHeartbeat();
//	}

}
