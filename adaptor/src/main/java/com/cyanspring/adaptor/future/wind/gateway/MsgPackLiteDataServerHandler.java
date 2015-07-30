package com.cyanspring.adaptor.future.wind.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.Network.Transport.MsgPackException;

import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_INDEX_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_TRANSACTION;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MsgPackLiteDataServerHandler extends ChannelInboundHandlerAdapter {
	private static final ConcurrentHashMap<Channel,Registration> channels = new ConcurrentHashMap<Channel,Registration>();
	public static final Registration registrationGlobal = new Registration();
	public static final int maxMsgPackCount = 128;

	private static final Logger log = LoggerFactory.getLogger(MsgPackLiteDataServerHandler.class);
	
	static public void resubscribe(Channel channel) {
		String strSubscribe = registrationGlobal.getSubscribeMarket();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}
		strSubscribe = registrationGlobal.getSubscribeSymbol();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}
		strSubscribe = registrationGlobal.getSubscribeTransaction();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}
		strSubscribe = registrationGlobal.getSubscribeSymbolMF();
		if(strSubscribe != null) {
			channel.write(addHashTail(strSubscribe,true));
		}			
	}

	public static String addHashTail(String str,boolean bAddHash)
	{
		if(bAddHash) {
			return str + "|Hash=" + str.hashCode();
		}
		return str;
	}	

	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();			
		
		channels.put(incoming,new Registration());
		log.info("[MsgPack Server] - " + incoming.remoteAddress().toString() + " has joined! , Current Count : " + channels.size());
		sendMarkets(incoming);
	}
	
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.remove(ctx.channel());
		log.info("[MsgPack Server] - " + incoming.remoteAddress().toString() + " has removed , Current Count : " + channels.size());
	
	}	
	

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
        String in = null;
        if(msg instanceof String) {
        	in = (String)msg;
        } else if(msg instanceof byte[]){
        	in = new String((byte[])msg,"UTF-8");
        }
        try {
        		if(in != null) {
        			Channel channel = ctx.channel();
        			Registration lst = channels.get(channel);
        			if(lst == null) {        			
            			log.info("in : [" + in + "] , " + channel.remoteAddress().toString()); 
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
	
	static public boolean isRegisteredByClient(String symbol) {	
		if(channels.size() == 0) {
			return false;
		}
		return registrationGlobal.hadSymbol(symbol);
	}	
	
	static public boolean isRegisteredTransactionByClient(String symbol) {	
		if(channels.size() == 0) {
			return false;
		}
		return registrationGlobal.hadTransaction(symbol);
	}		
		
    private static void subscribeSymbols(Channel channel , String symbols,Registration lst,boolean bTransaction,boolean isMF) {
		String[] sym_arr = symbols.split(";");
		HashMap<Integer, Object> map;
		int cnt = 0;
		StringBuilder subscribeSF = null , subscribeMF = null;
		for(String str : sym_arr)
		{
			if(WindGateway.mpCascading) {
				map = MsgPackLiteDataClientHandler.mapQuotation.get(str);
				if(map == null) {
					if(bTransaction == false) { // 如果是要求逐筆成交,就透過 SubsTrans 來訂閱,就同時會收到 quote 與 transaction
						if(isMF) {
							if(subscribeMF == null) {
								subscribeMF = new StringBuilder("API=SUBSCRIBEMF|Symbol=" + str);
							} else {
								subscribeMF.append(";" + str);
							}							
						} else {
							if(subscribeSF == null) {
								subscribeSF = new StringBuilder("API=SUBSCRIBE|Symbol=" + str);
							} else {
								subscribeSF.append(";" + str);
							}																
						}
						log.debug("Sysmbol not found! : " + str + " , subscription from : " + channel.remoteAddress().toString()); 
					}					
				}
			} else {
    			map = getMarketData(str);
    			if(map == null)
    			{
    				map = getFutureData(str);
    				if(map == null)
    				{
    					map = getIndexData(str);    			
    				}    				
    			}
    			if(map == null) {
					if(isMF) {
						WindGateway.instance.requestSymbolMF(str);
					} else {
						WindGateway.instance.requestSymbol(str);
					}
					log.debug("Sysmbol not found! : " + str + " , subscription from : " + channel.remoteAddress().toString());    				
    			}
			}								
						
			if(bTransaction == false) {
				cnt = lst.addMsgPack(map);
				if(cnt >= maxMsgPackCount) {
					channel.writeAndFlush(lst.flushMsgPack());
				}
			}
			// 先加到  Global Register Symbol
			registrationGlobal.addSymbol(str);								
			// 加到 Client 的 Registration
			if(lst.addSymbol(str) == false) {								
				log.debug("Re-subscribe , Send Snapshot : " + str + " , from : " + channel.remoteAddress().toString());
			}					
		}    	
		if(lst.MsgPackArrayCount() > 0) {
			channel.writeAndFlush(lst.flushMsgPack());
		}
		
		if(subscribeSF != null) {
			MsgPackLiteDataClientHandler.sendRequest(addHashTail(subscribeSF.toString(),true));
		}		
		if(subscribeMF != null) {
			MsgPackLiteDataClientHandler.sendRequest(addHashTail(subscribeMF.toString(),true));
		}		
    }
    
    private static void subscribeTransactions(Channel channel , String symbols,Registration lst) {
		String[] sym_arr = symbols.split(";");
		HashMap<Integer, Object> map;
		for(String str : sym_arr)
		{
	    	if(WindGateway.mpCascading) {
	    		map =  MsgPackLiteDataClientHandler.mapTransaction.get(str);
	    	} else {			
	    		map = getTransaction(str);
	    	}
			if(map == null) {
				if(WindGateway.mpCascading) { //) && registrationGlobal.hadTransaction(str) == false) {
					MsgPackLiteDataClientHandler.sendRequest(addHashTail("API=SubsTrans|Symbol=" + str,true));	
				}			
			} else {
				if(lst.addMsgPack(map) >= maxMsgPackCount) {
					channel.writeAndFlush(lst.flushMsgPack());
				}				
			}
			
			// 先加到  Global Register Symbol
			registrationGlobal.addTransaction(str);								
			// 加到 Client 的 Registration
			if(lst.addTransaction(str) == false) { // 已存在,重複訂閱.								
				log.info("Re-subscribe transaction , " + ((map == null) ? "Subscribe to server : " : "Send Snapshot : ") + str + " , from : " + channel.remoteAddress().toString());
			}					
		}   
		if(lst.MsgPackArrayCount() > 0) {
			channel.writeAndFlush(lst.flushMsgPack());
		}		
    }    
    
    private static void unsubscribeTransactions(Channel channel , String symbols,Registration lst) {
		String[] sym_arr = symbols.split(";");
		boolean canRemove = true;
		for(String symbol : sym_arr)
		{
			lst.removeTransaction(symbol);
			log.info("Unsubscribe Transaction : " + symbol + " , from : " + channel.remoteAddress().toString());
			// 檢查看看還有沒有人訂閱這個 Symbol 的 Transaction , 都沒人訂閱才從 list 裡面刪除
			Collection<Registration> regs = channels.values();
			for(Registration reg : regs) {
				if(reg == lst)
				{
					continue;
				}
				if(reg.hadTransaction(symbol)) {
					canRemove = false;
				}				
			}					
			if(canRemove) {
				registrationGlobal.removeTransaction(symbol);
				log.info("Remove transaction from registrationGlobal : " + symbol);
				if(WindGateway.mpCascading) { 
					MsgPackLiteDataClientHandler.sendRequest(addHashTail("API=UnSubsTrans|Symbol=" + symbol,true));
					if(MsgPackLiteDataClientHandler.mapTransaction.containsKey(symbol)) {
						MsgPackLiteDataClientHandler.mapTransaction.remove(symbol);
					}
				}					
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
			if(weDontHave && WindGateway.mpCascading) {				
				MsgPackLiteDataClientHandler.sendRequest(addHashTail("API=SUBSCRIBE|Market=" + market,true));			
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
        			//System.out.println(strlog);
        			log.debug(strlog);					
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
						if ((strDataType.equals("SUBSCRIBE") || strDataType.equals("SubsTrans"))) {						
							if(symbols != null) {
								subscribeSymbols(channel,symbols,lst,strDataType.equals("SubsTrans"),false);
							}
							if(strMarket != null) {
								subscribeMarkets(channel,strMarket,lst);
							}
							if(strDataType.equals("SubsTrans") && symbols != null) {
								subscribeTransactions(channel,symbols,lst);
							}								
						} else if (strDataType.equals("SUBSCRIBEMF")) {
							if(symbols != null) {
								subscribeSymbols(channel,symbols,lst,false,true);
							}
							if(strMarket != null) {
								subscribeMarkets(channel,strMarket,lst);
							}														
						}
						else if(strDataType.equals("ClearSubscribe")) {						
							lst.clear();
							rearrangeRegistration();
							log.info("Clear Subscribe from : " + channel.remoteAddress().toString());							
						}	else if(strDataType.equals("GetMarkets")) {						
							if(WindGateway.mpCascading) {
								MsgPackLiteDataClientHandler.sendRequest(msg);
							} else {
								sendMarkets(channel);
							}
						}	else if(strDataType.equals("GetCodeTable")) {						
							if(WindGateway.mpCascading) {
								MsgPackLiteDataClientHandler.sendRequest(msg);
							} else {
								sendCodeTable(channel,strMarket);
							}															
						}	else if(strDataType.equals("UnSubsTrans")) {
							unsubscribeTransactions(channel,symbols,lst);
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
    	ArrayList<String>lst = new ArrayList<String>();
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
    			lst.add(market);
    		}
    	}
    	if(lst.size() == 0) {
    		lst = null;
    		return;
    	}
    	HashMap<Integer,Object> map = new HashMap<Integer,Object>();
    	map.put(FDTFields.PacketType,FDTFields.WindMarkets);
    	map.put(FDTFields.ArrayOfString,lst);
    	channel.writeAndFlush(map);
    }
    
    public static void sendPacketArray(Channel channel,ArrayList<HashMap<Integer,Object>>packetArray) {
		HashMap<Integer,Object> map = new HashMap<Integer,Object>();
		map.put(FDTFields.PacketType,FDTFields.PacketArray);
		map.put(FDTFields.ArrayOfPacket,packetArray);
		channel.writeAndFlush(map);    	
    	
    }
    
    public static HashMap<Integer,Object> codeToMap(TDF_CODE code) {
    	HashMap<Integer,Object> map = new HashMap<Integer,Object>();
    	map.put(FDTFields.PacketType, FDTFields.WindCodeTable);
    	map.put(FDTFields.WindSymbolCode,code.getWindCode());
    	if(code.getCode() != null && code.getCode().isEmpty() == false) {
    		map.put(FDTFields.ShortName, code.getCode());
    	}
    	if(code.getMarket() != null && code.getMarket().isEmpty() == false) {
    		map.put(FDTFields.SecurityExchange, code.getMarket());
    	}
    	if(code.getCNName() != null && code.getCNName().isEmpty() == false) {
    		map.put(FDTFields.CNName, code.getCNName());
    	}
    	if(code.getENName() != null && code.getENName().isEmpty() == false) {
    		map.put(FDTFields.EnglishName, code.getENName());
    	}
    	map.put(FDTFields.SecurityType, code.getType());
    	return map;
    }
    
    public static void sendCodeTable(Channel channel,String market)
    {
    	if(market == null) {    	
			String logstr = "Missing Market while request Code Table : from " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr); 
			return;
    	}
    	ConcurrentHashMap<String,TDF_CODE> lst = WindGateway.mapCodeTable.get(market);
    	if(lst == null || lst.size() == 0) {    	
			String logstr = "No symbol at market : " + market + " , request from : " + channel.remoteAddress();
			System.out.println(logstr);
			log.warn(logstr);  
			return;
    	}

    		ArrayList<HashMap<Integer,Object>>packetArray = new ArrayList<HashMap<Integer,Object>>();
    		int i = 0;
    		TDF_CODE code;		
    		HashMap<Integer,Object> map;
    	    @SuppressWarnings("rawtypes")
			Iterator it = lst.entrySet().iterator();
    	    while (it.hasNext()) {
    			i += 1;
    	        @SuppressWarnings("rawtypes")
				Map.Entry pair = (Map.Entry)it.next();
    	        code = (TDF_CODE) pair.getValue();
    	        map = codeToMap(code);
    	        map.put(FDTFields.SerialNumber, it.hasNext() ? i : -i);
    			packetArray.add(map);
    			if(i % maxMsgPackCount == 0) {
    				sendPacketArray(channel,packetArray);
    				packetArray.clear();
    			}    	   
    		}
    		if(packetArray.size() > 0) {
				sendPacketArray(channel,packetArray);
				packetArray.clear();
    		}
  	
    }
    
    public static HashMap<Integer,Object> getTransaction(String symbol) {
    	TDF_TRANSACTION data = WindGateway.mapTransaction.get(symbol);
    	if(data == null) {
    		return null;
    	}
    	return WindGateway.publishTransactionChangesToMap(null, data);
    }
    
    public static HashMap<Integer, Object> getMarketData(String symbol) {
    	TDF_MARKET_DATA data = WindGateway.mapMarketData.get(symbol);
    	if(data == null) {    	
    		return null;
    	}
		return WindGateway.publishMarketDataChangesToMap(null, data);    	
    }    
    public static boolean sendMarketData(Channel channel,String symbol) {    
		HashMap<Integer, Object> map = getMarketData(symbol);
		if(map == null) {
			return false;
		}
		channel.writeAndFlush(map);
		return true;
    }
           
    public static HashMap<Integer, Object> getFutureData(String symbol) {    
    	TDF_FUTURE_DATA data = WindGateway.mapFutureData.get(symbol.toUpperCase()); // 因為 DCE SHF 註冊的時候要用小寫商品代碼,但是 Wind 回復大寫商品代碼
    	if(data == null) {    	
    		return null;
    	}
    	return WindGateway.publishFutureChangesToMap(null, data);
    }
    public static boolean sendFutureData(Channel channel,String symbol) {    
    	HashMap<Integer, Object> map = getFutureData(symbol);
    	if(map == null) {
    		return false;
    	}
		channel.writeAndFlush(map);
		return true;
    }
       
    public static HashMap<Integer, Object> getIndexData(String symbol) {    
    	TDF_INDEX_DATA data = WindGateway.mapIndexData.get(symbol);   
    	if(data == null) {    	
    		return null;
    	}
    	return WindGateway.publishIndexDataChangesToMap(null, data);
    }
    public static boolean sendIndexData(Channel channel,String symbol) {    
    	HashMap<Integer, Object> map = getIndexData(symbol);
    	if(map == null) {
    		return false;
    	}
		channel.writeAndFlush(map);
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
						channel.write(WindGateway.publishMarketDataChangesToMap(null, data));
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
						channel.write(WindGateway.publishFutureChangesToMap(null, data));
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
						channel.write(WindGateway.publishIndexDataChangesToMap(null, data));
					}
				}		
			}		
    	} catch(NoSuchElementException e) {
    		log.warn(e.getMessage() + " at mapIndexData with market : " + market + " , client : " + channel.remoteAddress().toString());
    	}    	
    	channel.flush();
    }
       
    
    public static void sendQuotationDateChange(String market,int oldDate,int newDate) {
    	if(channels.size() == 0) {
    		return;
    	}    	
    	HashMap<Integer, Object> map = new HashMap<Integer, Object>();
    	map.put(FDTFields.PacketType,FDTFields.WindQuotationDateChange);
    	map.put(FDTFields.SecurityExchange, market);
    	map.put(FDTFields.LastTradingDay,oldDate);
    	map.put(FDTFields.TradingDay,newDate);
    	sendMessagePackToAllClient(map);
    }
    
    public static void sendMarketClose(String market,int time,String info) {
    	if(channels.size() == 0) {
    		return;
    	}
    	HashMap<Integer, Object> map = new HashMap<Integer, Object>();
    	map.put(FDTFields.PacketType, FDTFields.WindMarketClose);
    	map.put(FDTFields.SecurityExchange, market);
    	map.put(FDTFields.Time,time);
    	map.put(FDTFields.Information,info);
    	sendMessagePackToAllClient(map);
    }
    
    private static HashMap<Integer, Object> heartbeatMessagePack(int heartbeatCounter) {
    	HashMap<Integer, Object> map = new HashMap<Integer, Object>();
    	map.put(FDTFields.PacketType, FDTFields.Heartbeat);
    	map.put(FDTFields.SerialNumber,heartbeatCounter);
    	return map;
    }
    
    public static void sendHeartbeat(Channel channel,int heartbeatCounter) {
    	channel.writeAndFlush(heartbeatMessagePack(heartbeatCounter));
    }
    
    public static void sendMessagePackToAllClient(HashMap<Integer, Object> map) {
    	for(Channel channel : channels.keySet()) {    	
    		channel.writeAndFlush(map);
    	}
    }
    
    public static void sendMssagePackToAllClientByRegistration(HashMap<Integer, Object> map,String symbol) {
		Iterator<?> it = channels.entrySet().iterator();
		int cnt;
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();			
			Registration lst = (Registration)pairs.getValue();
			if(lst == null) {					
				continue;
			}
			if(lst.hadSymbol(symbol)) {
				cnt = lst.addMsgPack(map);
				if(cnt >= maxMsgPackCount) {
					((Channel)pairs.getKey()).writeAndFlush(lst.flushMsgPack());
				}
			}
		}		    
    }
    
    public static void sendMssagePackToAllClientByRegistrationTransaction(HashMap<Integer, Object> map,String symbol) {
		Iterator<?> it = channels.entrySet().iterator();
		int cnt;
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();			
			Registration lst = (Registration)pairs.getValue();
			if(lst == null) {					
				continue;
			}
			if(lst.hadTransaction(symbol)) {
				cnt = lst.addMsgPack(map);
				if(cnt >= maxMsgPackCount) {
					((Channel)pairs.getKey()).writeAndFlush(lst.flushMsgPack());
				}
			}
		}		    
    }    
    
    public static void flushAllClientMsgPack() {
		Iterator<?> it = channels.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();			
			Registration lst = (Registration)pairs.getValue();
			if(lst == null) {					
				continue;
			}
			if(lst.MsgPackArrayCount() > 0) {
				((Channel)pairs.getKey()).writeAndFlush(lst.flushMsgPack());
			}
		}		      	
    }
    
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
			throws Exception {    	
		log.info(e.getMessage() + " , from : " + ctx.channel().remoteAddress().toString(),e);
		if(e instanceof MsgPackException) {
			return;
		}
		ctx.close();
	}

}
