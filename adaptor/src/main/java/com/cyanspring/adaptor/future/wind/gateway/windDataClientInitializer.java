package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class windDataClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addLast("framer",new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()));
		pipeline.addLast("decoder",new StringDecoder());
		pipeline.addLast("encoder",new StringEncoder());
		pipeline.addLast("idleStateHandler", new IdleStateHandler(25, 10, 0));
		pipeline.addLast("idleHandler", new WindDataClientIdleHandler());
		pipeline.addLast("handler", new WindDataClientHandler());		
		
	}
}

class WindDataClientIdleHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory.getLogger(WindDataClientIdleHandler.class);
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
            		log.warn("Upstream reader idle for 25 seconds , channel will close : " + ctx.channel().remoteAddress().toString());
            		ctx.close();
            } 
            if (e.state() == IdleState.WRITER_IDLE) {
            	ctx.channel().writeAndFlush(WindGatewayHandler.addHashTail("API=ClientHeartBeat",true));
            }
        }
    } 	
}