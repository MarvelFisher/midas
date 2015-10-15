package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.network.transport.FDTFrameDecoder;
import com.cyanspring.network.transport.FDTFrameEncoder;
import com.cyanspring.adaptor.future.wind.ClientIdleHandler;
import com.cyanspring.adaptor.future.wind.IWindGWListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class MsgPackRefDataClientInitializer extends ChannelInitializer<SocketChannel> {

	private IWindGWListener windGWListener;

	MsgPackRefDataClientInitializer(IWindGWListener windGWListener){
		this.windGWListener = windGWListener;
	}

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline  pipeline = arg0.pipeline();
		pipeline.addLast("decoder", new FDTFrameDecoder());
		pipeline.addLast("encoder", new FDTFrameEncoder());
		pipeline.addLast("IdleStateHandler", new IdleStateHandler(7, 3, 0));
		pipeline.addLast("ClientIdleHandler", new ClientIdleHandler(true));
		pipeline.addLast("MsgPackRefDataClientHandler", new MsgPackRefDataClientHandler(this.windGWListener));
	}
}
