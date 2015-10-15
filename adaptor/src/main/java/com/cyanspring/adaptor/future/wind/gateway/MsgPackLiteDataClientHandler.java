package com.cyanspring.adaptor.future.wind.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.network.transport.FDTFields;
import com.cyanspring.network.transport.FDTFrameDecoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgPackLiteDataClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(MsgPackLiteDataClientHandler.class);
	public static ChannelHandlerContext ctx = null;
	
	private int bufLenMin = 0,bufLenMax = 0,blockCount = 0;
	private long throughput = 0,msLastTime = 0,msDiff = 0;
	
	public static ConcurrentHashMap<String,HashMap<Integer,Object>> mapQuotation = new ConcurrentHashMap<String,HashMap<Integer,Object>>();
	public static ConcurrentHashMap<String,HashMap<Integer,Object>> mapTransaction = new ConcurrentHashMap<String,HashMap<Integer,Object>>();
	public static ConcurrentHashMap<String,CascadingCodeTable> mapCascadingCodeTable = new ConcurrentHashMap<String,CascadingCodeTable>();
	
	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		
		msLastTime = System.currentTimeMillis();
		ctx = arg0;
		//ctx.channel().write(MsgPackLiteDataServerHandler.addHashTail("API=ReqHeartBeat",true));
		//ctx.channel().write(MsgPackLiteDataServerHandler.addHashTail("API=GetMarkets",true));
		mapQuotation.clear();
		mapTransaction.clear();		
		MsgPackLiteDataServerHandler.resubscribe(ctx.channel());
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
		
		/*
		try {
			if(arg1 instanceof HashMap<?,?>) {
				@SuppressWarnings("unchecked")
				HashMap<Integer,Object> in = (HashMap<Integer,Object>)arg1;
				if(in != null) {
					processData(in);
					MsgPackLiteDataServerHandler.flushAllClientMsgPack();
					if(calculateMessageFlow(FDTFrameDecoder.getPacketLen(),FDTFrameDecoder.getReceivedBytes(),FDTFrameDecoder.getDropBytes()))
					{
						FDTFrameDecoder.ResetCounter();
					}
				}
			} 
	    } finally {
	        ReferenceCountUtil.release(arg1);
	    }
	   	*/
		WindGateway.instance.AddMessage(arg1);
		if(calculateMessageFlow(FDTFrameDecoder.getPacketLen(),FDTFrameDecoder.getReceivedBytes(),FDTFrameDecoder.getDropBytes()))
		{
			FDTFrameDecoder.ResetCounter();
		}		
	
	}
	
	private boolean calculateMessageFlow(int rBytes,int dataReceived,int dropBytes) {
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
			
		blockCount += 1;
		msDiff = System.currentTimeMillis() - msLastTime;
		if(msDiff > 1000) {
			msLastTime = System.currentTimeMillis();
			if(throughput < dataReceived * 1000 / msDiff) {
				throughput = dataReceived * 1000 / msDiff;
				if(throughput > 1024) {
					log.info("maximal throughput : " + throughput / 1024 + " KB/Sec , " + blockCount + " packets/Sec , Drop " + dropBytes + " Bytes." );
				} else {
					log.info("maximal throughput : " + throughput + " Bytes/Sec , " + blockCount + " packets/Sec , Drop " + dropBytes + " Bytes." );
				}
			}			
			blockCount = 0;
			return true;
		}
		return false;
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
	
	
	public static void processData(HashMap<Integer,Object> in , boolean inArray) {
		
		try {
			// 沒有 packet type , 就無法處理.
			if(in.containsKey(FDTFields.PacketType) == false) {
				return;
			}				

			int iPacketType = ((Number)in.get(FDTFields.PacketType)).intValue();
			

			switch(iPacketType) {
			case FDTFields.PacketArray :
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<Integer,Object>> lst = (ArrayList<HashMap<Integer,Object>>)in.get(FDTFields.ArrayOfPacket);
				if(lst != null) {
					for(HashMap<Integer,Object> map : lst) {
						processData(map,true);
					}					
				}
				break;
			case FDTFields.WindMarkets :
				if(in.containsKey(FDTFields.ArrayOfString)) {
					processMarkets(in.get(FDTFields.ArrayOfString));
					MsgPackLiteDataServerHandler.sendMessagePackToAllClient(in);
				}
				break;
			case FDTFields.WindTransaction :
				if(in.containsKey(FDTFields.WindSymbolCode)) {
					String symbol = new String((byte[])in.get(FDTFields.WindSymbolCode),"UTF-8");
					MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistrationTransaction(in, symbol);
					HashMap<Integer,Object> mp = mapTransaction.get(symbol);
					if(mp == null) {
						mapTransaction.put(symbol, in);
					} else {
						mp.putAll(in);
					}
				}
				break;
			case FDTFields.WindIndexData :
			case FDTFields.WindMarketData :			
			case FDTFields.WindFutureData :
				if(in.containsKey(FDTFields.WindSymbolCode)) {					
					String symbol = new String((byte[])in.get(FDTFields.WindSymbolCode),"UTF-8");					
					MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(in, symbol,inArray);					
					HashMap<Integer,Object> mp = mapQuotation.get(symbol);
					if(mp == null) {
						mapQuotation.put(symbol, in);
					} else {
						mp.putAll(in);
					}					
				}
				break;
			case FDTFields.SnapShotEnds :
			case FDTFields.WindConnected :
			case FDTFields.WindHeartBeat :
			case FDTFields.WindMarketClose :
			case FDTFields.WindQuotationDateChange :
				if(inArray) {
					MsgPackLiteDataServerHandler.sendArrayMessagePackToAllClient(in);
				} else {
					MsgPackLiteDataServerHandler.sendMessagePackToAllClient(in);
				}
				break;
			case FDTFields.WindCodeTable :
				processCodeTable(in);
				break;				
			case FDTFields.WindCodeTableResult :
				processCodeTableResult(in);				
				break;			
			}
		}
		catch(Exception e) {
			log.warn(e.getMessage(),e);	
		}
	}	
	
	public static void processMarkets(Object obj) throws Exception 
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
		WindGateway.instance.convertMarketsMP(markets);
	}
	
	public static void processCodeTable(HashMap<Integer,Object> in) {
		try {			
			String symbol = new String((byte[])in.get(FDTFields.WindSymbolCode),"UTF-8");
			String market = new String((byte[])in.get(FDTFields.SecurityExchange),"UTF-8");
			int ser = ((Number)in.get(FDTFields.SerialNumber)).intValue();
			CascadingCodeTable cct = mapCascadingCodeTable.get(market);
			if(cct != null) {
				cct.mapMPCode.put(symbol,in);
			} else {
				log.warn("Missing Cascading Code Table , Market : " + market);
				return;
			}
			if(ser < 0 && cct.mpCodeTableResult != null) {
				log.info("Received Code Table : " + market + " , count " + cct.mapMPCode.size() + " , last serial : " + ser);
				MsgPackLiteDataServerHandler.sendMessagePackToAllClient(cct.mpCodeTableResult);
			}
			
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage(),e);
		}
	}
	public static void processCodeTableResult(HashMap<Integer,Object> in) {
		String market = null;
		CascadingCodeTable cct =  null;
		long codesHashCode = 0;
		int dataCount = 0;
		try {
			market = new String((byte[])in.get(FDTFields.SecurityExchange),"UTF-8");
			codesHashCode = ((Number)in.get(FDTFields.HashCode)).longValue();			
			cct = mapCascadingCodeTable.get(market);					
			if(cct != null) {				
				dataCount = ((Number)in.get(FDTFields.DataCount)).intValue();
				if(cct.codesHashCode == codesHashCode && cct.mapMPCode.size() == dataCount) {
					log.info("Code Table No Change , Market : " + market);
					return;
				}
			}
			if(cct == null) {
				cct = new CascadingCodeTable();				
			}
			cct.mapMPCode.clear();
			cct.codesHashCode = codesHashCode;
			cct.mpCodeTableResult = in;
			cct.strMarket = market;
			cct.DataCount = dataCount;
			mapCascadingCodeTable.put(market, cct);
			
			if(ctx != null && market != null) {
				ctx.writeAndFlush(MsgPackLiteDataServerHandler.addHashTail("API=GetCodeTable|Market=" + market,true));
				log.info("Request Code Table , Market : " + market);
			}			
		} catch (Exception e) {
			log.warn("Exception : " + e.getMessage(),e);
		}	
	}
	public static ConcurrentHashMap<String, HashMap<Integer,Object>>getMPCodeByMarket(String market) {
		CascadingCodeTable cct = mapCascadingCodeTable.get(market);
		if(cct != null) {
			return cct.mapMPCode;
		}
		return null;
	}
}

class CascadingCodeTable {
	String strMarket = "";
	int CodeDate = 0;
	int DataCount = 0;
	long codesHashCode = 0;
	public HashMap<Integer,Object> mpCodeTableResult = null;
	public ConcurrentHashMap<String, HashMap<Integer,Object>> mapMPCode = new ConcurrentHashMap<String,HashMap<Integer,Object>>();

}
