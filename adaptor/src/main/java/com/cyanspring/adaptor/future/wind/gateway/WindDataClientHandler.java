package com.cyanspring.adaptor.future.wind.gateway;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindDataClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(WindDataClientHandler.class);
	public static ChannelHandlerContext ctx = null;
	
	private int bufLenMin = 0,bufLenMax = 0,blockCount = 0;
	private long throughput = 0,dataReceived = 0,msLastTime = 0,msDiff = 0;

	
	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		
		msLastTime = System.currentTimeMillis();
		ctx = arg0;
		ctx.channel().write(WindGatewayHandler.addHashTail("API=ReqHeartBeat",true));
		ctx.channel().write(WindGatewayHandler.addHashTail("API=GetMarkets",true));
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

		try {
			if(arg1 instanceof String) {
				String in;
				in = (String)arg1;
				if(in != null) {
					processData(in);
					calculateMessageFlow(in.length());					
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
				if (strDataType.equals("TRANSACTION")) {
					convertTransaction(in_arr,in);
				}
				if (strDataType.equals("DATA_FUTURE")) {
					convertFutureData(in_arr,in);					
				}
				if (strDataType.equals("DATA_MARKET")) {
					convertMarketData(in_arr,in);
				}
				if (strDataType.equals("DATA_INDEX")) {
					convertIndexData(in_arr,in);
				}
				if (strDataType.equals("Heart Beat")) {
					WindGateway.instance.publishWindDataNoHash(in, null);
				}
				if (strDataType.equals("QDateChange")) {
					WindGateway.instance.publishWindDataNoHash(in, null);
				}
				if (strDataType.equals("MarketClose")) {
					WindGateway.instance.publishWindDataNoHash(in, null);
				}
				if (strDataType.equals("CODE")) {
					WindGateway.instance.publishWindDataNoHash(in, null);
				}
				if (strDataType.equals("Markets")) {
					WindGateway.instance.convertMarkets(in_arr);
					WindGateway.instance.publishWindDataNoHash(in, null);					
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
	
	private void convertFutureData(String[] in_arr,String in) {
		String key = null;
		String value = null;
		String[] kv_arr = null;
		String symbol;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							symbol = value;
							WindGatewayHandler.publishWindData(in,symbol,false);
							return;
						default:
							break;
						}
					}
				}
			}	
		} catch(Exception e) {
			log.error(e.getMessage() + " => " + in);
		}
	}
	
	private void convertMarketData(String[] in_arr,String in) {
		String key = null;
		String value = null;
		String[] kv_arr = null;
		String symbol;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							symbol = value;
							WindGatewayHandler.publishWindData(in,symbol,false);
							return;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage() + " => " + in);			
		}		
	}	
	
	private void convertIndexData(String[] in_arr,String in) {
		String key = null;
		String value = null;
		String[] kv_arr = null;
		String symbol;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							symbol = value;
							WindGatewayHandler.publishWindData(in,symbol,false);
							return;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage() + " => " + in);
		}	
	}
	
	private void convertTransaction(String[] in_arr,String in) {
		String key = null;
		String value = null;
		String[] kv_arr = null;
		String symbol;

		try {
			for (int i = 0; i < in_arr.length; i++) {
				if (in_arr[i] != null && !"".equals(in_arr[i])) {
					kv_arr = in_arr[i].split("=");
					if (kv_arr.length > 1) {
						key = kv_arr[0];
						value = kv_arr[1];
						switch (key) {
						case "Symbol" :
							symbol = value;
							WindGatewayHandler.publishWindTransaction(in, symbol, false);
							return;
						default:
							break;
						}
					}
				}
			}	
		} catch(Exception e) {
			log.error(e.getMessage() + " => " + in);
		}
	}	
}
