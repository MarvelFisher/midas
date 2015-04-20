package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_INDEX_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_MSG_ID;
import cn.com.wind.td.tdf.TDF_QUOTATIONDATE_CHANGE;

import com.cyanspring.adaptor.future.wind.WindFutureDataAdaptor;
import com.cyanspring.id.Library.Util.LogUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class WindDataClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(WindDataClientIdleHandler.class);
	public static ChannelHandlerContext ctx = null;
	
	private int bufLenMin = 0,bufLenMax = 0,blockCount = 0;
	private long throughput = 0,dataReceived = 0,msLastTime = 0,msDiff = 0;	

	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		
		msLastTime = System.currentTimeMillis();
		ctx = arg0;
		ctx.channel().write(WindGatewayHandler.addHashTail("API=ReqHeartBeat"));
		WindGatewayHandler.resubscribe(ctx.channel());
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
		String in = (String)arg1;
		try {
			if(in != null) {
				processData(in);
				calculateMessageFlow(in.length());					
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
	
	private void processData(String in) {
		int dataType;
		String strDataType = null,strHash = null;
		String[] in_arr = in.split("\\|");
		for (String str : in_arr) {
			if (str.contains("API=")) {
				strDataType = str.substring(4);
			}
			if (str.contains("Hash=")) {
				strHash = str.substring(5);
			}			
		}
		int endindex = in.indexOf("|Hash=");
		if (endindex > 0) {
			String tempStr = in.substring(0, endindex);
			int hascode = tempStr.hashCode();

			// Compare hash code
			if (hascode == Integer.parseInt(strHash)) {
				if (strDataType.equals("DATA_FUTURE")) {
					WindGateway.instance.receiveFutureData(convertFutureData(in_arr,in));					
				}
				if (strDataType.equals("DATA_MARKET")) {
					WindGateway.instance.receiveMarketData(convertMarketData(in_arr,in));
				}
				if (strDataType.equals("DATA_INDEX")) {
					WindGateway.instance.receiveIndexData(convertIndexData(in_arr,in));
				}
				if (strDataType.equals("Heart Beat")) {
					WindGateway.instance.publishWindData(in, null);
				}
				if (strDataType.equals("QDateChange")) {
					WindGateway.instance.publishWindData(in, null);
				}
				if (strDataType.equals("MarketClose")) {
					WindGateway.instance.publishWindData(in, null);
				}
				if (strDataType.equals("CODE")) {
					WindGateway.instance.publishWindData(in, null);
				}
				if (strDataType.equals("Markets")) {
					WindGateway.instance.publishWindData(in, null);					
				}
			}
		}		
	}
	public static long[] parseStringTolong(String[] str_arr) {
		long[] long_arr = new long[str_arr.length];
		for (int i = 0; i < str_arr.length; i++) {
			long_arr[i] = Long.parseLong(str_arr[i]);
		}
		return long_arr;
	}	
	
	private TDF_FUTURE_DATA convertFutureData(String[] in_arr,String in) {
		TDF_FUTURE_DATA future = new TDF_FUTURE_DATA();
		String key = null;
		String value = null;
		String[] kv_arr = null;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							future.setWindCode(value);
							future.setCode(value.split("\\.")[0]);
							break;
						case "ActionDay":
							future.setActionDay(Integer.parseInt(value));
							break;
						case "AskPrice":
							future.setAskPrice(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "AskVol":
							future.setAskVol(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "BidPrice":
							future.setBidPrice(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "BidVol":
							future.setBidVol(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "Close":
							future.setClose(Long.parseLong(value));
							break;
						case "High":
							future.setHigh(Long.parseLong(value));
							break;
						case "Ceil":
							future.setHighLimited(Long.parseLong(value));
							break;
						case "Low":
							future.setLow(Long.parseLong(value));
							break;
						case "Floor":
							future.setLowLimited(Long.parseLong(value));
							break;
						case "Last":
							future.setMatch(Long.parseLong(value));
							break;
						case "Open":
							future.setOpen(Long.parseLong(value));
							break;
						case "OI":
							future.setOpenInterest(Long.parseLong(value));
							break;
						case "PreClose":
							future.setPreClose(Long.parseLong(value));
							break;
						case "SettlePrice":
							future.setSettlePrice(Long.parseLong(value));
							break;
						case "Status":
							future.setStatus(Integer.parseInt(value));
							break;
						case "Time":
							future.setTime(Integer.parseInt(value));
							break;
						case "TradingDay":
							future.setTradingDay(Integer.parseInt(value));
							break;
						case "Turnover":
							future.setTurnover(Long.parseLong(value));
							break;
						case "Volume":
							future.setVolume(Long.parseLong(value));
							break;
						default:
							break;
						}
					}
				}
			}	
		} catch(Exception e) {
			log.error(e.getMessage() + " => " + in);
			return null;
		}
		
		return (future.getWindCode() == null ) ? null : future;
	}
	
	private TDF_MARKET_DATA convertMarketData(String[] in_arr,String in) {
		TDF_MARKET_DATA stock = new TDF_MARKET_DATA();
		String key = null;
		String value = null;
		String[] kv_arr = null;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							stock.setWindCode(value);
							stock.setCode(value.split("\\.")[0]);
							break;
							
						case "ActionDay":
							stock.setActionDay(Integer.parseInt(value));
							break;
						case "AskPrice":
							stock.setAskPrice(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "AskVol":
							stock.setAskVol(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "BidPrice":
							stock.setBidPrice(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "BidVol":
							stock.setBidVol(parseStringTolong(value.substring(1,
									value.length() - 1).split("\\s")));
							break;
						case "High":
							stock.setHigh(Long.parseLong(value));
							break;
						case "Ceil":
							stock.setHighLimited(Long.parseLong(value));
							break;
						case "Low":
							stock.setLow(Long.parseLong(value));
							break;
						case "Floor":
							stock.setLowLimited(Long.parseLong(value));
							break;
						case "Last":
							stock.setMatch(Long.parseLong(value));
							break;
						case "Open":
							stock.setOpen(Long.parseLong(value));
							break;
						case "IOPV":
							stock.setIOPV(Integer.parseInt(value));
							break;
						case "PreClose":
							stock.setPreClose(Long.parseLong(value));
							break;
						case "Status":
							stock.setStatus(Integer.parseInt(value));
							break;
						case "Time":
							stock.setTime(Integer.parseInt(value));
							break;
						case "TradingDay":
							stock.setTradingDay(Integer.parseInt(value));
							break;
						case "Turnover":
							stock.setTurnover(Long.parseLong(value));
							break;
						case "Volume":
							stock.setVolume(Long.parseLong(value));
							break;
						case "NumTrades":
							stock.setNumTrades(Long.parseLong(value));
							break;
						case "TotalBidVol":
							stock.setTotalBidVol(Long.parseLong(value));
							break;
						case "WgtAvgAskPrice":
							stock.setWeightedAvgAskPrice(Long.parseLong(value));
							break;
						case "WgtAvgBidPrice":
							stock.setWeightedAvgBidPrice(Long.parseLong(value));
							break;
						case "YieldToMaturity":
							stock.setYieldToMaturity(Integer.parseInt(value));
							break;
						case "Prefix":
							stock.setPrefix(value);
							break;
						case "Syl1":
							stock.setSyl1(Integer.parseInt(value));
							break;
						case "Syl2":
							stock.setSyl2(Integer.parseInt(value));
							break;
						case "SD2":
							stock.setSD2(Integer.parseInt(value));
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage() + " => " + in);
			return null;
		}
		return (stock.getWindCode() == null) ? null : stock;		
	}	
	
	private TDF_INDEX_DATA convertIndexData(String[] in_arr,String in) {
		TDF_INDEX_DATA index = new TDF_INDEX_DATA();
		String key = null;
		String value = null;
		String[] kv_arr = null;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							index.setWindCode(value);
							index.setCode(value.split("\\.")[0]);
							break;						
						case "ActionDay":
							index.setActionDay(Integer.parseInt(value));
							break;
						case "HighIndex":
							index.setHighIndex(Integer.parseInt(value));
							break;
						case "LastIndex" :
							index.setLastIndex(Integer.parseInt(value));
							break;
						case "LowIndex":
							index.setLowIndex(Integer.parseInt(value));
							break;
						case "OpenIndex":
							index.setOpenIndex(Integer.parseInt(value));
							break;
						case "PrevIndex":
							index.setPreCloseIndex(Integer.parseInt(value));
							break;
						case "Time":
							index.setTime(Integer.parseInt(value));
							break;
						case "TotalVolume":
							index.setTotalVolume(Long.parseLong(value));
							break;
						case "TradingDay" :
							index.setTradingDay(Integer.parseInt(value));
							break;
						case "Turnover" :
							index.setTurnover(Long.parseLong(value));
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage() + " => " + in);
			return null;
		}
		return (index.getWindCode() == null) ? null : index;		
	}	
}
