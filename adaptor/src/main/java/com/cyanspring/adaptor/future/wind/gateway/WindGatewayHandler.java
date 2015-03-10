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

import cn.com.wind.td.tdf.TDF_CODE;
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
	private static final ArrayList<String> arrGlobalSubsSym = new ArrayList<String>();  
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);
	
	static public boolean isRegisteredByClient(String symbol)
	{
		synchronized(arrGlobalSubsSym)
		{
			if(Collections.binarySearch(arrGlobalSubsSym,symbol) < 0)
			{
				return false;
			}
		}		
		return true;
	}
	
	public static String addHashTail(String str)
	{
		return str + "|Hash=" + str.hashCode() + "\r\n";
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public void publishWindData(String str,String symbol)
	{
		str = addHashTail(str);
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
			String strMarket = null;
			if (msg != null) {
				boolean clientHeartBeat = false;
				String[] in_arr = msg.split("\\|");
				for (String str : in_arr) {
					if (str.startsWith("API=")) {
						strDataType = str.substring(4);
						if(strDataType.equals("ClientHeartBeat"))
						{
							clientHeartBeat = true;
						}
					}
					if (str.startsWith("Hash=")) {
						strHash = str.substring(5);
					}
					if(str.startsWith("Symbol="))
					{
						symbols = str.substring(7);
					}				
					if(str.startsWith("Market="))
					{
						strMarket = str.substring(7);
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
								// 先加到  Global Register Symbol
								int pos;
								synchronized(arrGlobalSubsSym)
								{
									pos = Collections.binarySearch(arrGlobalSubsSym, str);
									if(pos < 0)
									{
										arrGlobalSubsSym.add(~pos,str);
									}
								}
								
								pos = Collections.binarySearch(lst, str);
								if(pos >= 0)
								{
									String logstr = "Re-subscribe , Send Snapshot : " + str + " , from : " + channel.remoteAddress();
									System.out.println(logstr);
									log.info(logstr);
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
						else if(strDataType.equals("GetMarkets"))
						{
							sendMarkets(channel);
						}
						else if(strDataType.equals("GetCodeTable"))
						{
							sendCodeTable(channel,strMarket);
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
    public static void sendMarkets(Channel channel)
    {
		StringBuilder markets = new StringBuilder("API=Markets|Markets=");
    	synchronized(WindGateway.mapCodeTable)
    	{
    		if(WindGateway.mapCodeTable.size() == 0)
    		{
    			return;
    		}
    	
    		Iterator<?> it = WindGateway.mapCodeTable.entrySet().iterator();			
    		while (it.hasNext()) {
    			@SuppressWarnings("rawtypes")
    			Map.Entry pairs = (Map.Entry)it.next();
    			String market = (String)pairs.getKey();
    			if(market == null || market == "")
    			{
    				continue;
    			}
    			markets.append(market + ",");
    		}
    	}
    	channel.writeAndFlush(addHashTail(markets.toString()));
    }
    
    public static void sendCodeTable(Channel channel,String market)
    {
    	if(market == null)
    	{
			String logstr = "Missing Market while request Code Table : from " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr);    		
    	}
    	ArrayList<TDF_CODE> lst = WindGateway.mapCodeTable.get(market);
    	if(lst == null || lst.size() == 0)
    	{
			String logstr = "No symbol at market : " + market + " , request from : " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr);    		
    	}
    	synchronized(lst)
    	{
    		String strCode;
    		int i = 0;
    		for(TDF_CODE code : lst)
    		{
    			i += 1;
    			strCode = tdfCodeToString(code);
    			if(i == lst.size())
    			{
    				strCode = strCode + "|Ser=-" + i;    						
    			}
    			else
    			{
    				strCode = strCode + "|Ser=" + i;
    			}
    			channel.writeAndFlush(addHashTail(strCode));
    		}
    	}    	
    }
    
    public static String tdfCodeToString(TDF_CODE code)
    {
    	StringBuilder sb = new StringBuilder("API=CODE|Symbol=" + code.getWindCode());
    	sb.append("|OrgSymbol=" + code.getCode());
    	sb.append("|CNName=" + code.getCNName());
    	sb.append("|ENName=" + code.getENName());
    	sb.append("|Market=" + code.getMarket());
    	sb.append("|Type=" + code.getType());
    	return sb.toString();
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
