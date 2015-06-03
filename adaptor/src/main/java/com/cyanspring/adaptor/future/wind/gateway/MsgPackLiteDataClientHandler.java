package com.cyanspring.adaptor.future.wind.gateway;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.Network.Transport.FDTFields;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MsgPackLiteDataClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(MsgPackLiteDataClientHandler.class);
	public static ChannelHandlerContext ctx = null;
	
	private int bufLenMin = 0,bufLenMax = 0,blockCount = 0;
	private long throughput = 0,dataReceived = 0,msLastTime = 0,msDiff = 0;
	
	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		
		msLastTime = System.currentTimeMillis();
		ctx = arg0;
		ctx.channel().write(WindGatewayHandler.addHashTail("API=ReqHeartBeat",true));
		ctx.channel().write(WindGatewayHandler.addHashTail("API=GetMarkets",true));
		WindGatewayHandler.resubscribe(ctx.channel(),MsgPackLiteDataServerHandler.registrationGlobal);
		ctx.channel().flush();
		log.info(ctx.channel().localAddress().toString() + " Connected with data server : " + ctx.channel().remoteAddress().toString());

	}	
	
	public static void sendRequest(String str) {
		if(ctx != null) {
			ctx.writeAndFlush(str);
		}
	}


	public void channelInactive(ChannelHandlerContext arg0) throws Exception {
		ctx = null;

	}
	
	public void channelRead(ChannelHandlerContext arg0, Object arg1)
			throws Exception {

		try {
			if(arg1 instanceof HashMap<?,?>) {
				HashMap<Integer,Object> in = (HashMap<Integer,Object>)arg1;
				if(in != null) {
					processData(in);
					//calculateMessageFlow(in.length());					
				}
			} 
	    } finally {
	        ReferenceCountUtil.release(arg1);
	    }		
	
	}
	
	private void calculateMessageFlow(int rBytes) {
		if(bufLenMin > rBytes) 
		{
			bufLenMin = rBytes;
			log.info("minimal recv len from wind gateway : " + bufLenMin);			
		} else {
			if(bufLenMin == 0) {
				bufLenMin = rBytes;
				log.info("first time recv len from wind gateway : " + bufLenMin);	
			}
		}				
		if(bufLenMax < rBytes) {
			bufLenMax = rBytes;
			log.info("maximal recv len from gateway : " + bufLenMax);				
		}
		
		dataReceived += rBytes;		
		blockCount += 1;
		msDiff = System.currentTimeMillis() - msLastTime;
		if(msDiff > 1000) {
			msLastTime = System.currentTimeMillis();
			if(throughput < dataReceived * 1000 / msDiff) {
				throughput = dataReceived * 1000 / msDiff;
				if(throughput > 1024) {
					log.info("maximal throughput : " + throughput / 1024 + " KB/Sec , " + blockCount + " blocks/Sec");
				} else {
					log.info("maximal throughput : " + throughput + " Bytes/Sec , " + blockCount + " blocks/Sec");
				}
			}
			dataReceived = 0;
			blockCount = 0;
		}
	}
	
	public void channelReadComplete(ChannelHandlerContext arg0)
			throws Exception {
		arg0.flush();

	}	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {    	
    	log.warn("ExceptionCaught : " + cause.getMessage() + " - " + ctx.channel().remoteAddress().toString());
		ctx.close();
		
		if(cause.getMessage() == "Direct buffer memory") {
			log.info("Request System GC");
			System.gc();
		} else {
	    	log.warn(cause.getMessage(),cause);	     		
		}
	}
	
	private void processData(HashMap<Integer,Object> in) {
		
		try {
			// 沒有 packet type , 就無法處理.
			if(in.containsKey(FDTFields.PacketType) == false) {
				return;
			}
			int iPacketType = (int)in.get(FDTFields.PacketType);
			switch(iPacketType) {
			case FDTFields.WindMarkets :
				if(in.containsKey(FDTFields.ArrayOfString)) {
					processMarkets(in.get(FDTFields.ArrayOfString));
				}
				break;
			}	
		}
		catch(Exception e) {
			
		}
	}	
	
	private void processMarkets(Object obj) throws Exception 
	{
		if(obj == null) {
			return;			
		}
		ArrayList<String> markets = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		ArrayList<byte[]> lst = (ArrayList<byte[]>)obj;
		for(byte[] bytes : lst) {
			markets.add(new String(bytes,"UTF-8"));
		}

	}
}
