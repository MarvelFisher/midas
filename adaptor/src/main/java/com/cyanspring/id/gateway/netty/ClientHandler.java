package com.cyanspring.id.gateway.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.gateway.MarketStatus;
import com.cyanspring.id.Library.Util.BitConverter;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.IdSymbolUtil;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.Network.SpecialCharDef;
import com.cyanspring.id.gateway.IDGateWayDialog;
import com.cyanspring.id.gateway.IdGateway;
import com.cyanspring.id.gateway.Parser;
import com.cyanspring.id.gateway.QuoteMgr;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter implements
		AutoCloseable {
	public static final Integer connected = 1;
	public static final Integer disConnected = 2;
	private static final Logger log = LoggerFactory.getLogger(IdGateway.class);
	public static ClientHandler Instance = null;
	static ChannelHandlerContext ctx; // context deal with server
	//static TimerThread timer = null;
	public static Date lastRecv = new Date(0);
	public static Date lastCheck = new Date(0);

	/**
	 * sendData to server
	 * 
	 * @param data
	 */
	public static void sendData(byte[] data) {
		ctx.writeAndFlush(Unpooled.copiedBuffer(data));
		data = null;
		/*
		final ByteBuf buffer = Unpooled.copiedBuffer(data);
		data = null;

		ChannelFuture future = ctx.writeAndFlush(buffer);

		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if (buffer != null && buffer.refCnt() > 0)
					buffer.release();
			}
		});
		*/

	}

	/**
	 * Creates a client-side handler.
	 */
	public ClientHandler() {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ClientHandler.ctx = null;
		ctx.pipeline().fireUserEventTriggered(ClientHandler.disConnected);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ClientHandler.ctx = ctx;
		ctx.pipeline().fireUserEventTriggered(ClientHandler.connected);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				if (IdGateway.isConnecting == false) {
					IdGateway.isConnected = false;
					if (IdGateway.instance().getStatus() != MarketStatus.CLOSE) {
						IdGateway.instance().reconClient();
					}
					log.error("Read idle");
				}				
			}
		}
		else {
		if (evt == ClientHandler.connected) {
			logOn(IdGateway.instance().getAccount(), IdGateway.instance()
					.getPassword());
			if (IdGateway.instance().isGateway() == true) {
				setCTFOn(IdGateway.instance().getExch());
			} else {
				setCTFOnSymbols();
			}
		} else if (evt == ClientHandler.disConnected) {
			IdGateway.isConnected = false;
			IdGateway.instance().reconClient();
		}
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			ByteBuf buffer = (ByteBuf) msg;
			byte[] data = new byte[buffer.readableBytes()];
			buffer.readBytes(data);
			Parser.Instance().processData(data);
			IdGateway.instance().addSize(IDGateWayDialog.TXT_InSize,
					data.length);
			data = null;
			lastRecv = DateUtil.now();
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	static String getRemotIP(Channel ch) {
		InetSocketAddress socketAddress = (InetSocketAddress) ch
				.remoteAddress();
		InetAddress inetaddress = socketAddress.getAddress();
		return inetaddress.getHostAddress(); // IP address of
												// client
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		String strIP = getRemotIP(ctx.channel());
		LogUtil.logError(log, "[%s] Exception : %s", strIP, cause.getMessage());
		LogUtil.logException(log, (Exception) cause);
		ctx.close();
		IdGateway.isConnected = false;
		IdGateway.instance().reconClient();
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
		IdGateway.instance().addLog("[logOn] %s", strLogon);

		byte[] arrData = makeFrame(strLogon);

		sendData(arrData);
	}

	/**
	 * Send Subscription frame
	 * 
	 * @param nSourceID
	 *            e.g. 687 is Forex Exancge
	 */
	public static void setCTFOn(int nSourceID) {
		FixStringBuilder sbSymbol = new FixStringBuilder('=', '|');

		sbSymbol.append(5022);
		sbSymbol.append("Subscribe");
		sbSymbol.append(4);
		sbSymbol.append(nSourceID);
		sbSymbol.append(5026);
		sbSymbol.append(1);
		if (IdGateway.instance().isGateway()) {
			sbSymbol.append(9999);
			sbSymbol.append(1);
		}

		String strSetCTFOn = sbSymbol.toString();
		LogUtil.logInfo(log, "[setCTFOn]%s", strSetCTFOn);
		IdGateway.instance().addLog("[setCTFOn]%s", strSetCTFOn);

		byte[] arrData = makeFrame(strSetCTFOn);

		sendData(arrData);
	}

	/**
     * 
     */
	public static void setCTFOnSymbols() {
		
		ArrayList<String> list = QuoteMgr.Instance().getSymbolList();
		for (String s : list) {
			int exch = IdGateway.instance().getExch(s);
			setCTFOn(exch, s);
		}

		IdGateway.instance().addLog("[setCTFOnSymbols] count : %d", list.size());
	}

	/**
	 * Send Subscription frame
	 * 
	 * @param nSourceID
	 *            : exchange
	 * @param strSymbol
	 *            Symbol "X:S"+ symbol is comstock symbol
	 */
	public static void setCTFOn(int nSourceID, String strSymbol) {
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
		LogUtil.logInfo(log, "[setCTFOn]%s", strSetCTFOn);

		byte[] arrData = makeFrame(strSetCTFOn);

		sendData(arrData);
	}

	@Override
	protected void finalize() throws Throwable {
		uninit();

	}

	void uninit() throws Exception {
	}

	@Override
	public void close() throws Exception {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}
}
