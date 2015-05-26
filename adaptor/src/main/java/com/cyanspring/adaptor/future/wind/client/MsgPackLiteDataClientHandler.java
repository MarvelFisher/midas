package com.cyanspring.adaptor.future.wind.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
















import com.cyanspring.Network.Transport.FDTFields;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MsgPackLiteDataClientHandler extends ChannelInboundHandlerAdapter {
	static ChannelHandlerContext ctx = null;
	private static final Logger log = LoggerFactory
			.getLogger(MsgPackLiteDataClientHandler.class);	

	@SuppressWarnings("unchecked")
	public void channelRead(ChannelHandlerContext arg0, Object msg) {
		try {
			if(msg instanceof byte[]) {
				String in = new String((byte[])msg,"UTF-8");
				if(in != null) {
					System.out.println("Server Response : " + in);
				}				
			} else if(msg instanceof Map<?,?>) {
				processMessagePack(msg);							
			}
		} catch(Exception e) {
			log.error(e.getMessage(),e);
	    } finally {
	        ReferenceCountUtil.release(msg); 
	    }			
	}

	public void handlerActive(ChannelHandlerContext arg0) throws Exception {
		ctx = arg0;
		log.info("Connected with server : " + ctx.channel().remoteAddress().toString());
	}


	public void handlerInactive(ChannelHandlerContext arg0) throws Exception {
		if(ctx == arg0) {
			ctx = null;
		}
		log.info("Disconnect with server : " + ctx.channel().remoteAddress().toString());
	}
	
	static public void processMessagePack(Object msg) {
		try {
			@SuppressWarnings("unchecked")
			HashMap<Integer,Object> map = (HashMap<Integer,Object>)msg;
			if(map.containsKey(1) == false) {		
				return;
			}
			int packetType = (int)map.get(FDTFields.PacketType);
			if(packetType == FDTFields.WindMarkets) {
				@SuppressWarnings("unchecked")
				ArrayList<byte[]> markets = (ArrayList<byte[]>)map.get(FDTFields.ArrayOfString);
				for(byte[] market : markets) {
					log.info("Market : " + new String(market,"UTF-8"));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable e) throws Exception {
		log.error(e.getMessage(),e);
		if(ctx != null) {
			ctx.close();
			ctx = null;
		}
	}	

}
