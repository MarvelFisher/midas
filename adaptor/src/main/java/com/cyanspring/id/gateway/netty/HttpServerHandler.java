package com.cyanspring.id.gateway.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.gateway.IdGateway;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final Logger log = LoggerFactory.getLogger(IdGateway.class);

	// private FullHttpRequest fullHttpRequest;

	private HttpPostRequestDecoder decoder;

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}

	public void messageReceived(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {

		FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;

		if (IdGateway.isSSL) {
			System.out
					.println("Your session is protected by "
							+ ctx.pipeline().get(SslHandler.class).engine()
									.getSession().getCipherSuite()
							+ " cipher suite.\n");
		}

		HttpUrlParam params = HttpUrlParam.parse(fullHttpRequest.getUri());

		WebTask.onTask(ctx, params);

		params.close();
	}

	/*
	 * private void reset() { fullHttpRequest = null; // destroy the decoder to
	 * release all resources decoder.destroy(); decoder = null; }
	 */

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LogUtil.logException(log, (Exception) cause);
		sendData(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, String.format(
				"Error: %s%n", HttpResponseStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		messageReceived(ctx, msg);
	}

	public static void sendData(ChannelHandlerContext ctx,
			HttpResponseStatus status, String strContent) {
		sendData(ctx, status, strContent, CharsetUtil.UTF_8);
	}

	public static void sendData(ChannelHandlerContext ctx,
			HttpResponseStatus status, String strContent, Charset enc) {

		ByteBuf buffer = Unpooled.copiedBuffer(strContent.getBytes(enc));
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, buffer);
		response.setStatus(status);
		response.headers().set(CONTENT_TYPE,
				String.format("text/html; charset=%s", enc.toString()));
		response.headers().set(CONTENT_LENGTH, buffer.readableBytes());
		
		// Close the connection as soon as the error message is sent.
		try {
			ctx.channel().writeAndFlush(response).sync();
		} catch (InterruptedException e) {
			LogUtil.logException(log, e);
		}			
	}

	public static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {

		ByteBuf buffer = Unpooled.copiedBuffer(String.format("Failure: %s%n",
				status.toString()).getBytes(CharsetUtil.UTF_8));
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, buffer);
		response.setStatus(status);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().set(CONTENT_LENGTH, buffer.readableBytes());

		// Close the connection as soon as the error message is sent.
		try {
			ctx.channel().writeAndFlush(response).sync();
		} catch (InterruptedException e) {
			LogUtil.logException(log, e);
		}			
	}

}