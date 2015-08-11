package com.cyanspring.adaptor.future.wind;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by FDT on 15/8/10.
 */
public interface IWindGWListener {
    public void processChannelActive(ChannelHandlerContext ctx);
    public void processChannelRead(Object msg);
    public void processChannelInActive();
    public void setChannelHandlerContext(ChannelHandlerContext ctx);
}
