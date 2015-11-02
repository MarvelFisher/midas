package com.cyanspring.marketdata.adaptor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter implements AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(WindAdaptor.class);

    IWindGateWayListener windGateWayListener;

    ClientHandler(IWindGateWayListener windGateWayListener){
        this.windGateWayListener = windGateWayListener;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            windGateWayListener.processChannelRead(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn(cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind channel Active");
        windGateWayListener.setChannelHandlerContext(ctx);
        windGateWayListener.processChannelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Wind channel InActive");
        windGateWayListener.processChannelInActive();
    }

    @Override
    public void close() throws Exception {
    }
}
