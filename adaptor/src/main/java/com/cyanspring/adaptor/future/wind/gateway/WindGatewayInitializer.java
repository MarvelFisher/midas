package com.cyanspring.adaptor.future.wind.gateway;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class WindGatewayInitializer extends ChannelInitializer<SocketChannel> {

	//public static WindGatewayHandler windGatewayHandler = new WindGatewayHandler();
	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline pipeline = arg0.pipeline();
		
		pipeline.addLast("framer",new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()));
		pipeline.addLast("decoder",new StringDecoder());
		pipeline.addLast("encoder",new StringEncoder());

		//windGatewayHandler = new WindGatewayHandler();
		pipeline.addLast("handler", new WindGatewayHandler()); // new WindGatewayHandler()); // windGatewayHandler);
		
	}

}
