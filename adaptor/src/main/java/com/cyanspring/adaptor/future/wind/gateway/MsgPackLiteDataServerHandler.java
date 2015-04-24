package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MsgPackLiteDataServerHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(MsgPackLiteDataServerHandler.class);
	
	ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		Channel channel = arg0.channel();
		channels.writeAndFlush(channel.remoteAddress().toString() +  " join");
		channel.writeAndFlush("You had connected with server!");
		channels.add(channel);

		log.info("Add Client : " + channel.remoteAddress().toString());
	}
	
	public void channelInactive(ChannelHandlerContext arg0) throws Exception {
		Channel channel = arg0.channel();
		channels.remove(channel);
		channels.writeAndFlush(channel.remoteAddress().toString() +  " exit");;
		log.info("Remove Client : " + channel.remoteAddress().toString());
	}	
	
	public void channelRead(ChannelHandlerContext arg0, Object arg1)
			throws Exception {
		String in = (String)arg1;
		try {
			if(in != null) {
				channels.writeAndFlush(in);
			}
	    } finally {
	        ReferenceCountUtil.release(arg1);
	    }			
	}	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
			throws Exception {    	
		log.info(e.getMessage(),e);
		ctx.close();
	}	
}
