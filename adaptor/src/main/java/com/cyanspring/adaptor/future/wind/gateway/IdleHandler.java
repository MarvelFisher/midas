package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class IdleHandler extends ChannelDuplexHandler {
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.IdleHandler.class);
	
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
            	String str = "[Server] - Read Timeout : " + ctx.channel().remoteAddress();
            	log.warn(str);
            	System.out.println(str);
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
            	String str = WindGatewayHandler.addHashTail("API=ServerHeartBeat");
                ctx.writeAndFlush(str);
            }
        }
    }
}
