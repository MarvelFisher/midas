package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.adaptor.future.wind.IWindGWListener;
import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.id.Library.Util.FixStringBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MsgPackRefDataClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory
            .getLogger(MsgPackRefDataClientHandler.class);

    private ChannelHandlerContext context;
    private IWindGWListener windGWListener;

    MsgPackRefDataClientHandler(IWindGWListener windGWListener){
        this.windGWListener = windGWListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HashMap) windGWListener.processChannelRead(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind RefData channel Active");
        this.context = ctx;
        sendReqHeartbeat(); // send request heartbeat message
        windGWListener.setChannelHandlerContext(ctx);
        windGWListener.processChannelActive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind RefData channel InActive");
        windGWListener.processChannelInActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    /**
     * Send Request HeartBeat Message
     */
    public void sendReqHeartbeat() {
        FixStringBuilder fsb = new FixStringBuilder('=', '|');
        fsb.append("API");
        fsb.append("ReqHeartBeat");
        int fsbhashCode = fsb.toString().hashCode();
        fsb.append("Hash");
        fsb.append(String.valueOf(fsbhashCode));
        log.info("[ReqHeartBeat]" + fsb.toString());
        sendData(fsb.toString());
    }

    /**
     * sendData to server
     *
     * @param data
     */
    public void sendData(String data) {
        context.channel().writeAndFlush(data);
    }
}
