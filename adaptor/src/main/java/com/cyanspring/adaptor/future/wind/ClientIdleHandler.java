package com.cyanspring.adaptor.future.wind;

import com.cyanspring.id.Library.Util.FixStringBuilder;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientIdleHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory
            .getLogger(WindGateWayAdapter.class);

    private boolean isMsgPack = false;

    public ClientIdleHandler(boolean isMsgPack){
        this.isMsgPack = isMsgPack;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                LogUtil.logInfo(log, "READER_IDLE");
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(makeHeartBeatMsg());
            }
        }
    }

    /**
     * Make Client HeartBeat Message
     */
    public String makeHeartBeatMsg() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');

        fsb.append("API");
        fsb.append("ClientHeartBeat");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));
        if(!isMsgPack) fsb.append("\r\n");
        return fsb.toString();
    }
}
