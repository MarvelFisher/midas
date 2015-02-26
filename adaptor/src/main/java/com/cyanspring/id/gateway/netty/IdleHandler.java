package com.cyanspring.id.gateway.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.id.MarketStatus;
import com.cyanspring.id.gateway.IdGateway;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class IdleHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory
			.getLogger(IdleHandler.class);

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				if (IdGateway.isConnecting == false) {
					IdGateway.isConnected = false;
					if (IdGateway.instance().getStatus() != MarketStatus.CLOSE) {
						IdGateway.instance().reconClient();
					}
					log.error("Read idle");
				}
				
			}
/*			
			else if (e.state() == IdleState.WRITER_IDLE) {
				// String str =
				// WindGatewayHandler.addHashTail("API=ServerHeartBeat");
				// ctx.writeAndFlush(str);
			}
*/			
		}
	}
}