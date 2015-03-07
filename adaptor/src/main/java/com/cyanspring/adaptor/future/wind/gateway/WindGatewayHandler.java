package com.cyanspring.adaptor.future.wind.gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.impl.Log4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.ib.IbAdaptor;

import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_INDEX_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

public class WindGatewayHandler extends ChannelInboundHandlerAdapter {
	
	//private static final ChannelGroup channels = new DefaultChannelGroup(null);
	private static final Hashtable<Channel,ArrayList<String>> channels = new Hashtable<Channel,ArrayList<String>>();
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);	
	
	public static String addHashTail(String str)
	{
		return str + "|Hash=" + str.hashCode() + "\r\n";
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public void publishWindData(String str,String symbol)
	{
		str = addHashTail(str);
		//for(Channel channel : channels)
		//{
		//	channel.writeAndFlush(str);
		//}
		synchronized(channels)
		{
			Iterator<?> it = channels.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				if(symbol != null)
				{
					ArrayList<String> lst = (ArrayList<String>)pairs.getValue();
					if(lst == null || Collections.binarySearch(lst,symbol) < 0)
					{
						continue;
					}
				}
				((Channel)pairs.getKey()).writeAndFlush(str);
			}
		}		
	}

	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		
		/*
		synchronized(WindGateway.mapFutureData)
		{
			Iterator<?> it = WindGateway.mapFutureData.entrySet().iterator();
			String str;
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				str = addHashTail(WindGateway.publishFutureChanges(null, (TDF_FUTURE_DATA)pairs.getValue()));
				incoming.writeAndFlush(str);
			}
		}
		synchronized(WindGateway.mapMarketData)
		{
			Iterator<?> it = WindGateway.mapMarketData.entrySet().iterator();
			String str;
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				str = addHashTail(WindGateway.publishMarketDataChanges(null, (TDF_MARKET_DATA)pairs.getValue()));
				incoming.writeAndFlush(str);
			}			
		}
		*/
		channels.put(ctx.channel(),new ArrayList<String>());
		String logstr = "[Server] - " + incoming.remoteAddress() + " has joined! , Current Count : " + channels.size();
		System.out.println(logstr);
		log.info(logstr);
	}
	
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.remove(ctx.channel());
		{
			String logstr = "[Server] - " + incoming.remoteAddress() + " has removed , Current Count : " + channels.size();
			System.out.println(logstr);
			log.info(logstr);
		}		
	}
			

    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // Discard the received data silently.
        String in = (String) msg;
        try {
        		if(in != null) {
        			//String strlog = "in : [" + in + "] , " + ctx.channel().remoteAddress();
        			//System.out.println(strlog);
        			//log.info(strlog);
        			Channel channel = ctx.channel();
        			ArrayList<String> lst = channels.get(channel);
        			if(lst == null)
        			{
            			String strlog = "in : [" + in + "] , " + channel.remoteAddress();
            			System.out.println(strlog);
            			log.info(strlog);        				
        				String logstr = "channel not found : " + in;
        				System.out.println(logstr);
        				log.error(logstr);
        			}
        			else
        			{
        				parseRequest(ctx,in,lst);// Add symbol to map;
        			}
        		}            
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }
    
    private void parseRequest(ChannelHandlerContext ctx,String msg,ArrayList<String> lst)
    {    	
    	Channel channel = ctx.channel();
    	try {
			String strHash = null;
			String strDataType = null;
			String symbols = null;
			if (msg != null) {
				boolean clientHeartBeat = false;
				String[] in_arr = msg.split("\\|");
				for (String str : in_arr) {
					if (str.contains("API=")) {
						strDataType = str.substring(4);
						if(strDataType.equals("ClientHeartBeat"))
						{
							clientHeartBeat = true;
						}
					}
					if (str.contains("Hash=")) {
						strHash = str.substring(5);
					}
					if(str.contains("Symbol="))
					{
						symbols = str.substring(7);
					}					
				}
				if(false == clientHeartBeat)
				{
        			String strlog = "in : [" + msg + "] , " + channel.remoteAddress();
        			System.out.println(strlog);
        			log.info(strlog);					
				}
				int endindex = msg.indexOf("|Hash=");
				if (endindex > 0) {
					String tempStr = msg.substring(0, endindex);
					int hascode = tempStr.hashCode();

					// Compare hash code
					if (hascode != Integer.parseInt(strHash)) 
					{
						String logstr = "HashCode mismatch : " + msg + " , from : " + channel.remoteAddress();
						System.out.println(logstr);
						log.warn(logstr);
						return;
					}
					if(strDataType == null)
					{
						String logstr = "missing API function : " + msg + " , from : " + channel.remoteAddress();
						System.out.println(logstr);
						log.warn(logstr);
						return;
					}
					else
					{
						if (strDataType.equals("SUBSCRIBE") && symbols != null)
						{
							String[] sym_arr = symbols.split(";");
							for(String str : sym_arr)
							{
								if(sendMarketData(channel,str) == false)
								{
									if(sendFutureData(channel,str) == false)
									{
										if(sendIndexData(channel,str) == false)
										{
											String logstr = "Sysmbol not found! : " + str + " , from : " + channel.remoteAddress();
											System.out.println(logstr);
											log.error(logstr);
										}
									}						
								}
								int pos = Collections.binarySearch(lst, str);
								if(pos >= 0)
								{
									String logstr = "Re-subscribe , Send Snapshot : " + str + " , from : " + channel.remoteAddress();
									System.out.println(logstr);
									log.warn(logstr);
									continue;
								}
								lst.add(~pos, str);						
							}
						}	
						else if(strDataType.equals("ClearSubscribe"))
						{
							lst.clear();
							String logstr = "Clear Subscribe from : " + channel.remoteAddress();
							System.out.println(logstr);
							log.info(logstr);							
						}
						else if(strDataType.equals("ReqHeartBeat"))
						{
							if(ctx.pipeline().get("idleHandler") == null)
							{
								ctx.pipeline().addAfter("encoder", "idleStateHandler", new IdleStateHandler(25, 10, 0));
								ctx.pipeline().addBefore("handler", "idleHandler", new IdleHandler());
							}
							String logstr = "Request HeartBeat : " + channel.remoteAddress();						
							log.info(logstr);
							System.out.println(logstr);						
						}
					}	
				}
				else
				{
					String logstr = "Missing HashCode  : " + msg + " , from : " + channel.remoteAddress();
					System.out.println(logstr);
					log.warn(logstr);
				}				
			}
		} 
    	catch (Exception e) {
    		e.printStackTrace();
    		log.warn("Exception during parseRequest : " + e.getMessage() );
    	}
    	finally
    	{
    		System.out.flush();    		
    	}
	}
    
    public static boolean sendMarketData(Channel channel,String symbol)
    {
    	TDF_MARKET_DATA data = WindGateway.mapMarketData.get(symbol);
    	if(data == null)
    	{
    		return false;
    	}
		String str = addHashTail(WindGateway.publishMarketDataChanges(null, data));
		channel.writeAndFlush(str);
		return true;
    }
    
    public static boolean sendFutureData(Channel channel,String symbol)
    {
    	TDF_FUTURE_DATA data = WindGateway.mapFutureData.get(symbol);
    	if(data == null)
    	{
    		return false;
    	}
		String str = addHashTail(WindGateway.publishFutureChanges(null, data));
		channel.writeAndFlush(str);
		return true;
    }
    public static boolean sendIndexData(Channel channel,String symbol)
    {
    	TDF_INDEX_DATA data = WindGateway.mapIndexData.get(symbol);
    	if(data == null)
    	{
    		return false;
    	}
		String str = addHashTail(WindGateway.publishIndexDataChanges(null, data));
		channel.writeAndFlush(str);
		return true;
    }    
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
    	String logstr = "ExceptionCaught : " + ctx.channel().remoteAddress();
    	log.warn(logstr);
    	//log.warn(cause.getMessage());
    	log.warn("NetIO",cause);
    	System.out.print(logstr);
        cause.printStackTrace();
        ctx.close();
    }    
}
