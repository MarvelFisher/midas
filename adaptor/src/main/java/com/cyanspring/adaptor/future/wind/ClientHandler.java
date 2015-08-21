package com.cyanspring.adaptor.future.wind;

import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter implements AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(WindGateWayAdapter.class);

    IWindGWListener windGWListener;

    ClientHandler(IWindGWListener windGWListener){
        this.windGWListener = windGWListener;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            windGWListener.processChannelRead(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        LogUtil.logException(log, (Exception) cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.logInfo(log, "Wind channel Active");
        windGWListener.setChannelHandlerContext(ctx);
        windGWListener.processChannelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.logInfo(log, "Wind channel InActive");
        windGWListener.processChannelInActive();
    }

    @Override
    public void close() throws Exception {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }

    void uninit() throws Exception {
    }

}
