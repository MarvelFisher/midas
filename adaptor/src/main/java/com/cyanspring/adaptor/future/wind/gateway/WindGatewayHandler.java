package com.cyanspring.adaptor.future.wind.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_INDEX_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

public class WindGatewayHandler extends ChannelInboundHandlerAdapter {
	
	//private static final ChannelGroup channels = new DefaultChannelGroup(null);
	private static final Hashtable<Channel,Registration> channels = new Hashtable<Channel,Registration>();
	private static final Registration registrationGlobal = new Registration();  
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);
	
	static public void resubscribe(Channel channel) {
		String strSubscribe = registrationGlobal.getSubscribeMarket();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}
		strSubscribe = registrationGlobal.getSubscribeSymbol();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}
	}
	
	static public boolean isRegisteredByClient(String symbol)
	{	
		return registrationGlobal.hadSymbol(symbol);
	}
	
	public static String addHashTail(String str,boolean bAddHash)
	{
		if(bAddHash) {
			return str + "|Hash=" + str.hashCode() + "\r\n";
		}
		return str + "\r\n";
	}
	
	synchronized static public void publishWindData(String str,String symbol,boolean bAddHash) {	
		String outString = addHashTail(str,bAddHash);
		synchronized(channels) {		
			Iterator<?> it = channels.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				if(symbol != null) {				
					Registration lst = (Registration)pairs.getValue();
					if(lst == null || lst.hadSymbol(symbol) == false) {					
						continue;
					}
				}
				((Channel)pairs.getKey()).writeAndFlush(outString);
			}
		}		
	}

	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		
		channels.put(ctx.channel(),new Registration());
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
        			Registration lst = channels.get(channel);
        			if(lst == null) {        			
            			log.info("in : [" + in + "] , " + channel.remoteAddress().toString());        				;
        				log.error("channel not found : " + in);
        			}
        			else {        			
        				parseRequest(ctx,in,lst);// Add symbol to map;
        			}
        		}            
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }
    
    private static void subscribeSymbols(Channel channel , String symbols,Registration lst) {
		String[] sym_arr = symbols.split(";");
		for(String str : sym_arr)
		{
			if(sendMarketData(channel,str) == false)
			{
				if(sendFutureData(channel,str) == false)
				{
					if(sendIndexData(channel,str) == false)
					{	
						if(WindGateway.cascading) {
							WindDataClientHandler.sendRequest(addHashTail("API=SUBSCRIBE|Symbol=" + str,true));
						} else {
							log.error("Sysmbol not found! : " + str + " , subscription from : " + channel.remoteAddress().toString());
						}
					}
				}						
			}
			// 先加到  Global Register Symbol
			registrationGlobal.addSymbol(str);								
			// 加到 Client 的 Registration
			if(lst.addSymbol(str) == false) {								
				log.info("Re-subscribe , Send Snapshot : " + str + " , from : " + channel.remoteAddress().toString());
			}					
		}    	
    }
    
    
    private static void subscribeMarkets(Channel channel , String markets, Registration lst) {
		String[] market_arr = markets.split(";");
		boolean weDontHave,newlyAdded;
		for(String market : market_arr) {
			newlyAdded = lst.addMarket(market);
			weDontHave = registrationGlobal.addMarket(market);
			log.info((newlyAdded ? "Subscribe Makret : " : "Re-subscribe Market : " ) + market + " , from " + channel.remoteAddress().toString());
			if(weDontHave && WindGateway.cascading) {				
				WindDataClientHandler.sendRequest(addHashTail("API=SUBSCRIBE|Market=" + market,true));			
			} else {				
				sendDataByMarket(channel,market);				
			}
		}
    }
    
    private static void rearrangeRegistration() {
		synchronized(channels) {
			registrationGlobal.clear();
			Iterator<?> it = channels.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();			
				Registration lst = (Registration)pairs.getValue();
				if(lst == null) {					
					continue;
				}
				registrationGlobal.addRegistration(lst);
				
			}
		}
    }
    
    private static void parseRequest(ChannelHandlerContext ctx,String msg,Registration lst) {       
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
						if(strDataType.equals("ClientHeartBeat")) {						
							clientHeartBeat = true;
						}
					}
					if (str.startsWith("Hash=")) {
						strHash = str.substring(5);
					}
					if(str.startsWith("Symbol=")) {					
						symbols = str.substring(7);
					}				
					if(str.startsWith("Market=")) {					
						strMarket = str.substring(7);
					}
				}
				if(false == clientHeartBeat) {				
        			String strlog = "in : [" + msg + "] , " + channel.remoteAddress();
        			System.out.println(strlog);
        			log.info(strlog);					
				}
				int endindex = msg.indexOf("|Hash=");
				if (endindex > 0) {
					String tempStr = msg.substring(0, endindex);
					int hascode = tempStr.hashCode();

					// Compare hash code
					if (hascode != Integer.parseInt(strHash)) {					
						String logstr = "HashCode mismatch : " + msg + " , from : " + channel.remoteAddress();
						System.out.println(logstr);
						log.warn(logstr);
						return;
					}
					if(strDataType == null) {					
						String logstr = "missing API function : " + msg + " , from : " + channel.remoteAddress();
						System.out.println(logstr);
						log.warn(logstr);
						return;
					}	else	{
						if (strDataType.equals("SUBSCRIBE") && symbols != null) {						
							if(symbols != null) {
								subscribeSymbols(channel,symbols,lst);
							}
							if(strMarket != null) {
								subscribeMarkets(channel,strMarket,lst);
							}
						}	else if(strDataType.equals("ClearSubscribe")) {						
							lst.clear();
							rearrangeRegistration();
							log.info("Clear Subscribe from : " + channel.remoteAddress().toString());							
						}	else if(strDataType.equals("GetMarkets")) {						
							if(WindGateway.cascading) {
								WindDataClientHandler.sendRequest(msg);
							} else {
								sendMarkets(channel);
							}
						}	else if(strDataType.equals("GetCodeTable")) {						
							if(WindGateway.cascading) {
								WindDataClientHandler.sendRequest(msg);
							} else {
								sendCodeTable(channel,strMarket);
							}
						}	else if(strDataType.equals("ReqHeartBeat")) {						
							if(ctx.pipeline().get("idleHandler") == null) {							
								ctx.pipeline().addAfter("encoder", "idleStateHandler", new IdleStateHandler(25, 10, 0));
								ctx.pipeline().addBefore("handler", "idleHandler", new IdleHandler());
							}
							String logstr = "Request HeartBeat : " + channel.remoteAddress();						
							log.info(logstr);											
						}
					}	
				}	else	{
					String logstr = "Missing HashCode  : " + msg + " , from : " + channel.remoteAddress();					
					log.warn(logstr);
				}				
			}
		}	catch (Exception e) {

    		log.warn(e.getMessage(),e);
    	}	finally	{
 	
    	}
	}
    public static void sendMarkets(Channel channel)
    {
		StringBuilder markets = new StringBuilder("API=Markets|Markets=");
    	synchronized(WindGateway.mapCodeTable) {    	
    		if(WindGateway.mapCodeTable.size() == 0) {    		
    			return;
    		}
    	
    		Iterator<?> it = WindGateway.mapCodeTable.entrySet().iterator();			
    		while (it.hasNext()) {
    			@SuppressWarnings("rawtypes")
    			Map.Entry pairs = (Map.Entry)it.next();
    			String market = (String)pairs.getKey();
    			if(market == null || market == "") {    			
    				continue;
    			}
    			markets.append(market + ",");
    		}
    	}
    	channel.writeAndFlush(addHashTail(markets.toString(),true));
    }
    
    public static void sendCodeTable(Channel channel,String market)
    {
    	if(market == null) {    	
			String logstr = "Missing Market while request Code Table : from " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr);    		
    	}
    	ArrayList<TDF_CODE> lst = WindGateway.mapCodeTable.get(market);
    	if(lst == null || lst.size() == 0) {    	
			String logstr = "No symbol at market : " + market + " , request from : " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr);    		
    	}
    	synchronized(lst) {    	
    		String strCode;
    		int i = 0;
    		for(TDF_CODE code : lst) {    		
    			i += 1;
    			strCode = tdfCodeToString(code);
    			if(i == lst.size()) {    			
    				strCode = strCode + "|Ser=-" + i;    						
    			}	else	{
    				strCode = strCode + "|Ser=" + i;
    			}
    			channel.writeAndFlush(addHashTail(strCode,true));
    		}
    	}    	
    }
    
    public static String tdfCodeToString(TDF_CODE code) {    
    	StringBuilder sb = new StringBuilder("API=CODE|Symbol=" + code.getWindCode());
    	sb.append("|OrgSymbol=" + code.getCode());
    	sb.append("|CNName=" + code.getCNName());
    	sb.append("|ENName=" + code.getENName());
    	sb.append("|Market=" + code.getMarket());
    	sb.append("|Type=" + code.getType());
    	return sb.toString();
    }
    
    public static boolean sendMarketData(Channel channel,String symbol) {    
    	TDF_MARKET_DATA data = WindGateway.mapMarketData.get(symbol);
    	if(data == null) {    	
    		return false;
    	}
		String str = addHashTail(WindGateway.publishMarketDataChanges(null, data),true);
		channel.writeAndFlush(str);
		return true;
    }
    
    public static boolean sendFutureData(Channel channel,String symbol) {    
    	TDF_FUTURE_DATA data = WindGateway.mapFutureData.get(symbol);
    	if(data == null) {    	
    		return false;
    	}
		String str = addHashTail(WindGateway.publishFutureChanges(null, data),true);
		channel.writeAndFlush(str);
		return true;
    }
    public static boolean sendIndexData(Channel channel,String symbol) {    
    	TDF_INDEX_DATA data = WindGateway.mapIndexData.get(symbol);
    	if(data == null) {    	
    		return false;
    	}
		String str = addHashTail(WindGateway.publishIndexDataChanges(null, data),true);
		channel.writeAndFlush(str);
		return true;
    }
    
    public static void sendDataByMarket(Channel channel,String market) {
    	market = "." + market;
    	try {
			Iterator<?> it = WindGateway.mapMarketData.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				TDF_MARKET_DATA data = (TDF_MARKET_DATA)pairs.getValue();
				if(data != null) {
					if(data.getWindCode().contains(market)) {
						channel.write(addHashTail(WindGateway.publishMarketDataChanges(null, data),true));
					}
				}		
			}
    	} catch(NoSuchElementException e) {
    		log.warn(e.getMessage() + " at mapMarketData with market : " + market + " , client : " + channel.remoteAddress().toString());
    	}			
			
    	try {
    		Iterator<?> it = WindGateway.mapFutureData.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				TDF_FUTURE_DATA data = (TDF_FUTURE_DATA)pairs.getValue();
				if(data != null) {
					if(data.getWindCode().contains(market)) {
						channel.write(addHashTail(WindGateway.publishFutureChanges(null, data),true));
					}
				}		
			}
    	} catch(NoSuchElementException e) {
    		log.warn(e.getMessage() + " at mapFutureData with market : " + market + " , client : " + channel.remoteAddress().toString());
    	}				
			
    	try {
    		Iterator<?>it = WindGateway.mapIndexData.entrySet().iterator();			
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				TDF_INDEX_DATA data = (TDF_INDEX_DATA)pairs.getValue();
				if(data != null) {
					if(data.getWindCode().contains(market)) {
						channel.write(addHashTail(WindGateway.publishIndexDataChanges(null, data),true));
					}
				}		
			}		
    	} catch(NoSuchElementException e) {
    		log.warn(e.getMessage() + " at mapIndexData with market : " + market + " , client : " + channel.remoteAddress().toString());
    	}
    	
    	channel.flush();
    }
    
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
    	String logstr = "ExceptionCaught : " + ctx.channel().remoteAddress();
    	log.warn(logstr);
    	log.warn(cause.getMessage(),cause);
        cause.printStackTrace();
        ctx.close();
    }    
}
