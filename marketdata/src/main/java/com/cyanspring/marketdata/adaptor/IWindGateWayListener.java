package com.cyanspring.marketdata.adaptor;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by FDT on 15/8/10.
 */
public interface IWindGateWayListener {
    public void processChannelActive(ChannelHandlerContext ctx);
    public void processChannelRead(Object msg);
    public void processChannelInActive();
    public void setChannelHandlerContext(ChannelHandlerContext ctx);
}
