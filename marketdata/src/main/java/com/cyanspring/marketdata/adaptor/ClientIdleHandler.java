package com.cyanspring.marketdata.adaptor;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientIdleHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory
            .getLogger(ClientIdleHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                log.info("READER_IDLE");
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(makeHeartBeatMsg());
            }
        }
    }

    public String makeHeartBeatMsg() {
        StringBuffer sb = new StringBuffer("");
        sb.append("API=ClientHeartBeat");
        int hashCode = sb.toString().hashCode();
        sb.append("|Hash=");
        sb.append(String.valueOf(hashCode));
        return sb.toString();
    }
}
