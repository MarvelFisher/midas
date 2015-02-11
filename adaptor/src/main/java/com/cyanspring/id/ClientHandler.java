package com.cyanspring.id;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.TimeSpan;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import com.cyanspring.id.IdMarketDataAdaptor;
import com.cyanspring.id.QuoteMgr;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Handler implementation for Forex data message, by using command
 * subscribe/unsubscribe to decide which market data to receive.
 * 
 * @author Hudson Chen
 * 
 */
public class ClientHandler extends ChannelInboundHandlerAdapter implements
		TimerEventHandler, AutoCloseable {

	private static final Logger log = LoggerFactory
			.getLogger(IdMarketDataAdaptor.class);

	public static Date lastRecv = DateUtil.now();
	public static Date lastCheck = DateUtil.now();
	static TimerThread timer = null;
	static ChannelHandlerContext context; // context deal with server

	/**
	 * sendData to server
	 * 
	 * @param data
	 */
	public static void sendData(byte[] data) {
		final ByteBuf buffer = Unpooled.copiedBuffer(data);
		data = null;
		ChannelFuture future = context.writeAndFlush(buffer);

		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if (buffer.refCnt() > 0)
					buffer.release();
			}
		});
	}

	/**
	 * Creates a client-side handler.
	 */
	public ClientHandler() {
		if (timer == null) {
			timer = new TimerThread();
			timer.setName("ClientHandler.Timer");
			timer.TimerEvent = this;
			timer.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.
	 * channel.ChannelHandlerContext)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		context = ctx;
		IdMarketDataAdaptor adaptor = IdMarketDataAdaptor.instance;
		adaptor.updateState(true);

		logOn(adaptor.getAccount(), adaptor.getPassword());
		String[] arrSymbol = IdMarketDataAdaptor.instance.getRefSymbol();
		if (arrSymbol.length > 0) {
			subscribe(arrSymbol);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelUnregistered(io.
	 * netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) {
		IdMarketDataAdaptor.instance.updateState(false);
		IdMarketDataAdaptor.isConnected = false;
		IdMarketDataAdaptor.instance.reconClient();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel
	 * .ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			lastRecv = DateUtil.now();
			final ByteBuf buffer = (ByteBuf) msg;
			byte[] data = new byte[buffer.readableBytes()];
			buffer.readBytes(data);
			Parser.instance().processData(new Date(), data);
			// buffer.release();
			data = null;
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(io.
	 * netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty
	 * .channel.ChannelHandlerContext, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		LogUtil.logException(log, (Exception) cause);
		ctx.close();
		IdMarketDataAdaptor.isConnected = false;
		IdMarketDataAdaptor.instance.reconClient();
	}

	/**
	 * covert String to Comstock frame 0x040x20 + 4 bytes Length + String + 0x03
	 * 
	 * @param strData
	 * @return
	 */
	public static byte[] makeFrame(String strData) {
		byte[] data = null;
		int nSizeLen = 4;
		byte[] arrData = strData.getBytes();

		data = BitConverter.toBytes(arrData.length, nSizeLen);
		int nLen = arrData.length + nSizeLen + 3;

		byte[] frame = new byte[nLen];
		int i = 0;
		frame[i] = SpecialCharDef.EOT;
		frame[i + 1] = SpecialCharDef.SPC;
		System.arraycopy(data, 0, frame, i + 2, nSizeLen);
		System.arraycopy(arrData, 0, frame, i + 2 + nSizeLen, arrData.length);
		frame[nLen - 1] = SpecialCharDef.ETX;
		return frame;
	}

	/**
	 * Send login frame
	 * 
	 * @param strUsername
	 * @param strPassword
	 */
	public static void logOn(String strUsername, String strPassword) {
		FixStringBuilder login = new FixStringBuilder('=', '|');

		login.append(5022);
		login.append("LoginUser");
		login.append(5026);
		login.append(7);
		login.append(5028);
		login.append(strUsername);
		login.append(5029);
		login.append(strPassword);

		String strLogon = login.toString();
		LogUtil.logInfo(log, "[logOn] %s", strLogon);
		Util.addLog("[logOn] %s", strLogon);

		byte[] arrData = makeFrame(strLogon);

		sendData(arrData);
	}

	/**
	 * Send Subscription frame
	 * 
	 * @param nSourceID
	 *            e.g. 687 is Forex Exancge
	 */
	public static void subscribe(int nSourceID) {
		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append(5022);
		sbSymbol.append("Subscribe");
		sbSymbol.append(4);
		sbSymbol.append(nSourceID);
		sbSymbol.append(5026);
		sbSymbol.append(1);

		String strSetCTFOn = sbSymbol.toString();
		LogUtil.logInfo(log, "[Subscribe]%s", strSetCTFOn);
		Util.addLog("[Subscribe]%s", strSetCTFOn);

		byte[] arrData = makeFrame(strSetCTFOn);

		sendData(arrData);
	}

	/**
	 * Send Subscription frame
	 */
	public static void subscribeSelect() {
		ArrayList<String> list = QuoteMgr.instance().SymbolList();
		for (String s : list) {
			subscribe(IdMarketDataAdaptor.instance.getExch(), s);
		}
	}

	/**
	 * Send Subscription frame
	 * 
	 * @param arrSymbol
	 *            symbol list to subscribe
	 */
	public static void subscribe(String[] arrSymbol) {

		for (String symbol : arrSymbol) {
			subscribe(IdMarketDataAdaptor.instance.getExch(symbol), symbol);
		}
	}

	/**
	 * Send Subscription frame
	 * 
	 * @param nSourceID
	 *            : exchange
	 * @param strSymbol
	 *            Symbol "X:S"+ symbol is comstock symbol
	 */
	public static void subscribe(int nSourceID, String strSymbol) {

		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append(5022);
		sbSymbol.append("Subscribe");
		sbSymbol.append(4);
		sbSymbol.append(nSourceID);
		sbSymbol.append(5);
		String idSymbol = IdSymbolUtil.toIdSymbol(strSymbol, nSourceID);
		sbSymbol.append(idSymbol);
		sbSymbol.append(5026);
		sbSymbol.append(1);

		String strSetCTFOn = sbSymbol.toString();
		LogUtil.logInfo(log, "[Subscribe]%s", strSetCTFOn);

		byte[] arrData = makeFrame(strSetCTFOn);

		sendData(arrData);
	}

	/**
	 * Send unSubscription frame
	 * 
	 * @param nSourceID
	 *            : exchange
	 * @param strSymbol
	 *            Symbol "X:S"+ symbol is comstock symbol
	 */
	public static void unSubscribe(int nSourceID, String strSymbol) {

		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append(5022);
		sbSymbol.append("Unsubscribe");
		sbSymbol.append(4);
		sbSymbol.append(nSourceID);
		sbSymbol.append(5);
		String idSymbol = IdSymbolUtil.toIdSymbol(strSymbol, nSourceID);
		sbSymbol.append(idSymbol);

		sbSymbol.append(5026);
		sbSymbol.append(1);

		String strSetCTFOn = sbSymbol.toString();
		LogUtil.logInfo(log, "[unSubscribe]%s", strSetCTFOn);

		byte[] arrData = makeFrame(strSetCTFOn);

		sendData(arrData);
	}

	@Override
	protected void finalize() throws Throwable {
		uninit();
	}

	@Override
	public void onTimer(TimerThread objSender) {
		if (lastCheck.getTime() < lastRecv.getTime()) {
			lastCheck = lastRecv;
		}
			
		Date now = DateUtil.now();
		TimeSpan ts = TimeSpan.getTimeSpan(now, lastCheck);
		if (IdMarketDataAdaptor.isConnecting == false && lastCheck.getTime() != 0 && ts.getTotalSeconds() > 20) {
			IdMarketDataAdaptor.isConnected = false;
			IdMarketDataAdaptor.instance.sendState(false);
			lastCheck = now;
			if (IdMarketDataAdaptor.instance.getStatus() != MarketStatus.CLOSE) {
				IdMarketDataAdaptor.instance.reconClient();
			}
		}
	}

	void uninit() throws Exception {
	}

	@Override
	public void close() throws Exception {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}
}
