package com.cyanspring.adaptor.future.wind;

import com.cyanspring.network.transport.FDTFrameDecoder;
import com.cyanspring.network.transport.FDTFrameEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class MsgPackClientInitializer extends ChannelInitializer<SocketChannel> {

	private IWindGWListener windGWListener;

	MsgPackClientInitializer(IWindGWListener windGWListener){
		this.windGWListener = windGWListener;
	}

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline  pipeline = arg0.pipeline();
		pipeline.addLast("decoder", new FDTFrameDecoder());
		pipeline.addLast("encoder", new FDTFrameEncoder());
		pipeline.addLast("IdleStateHandler", new IdleStateHandler(7, 3, 0));
		pipeline.addLast("ClientIdleHandler", new ClientIdleHandler(true));
		pipeline.addLast("ClientHandler", new ClientHandler(windGWListener));
	}
}
