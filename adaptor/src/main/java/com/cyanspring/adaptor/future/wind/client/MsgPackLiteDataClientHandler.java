package com.cyanspring.adaptor.future.wind.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MsgPackLiteDataClientHandler extends ChannelInboundHandlerAdapter {
	static ChannelHandlerContext ctx = null;
	private static final Logger log = LoggerFactory
			.getLogger(MsgPackLiteDataClientHandler.class);	

	public void channelRead(ChannelHandlerContext arg0, Object arg1) throws Exception {
		String in = (String)arg1;
		try {
			if(in != null) {
				System.out.println("Server Response : " + in);
			}
	    } finally {
	        ReferenceCountUtil.release(arg1); 
	    }		
	
	}

	public void exceptionCaught(ChannelHandlerContext arg0, Throwable e)
			throws Exception {
		// TODO Auto-generated method stub
		log.error(e.getMessage(),e);
		if(ctx != null) {
			ctx.close();
			ctx = null;
		}
	}


	public void handlerAdded(ChannelHandlerContext arg0) throws Exception {
		// TODO Auto-generated method stub
		ctx = arg0;
	}


	public void handlerRemoved(ChannelHandlerContext arg0) throws Exception {
		// TODO Auto-generated method stub
		if(ctx == arg0) {
			ctx = null;
		}
	}
		

}
