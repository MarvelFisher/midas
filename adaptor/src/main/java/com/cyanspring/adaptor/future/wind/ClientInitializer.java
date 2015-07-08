package com.cyanspring.adaptor.future.wind;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline  pipeline = arg0.pipeline();
		pipeline.addLast("fromer", new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()));
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("IdleStateHandler", new IdleStateHandler(25, 10, 0));
		pipeline.addLast("ClientIdleHandler", new ClientIdleHandler(false));
		pipeline.addLast("ClientHandler", new ClientHandler());
	}
}
