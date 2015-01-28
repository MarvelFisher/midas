package com.cyanspring.id.gateway.netty;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Threading.CustomThreadPool;
import com.cyanspring.id.Library.Threading.Delegate;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.Network.SocketUtil;
import com.cyanspring.id.gateway.IDGateWayDialog;
import com.cyanspring.id.gateway.IdGateway;
import com.cyanspring.id.gateway.UserClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(IdGateway.class);

	static final Method methodSend = Delegate.getMethod("onAsyncSend",
			ServerHandler.class, new Class[] { byte[].class });

	static final Method methodSend2 = Delegate.getMethod("onAsyncSend",
			ServerHandler.class, new Class[] { String.class, byte[].class });

	public static void onAsyncSend(byte[] data) {
		sendData(data);
		data = null;
	}

	public static void onAsyncSend(String symbol, byte[] data) {
		sendData(symbol, data);
		data = null;
	}

	public static void asyncSendData(byte[] data) {
		CustomThreadPool.asyncMethod(methodSend, data);
		data = null;
	}

	public static void asyncSendData(String symbol, byte[] data) {
		CustomThreadPool.asyncMethod(methodSend2, symbol, data);
		data = null;
	}

	public static final ChannelGroup channels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	public static final List<UserClient> clientlist = new ArrayList<UserClient>();

	public static Object clLock = new Object();

	public static List<UserClient> getClients() {
		return new ArrayList<UserClient>(clientlist);
	}

	public static UserClient getUserClient(ChannelHandlerContext ctx) {

		UserClient[] arr = new UserClient[clientlist.size()];
		for (UserClient client : clientlist.toArray(arr)) {
			if (client.isSameContext(ctx)) {
				return client;
			}
		}

		// else client not exist, create it
		synchronized (clLock) {
			UserClient newClient = new UserClient(ctx);
			clientlist.add(newClient);
			return newClient;
		}
	}

	public static void removeUserClient(ChannelHandlerContext ctx) {

		//if (channels.size() <= 0)
		//	return;
		
		List<UserClient> list = new ArrayList<UserClient>();
		list.addAll(clientlist);
		for (UserClient client : list) {
			
			if (client.isSameContext(ctx)) {
				try {
					client.close();
				} catch (Exception e) {
					LogUtil.logException(log, e);
				}
				
				synchronized (clLock) {
					clientlist.remove(client);
				}
			}
		}
	}

	public static void sendData(String strData) {
		sendData(strData.getBytes());
	}

	public static void sendData(String symbol, byte[] data) {

		byte[] packetData = null;
		try {

			if (channels.size() <= 0) {				
				data = null;
				return;
			}
			packetData = SocketUtil.packData(data);
			data = null;
			
			List<UserClient> list = new ArrayList<UserClient>();
			list.addAll(clientlist);
			for (UserClient client : list) {
				client.sendData(symbol, packetData);
			}
		} finally {
			packetData = null;
		}
	}

	public static void sendData(byte[] data) {
		if (channels.size() <= 0) {
			data = null;
			return;
		}
		byte[] packetData = SocketUtil.packData(data);
		data = null;
		IdGateway.instance().addSize(IDGateWayDialog.TXT_OutSize, packetData.length);

		final ByteBuf buffer = Unpooled.copiedBuffer(packetData);
		packetData = null;
		ChannelGroupFuture future = channels.writeAndFlush(buffer);
		future.addListener(new ChannelGroupFutureListener() {
			@Override
			public void operationComplete(ChannelGroupFuture arg0)
					throws Exception {
				if (buffer.refCnt() > 0)
					buffer.release();
			}
		});
	}

	public static void sendData(ChannelHandlerContext ctx, String symbol,
			String strData) {
		sendData(ctx, symbol, strData.getBytes());
	}

	public static void sendData(ChannelHandlerContext ctx, String symbol,
			byte[] data) {

		if (!channels.contains(ctx.channel())) {
			data = null;
			return;
		}

		final ByteBuf buffer = Unpooled.copiedBuffer(data);
		data = null;
		ChannelFuture future = ctx.writeAndFlush(buffer);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if (buffer.refCnt() > 0)
					buffer.release();
			}
		});
	}

	public static String getRemotIP(Channel ch) {
		InetSocketAddress socketAddress = (InetSocketAddress) ch
				.remoteAddress();
		InetAddress inetaddress = socketAddress.getAddress();
		return inetaddress.getHostAddress(); // IP address of client
	}

	public void updateContext() {

		ArrayList<String> list = new ArrayList<String>();
		for (Channel ch : ServerHandler.channels) {
			String ipAddress = getRemotIP(ch);
			list.add(ipAddress);
		}

		IdGateway.instance().updateClient(list);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		if (!channels.contains(ctx.channel())) {
			channels.add(ctx.channel());
			updateContext();
			String strIP = getRemotIP(ctx.channel());
			IdGateway.instance().addLog("add new Client : [%s]", strIP);
		}

		UserClient client = getUserClient(ctx);
		LogUtil.logInfo(log, "new Client ip:%s key:%s", client.getIp(),
				client.getKey());

	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) {
		String strIP = getRemotIP(ctx.channel());
		removeUserClient(ctx);
		ctx.close();
		updateContext();
		IdGateway.instance().addLog("remove Client : [%s]", strIP);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		final ByteBuf buffer = (ByteBuf) msg;
		byte[] data = new byte[buffer.readableBytes()];
		buffer.readBytes(data);
		buffer.release();
		UserClient client = getUserClient(ctx);
		client.onReceive(data);
		data = null;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		String strIP = getRemotIP(ctx.channel());
		LogUtil.logError(log, "[%s] Exception : %s", strIP, cause.getMessage());
		LogUtil.logException(log, (Exception) cause);
		removeUserClient(ctx);
		ctx.close();
	}
}
