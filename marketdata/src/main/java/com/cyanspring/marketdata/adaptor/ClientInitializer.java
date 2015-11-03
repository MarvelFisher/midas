package com.cyanspring.marketdata.adaptor;

import com.cyanspring.marketdata.util.FDTFrameDecoder;
import com.cyanspring.marketdata.util.FDTFrameEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	private IWindGateWayListener windGateWayListener;

	ClientInitializer(IWindGateWayListener windGateWayListener){
		this.windGateWayListener = windGateWayListener;
	}

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline  pipeline = arg0.pipeline();
		pipeline.addLast("decoder", new FDTFrameDecoder());
		pipeline.addLast("encoder", new FDTFrameEncoder());
		pipeline.addLast("IdleStateHandler", new IdleStateHandler(7, 3, 0));
		pipeline.addLast("ClientIdleHandler", new ClientIdleHandler());
		pipeline.addLast("ClientHandler", new ClientHandler(windGateWayListener));
	}
}
