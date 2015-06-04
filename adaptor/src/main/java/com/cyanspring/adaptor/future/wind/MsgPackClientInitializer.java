package com.cyanspring.adaptor.future.wind;

import com.cyanspring.Network.Transport.FDTFrameDecoder;
import com.cyanspring.Network.Transport.FDTFrameEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class MsgPackClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline  pipeline = arg0.pipeline();

		pipeline.addLast("decoder", new FDTFrameDecoder());
		pipeline.addLast("encoder", new FDTFrameEncoder());
		pipeline.addLast("IdleStateHandler", new IdleStateHandler(5, 3, 0));
		pipeline.addLast("ClientIdleHandler", new ClientIdleHandler());
		pipeline.addLast("ClientHandler", new ClientHandler());
		
	}
}
