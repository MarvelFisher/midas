package com.cyanspring.adaptor.future.wind.gateway;

import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import cn.com.wind.td.tdf.*;

public class WindGateway {
	
	private static WindGatewayInitializer windGatewayInitializer = null; 
	private final int port;
	public static HashMap<String,TDF_FUTURE_DATA> mapFutureData = new HashMap<String,TDF_FUTURE_DATA>(); 
	public static HashMap<String,TDF_MARKET_DATA> mapMarketData = new HashMap<String,TDF_MARKET_DATA>();
	public static HashMap<String,TDF_INDEX_DATA>  mapIndexData  = new HashMap<String,TDF_INDEX_DATA>();
	public static HashMap<String,TDF_TRANSACTION> mapTransaction = new HashMap<String,TDF_TRANSACTION>();
	public static HashMap<String,ArrayList<TDF_CODE>> mapCodeTable = new HashMap<String,ArrayList<TDF_CODE>>();
	
	private static String windMFServerIP = "114.80.154.34";
	private static String windMFServerPort = "10050";
	private static String windMFServerUserId = "TD1001888002";
	private static String windMFServerUserPwd = "35328058";
	
	private static String windSFServerIP = "114.80.154.34";
	private static String windSFServerPort = "10051";
	private static String windSFServerUserId = "TD1001888001";
	private static String windSFServerUserPwd = "62015725";
	public static boolean cascading = false;
	public static String upstreamIp = "202.55.14.140";
	public static int upstreamPort = 10049;
	public static WindGateway instance = null;
	private static int typeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX | DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);	
	
	public WindGateway(int port)
	{
		this.port = port;
	}	
	
	public static boolean compareArrays(long[] array1, long[] array2) {
        if (array1 != null && array2 != null){
          if (array1.length != array2.length)
              return false;
          else
              for (int i = 0; i < array2.length; i++) {
                  if (array2[i] != array1[i]) {
                      return false;    
                  }                 
            }
        }else{
          return false;
        }
        return true;
    }
	
	public static String arrayToString(long[] array)
	{
		StringBuilder sb = new StringBuilder("[");
		if(array != null)
		{
			for(long val : array)
			{
				sb.append(Long.toString(val) + " "); 
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.append("]").toString();
	}
	
	static public String publishTransactionChanges(TDF_TRANSACTION dirty,TDF_TRANSACTION data) {
		StringBuilder sb = new StringBuilder("API=TRANSACTION|Symbol=" + data.getWindCode());
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {
			sb.append("|ActionDay=" + data.getActionDay()); 
		}
		if(dirty == null || dirty.getAskOrder() != data.getAskOrder()) {

		}
		return sb.toString();
	}
	
	static public String publishFutureChanges(TDF_FUTURE_DATA dirty,TDF_FUTURE_DATA data)
	{
		StringBuilder sb = new StringBuilder("API=DATA_FUTURE|Symbol=" + data.getWindCode());		
		if(dirty == null || dirty.getActionDay() != data.getActionDay())		
		{
			sb.append("|ActionDay=" + data.getActionDay());
		}
		if(dirty == null || false == compareArrays(dirty.getAskPrice(),data.getAskPrice()))		
		{
			sb.append("|AskPrice=" + arrayToString(data.getAskPrice()));

		}		
		if(dirty == null || false == compareArrays(dirty.getAskVol(),data.getAskVol()))		
		{
			sb.append("|AskVol=" + arrayToString(data.getAskVol()));
		}			
		if(dirty == null || false == compareArrays(dirty.getBidPrice(),data.getBidPrice()))		
		{
			sb.append("|BidPrice=" + arrayToString(data.getBidPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidVol(),data.getBidVol()))		
		{
			sb.append("|BidVol=" + arrayToString(data.getBidVol()));			
		}		
		if(dirty == null || dirty.getClose() != data.getClose())
		{
			sb.append("|Close=" + data.getClose());								
		}
		if(dirty == null || dirty.getHigh() != data.getHigh())
		{
			sb.append("|High=" + data.getHigh());		
		}
		if(dirty == null || dirty.getHighLimited() != data.getHighLimited())
		{
			sb.append("|Ceil=" + data.getHighLimited());		
		}
		if(dirty == null || dirty.getLow() != data.getLow())
		{
			sb.append("|Low=" + data.getLow());
		}
		if(dirty == null || dirty.getLowLimited() != data.getLowLimited())
		{
			sb.append("|Floor=" + data.getLowLimited());
		}
		if(dirty == null || dirty.getMatch() != data.getMatch())
		{
			sb.append("|Last=" +data.getMatch());
		}
		if(dirty == null || dirty.getOpen() != data.getOpen())
		{
			sb.append("|Open=" + data.getOpen());
		}
		if(dirty == null || dirty.getOpenInterest() != data.getOpenInterest())
		{
			sb.append("|OI=" + data.getOpenInterest());
		}
		if(dirty == null || dirty.getPreClose() != data.getPreClose())
		{
			sb.append("|PreClose=" + data.getPreClose());
		}
		if(dirty == null || dirty.getSettlePrice() != data.getSettlePrice())
		{		
			sb.append("|SettlePrice=" + data.getSettlePrice());
		}
		if(dirty == null || dirty.getPreSettlePrice() != data.getPreSettlePrice())
		{		
			sb.append("|PreSettlePrice=" + data.getPreSettlePrice());
		}		
		if(dirty == null || dirty.getStatus() != data.getStatus())
		{
			sb.append("|Status=" + data.getStatus());
		}
		if(dirty == null || dirty.getTime() != data.getTime())
		{
			sb.append("|Time=" + data.getTime());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay())
		{
			sb.append("|TradingDay=" + data.getTradingDay());			
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover())
		{
			sb.append("|Turnover=" + data.getTurnover());			
		}
		if(dirty == null || dirty.getVolume() != data.getVolume())
		{
			sb.append("|Volume=" + data.getVolume());
		}		
		return sb.toString();

	}
	
	static public String publishMarketDataChanges(TDF_MARKET_DATA dirty,TDF_MARKET_DATA data)
	{
		StringBuilder sb = new StringBuilder("API=DATA_MARKET|Symbol=" + data.getWindCode());
		if(dirty == null || dirty.getActionDay() != data.getActionDay())
		{
			sb.append("|ActionDay=" + data.getActionDay());
		}
		if(dirty == null || false == compareArrays(dirty.getAskPrice(),data.getAskPrice()))			
		{
			sb.append("|AskPrice=" + arrayToString(data.getAskPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getAskVol(),data.getAskVol()))			
		{
			sb.append("|AskVol=" + arrayToString(data.getAskVol()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidPrice(),data.getBidPrice()))			
		{
			sb.append("|BidPrice=" + arrayToString(data.getBidPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidVol(),data.getBidVol()))			
		{
			sb.append("|BidVol=" + arrayToString(data.getBidVol()));			
		}		
		if(dirty == null || dirty.getHigh() != data.getHigh())
		{
			sb.append("|High=" + data.getHigh());	
		}
		if(dirty == null || dirty.getHighLimited() != data.getHighLimited())
		{
			sb.append("|Ceil=" + data.getHighLimited());		
		}
		if(dirty == null || dirty.getLow() != data.getLow())
		{
			sb.append("|Low=" + data.getLow());
		}
		if(dirty == null || dirty.getLowLimited() != data.getLowLimited())
		{
			sb.append("|Floor=" + data.getLowLimited());
		}
		if(dirty == null || dirty.getMatch() != data.getMatch())
		{
			sb.append("|Last=" +data.getMatch());
		}
		if(dirty == null || dirty.getOpen() != data.getOpen())
		{
			sb.append("|Open=" + data.getOpen());
		}		
		if(dirty == null || dirty.getIOPV() != data.getIOPV())
		{
			sb.append("|IOPV=" + data.getIOPV());
		}
		if(dirty == null || dirty.getPreClose() != data.getPreClose())
		{
			sb.append("|PreClose=" + data.getPreClose());
		}
		if(dirty == null || dirty.getStatus() != data.getStatus())
		{
			sb.append("|Status=" + data.getStatus());
		}
		if(dirty == null || dirty.getTime() != data.getTime())
		{
			sb.append("|Time=" + data.getTime());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay())
		{
			sb.append("|TradingDay=" + data.getTradingDay());			
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover())
		{
			sb.append("|Turnover=" + data.getTurnover());			
		}
		if(dirty == null || dirty.getVolume() != data.getVolume())
		{
			sb.append("|Volume=" + data.getVolume());
		}		
		if(dirty == null || dirty.getNumTrades() != data.getNumTrades())
		{
			sb.append("|NumTrades=" + data.getNumTrades());
		}
		if(dirty == null || dirty.getTotalAskVol() != data.getTotalAskVol())
		{
			sb.append("|TotalBidVol=" + data.getTotalAskVol());
		}		
		if(dirty == null || dirty.getTotalBidVol() != data.getTotalBidVol())
		{
			sb.append("|TotalBidVol=" + data.getTotalBidVol());
		}
		if(dirty == null || dirty.getWeightedAvgAskPrice() != data.getWeightedAvgAskPrice())
		{
			sb.append("|WgtAvgAskPrice=" + data.getWeightedAvgAskPrice());
		}
		if(dirty == null || dirty.getWeightedAvgBidPrice() != data.getWeightedAvgBidPrice())
		{
			sb.append("|WgtAvgBidPrice=" + data.getWeightedAvgBidPrice());
		}		
		if(dirty == null || dirty.getYieldToMaturity() != data.getYieldToMaturity())
		{
			sb.append("|YieldToMaturity=" + data.getYieldToMaturity());
		}
		if(dirty == null || dirty.getPrefix() != data.getPrefix())
		{
			sb.append("|Prefix=" + data.getPrefix());
		}
		if(dirty == null || dirty.getSyl1() != data.getSyl1())
		{
			sb.append("|Syl1=" + data.getSyl1());
		}
		if(dirty == null || dirty.getSyl2() != data.getSyl2())
		{
			sb.append("|Syl2=" + data.getSyl2());
		}		
		if(dirty == null || dirty.getSD2() != data.getSD2())
		{
			sb.append("|SD2=" + data.getSD2());
		}
		
		return sb.toString();
	}
	
	static public String publishIndexDataChanges(TDF_INDEX_DATA dirty,TDF_INDEX_DATA data)
	{
		StringBuilder sb = new StringBuilder("API=DATA_INDEX|Symbol=" + data.getWindCode());
		if(dirty == null || dirty.getActionDay() != data.getActionDay())
		{
			sb.append("|ActionDay=" + data.getActionDay());
		}
		if(dirty == null || dirty.getHighIndex() != data.getHighIndex())
		{
			sb.append("|HighIndex=" + data.getHighIndex());
		}
		if(dirty == null || dirty.getLastIndex() != data.getLastIndex())
		{
			sb.append("|LastIndex=" + data.getLastIndex());
		}
		if(dirty == null || dirty.getLowIndex() != data.getLowIndex())
		{
			sb.append("|LowIndex=" + data.getLowIndex());
		}
		if(dirty == null || dirty.getOpenIndex() != data.getOpenIndex())
		{
			sb.append("|OpenIndex=" + data.getOpenIndex());
		}
		if(dirty == null || dirty.getPreCloseIndex() != data.getPreCloseIndex())
		{
			sb.append("|PrevIndex=" + data.getPreCloseIndex());
		}
		if(dirty == null || dirty.getTime() != data.getTime())
		{
			sb.append("|Time=" + data.getTime());
		}
		if(dirty == null || dirty.getTotalVolume() != data.getTotalVolume())
		{
			sb.append("|TotalVolume=" + data.getTotalVolume());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay())
		{
			sb.append("|TradingDay=" + data.getTradingDay());
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover())
		{
			sb.append("|Turnover=" + data.getTurnover());
		}
		return sb.toString();		
	}
	
	void publishWindData(String str,String symbol) {	
		if(windGatewayInitializer != null )	{
			WindGatewayHandler.publishWindData(str,symbol,true);
		}		
	}
	
	public void receiveHeartBeat() {	
		publishWindData("API=Heart Beat",null);
	}
	
	public void receiveTransaction(TDF_TRANSACTION transactionData) {
		if(transactionData == null) {
			return;
		}
		String symbol = transactionData.getWindCode();
		TDF_TRANSACTION data = mapTransaction.get(symbol);
		mapTransaction.put(symbol,transactionData);
		String str = publishTransactionChanges(data,transactionData);
		publishWindData(str,symbol);
		if(data != null) {
			data = null;
		}
		
	}
	
	public void receiveFutureData(TDF_FUTURE_DATA futureData) {	
		if(futureData == null) {
			return;
		}
		String symbol = futureData.getWindCode();
		TDF_FUTURE_DATA data = mapFutureData.get(symbol);
		mapFutureData.put(symbol,futureData);
		if(WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishFutureChanges(data,futureData);
			publishWindData(str,symbol);
		}
		if(data != null) {		
			data = null;
		}					
	}
	
	public void receiveMarketData(TDF_MARKET_DATA marketData) {
		if(marketData == null) {
			return;
		}
		String symbol = marketData.getWindCode();
		TDF_MARKET_DATA data = mapMarketData.get(symbol);
		mapMarketData.put(marketData.getWindCode(),marketData);		
		if(WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishMarketDataChanges(data,marketData);
			publishWindData(str,symbol);
		}
		if(data != null) {		
			data = null;
		}		
	}
	
	public void receiveIndexData(TDF_INDEX_DATA indexData)
	{
		if(indexData == null) {
			return;
		}
		String symbol = indexData.getWindCode();
		TDF_INDEX_DATA data = mapIndexData.get(symbol);
		mapIndexData.put(indexData.getWindCode(),indexData);
		if(WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishIndexDataChanges(data,indexData);
			publishWindData(str,symbol);
		}
		if(data != null) {		
			data = null;
		}		
	}
	
	public void receiveMarketClose(TDF_MARKET_CLOSE marketClose) {
		String str = "API=MarketClose|Market=" + marketClose.getMarket() + 				
				"|Time=" + marketClose.getTime() + "|Info=" + marketClose.getInfo();
		log.info(str);
		publishWindData(str,null);				
	}
	
	public void receiveQuotationDateChange(TDF_QUOTATIONDATE_CHANGE dateChange) {
		String str = "API=QDateChange|Market=" + dateChange.getMarket() +
				"|OldDate=" + dateChange.getOldDate() + "|NewDate=" + dateChange.getNewDate();
		log.info(str);
		publishWindData(str,null);			
	}
	
	public void receiveCodeTable(String strMarket,TDF_CODE[] codes) {	
		ArrayList<TDF_CODE> lst;
		if(mapCodeTable.containsKey(strMarket))
		{
			lst = mapCodeTable.get(strMarket);
		}	else	{
			lst = new ArrayList<TDF_CODE>();
			mapCodeTable.put(strMarket, lst);
		}
		synchronized(lst) {		
			lst.clear();
			for(TDF_CODE code : codes) {			
				lst.add(code);
			}
		}
	}	
	
	public void run() throws InterruptedException
	{	
		Demo demo = null,demoStock = null;
		Thread t1 = null,t2 = null,t1Stock = null,t2Stock = null,clientThread = null;
		DataWrite dw = null,dwStock = null;
		WindDataClient windDataClient = null;
		

		if(cascading) {
			windDataClient = new WindDataClient();
			clientThread = new Thread(windDataClient,"windDataClient");
			clientThread.start();						
		} else {
			if(windMFServerIP != null && windMFServerIP != "")
			{
				demo = new Demo(windMFServerIP, Integer.parseInt(windMFServerPort) , windMFServerUserId, windMFServerUserPwd , typeFlags, this);
				DataHandler dh = new DataHandler (demo);
				t1 = new Thread(dh);
				t1.start();
				dw = new DataWrite (demo);   // 用來做 demo 的 斷線 reconnect
				t2 = new Thread ( dw );
				t2.start();
			}
			
			if(windSFServerIP != null && windSFServerIP != "")
			{
				demoStock = new Demo(windSFServerIP, Integer.parseInt(windSFServerPort) , windSFServerUserId, windSFServerUserPwd , typeFlags, this);
				DataHandler dhStock = new DataHandler (demoStock);
				t1Stock = new Thread(dhStock);
				t1Stock.start();
				dwStock = new DataWrite (demoStock);   // 用來做 demo 的 斷線 reconnect
				t2Stock = new Thread ( dwStock );
				t2Stock.start();
			}
		}

	
		EventLoopGroup bossGroup   = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup(16);
		
		try
		{
			windGatewayInitializer = new WindGatewayInitializer();
			ServerBootstrap  bootstrap = new ServerBootstrap()
			.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)			
			.childHandler(windGatewayInitializer);
		
			bootstrap.bind(port).sync().channel().closeFuture().sync();
		}
		finally
		{


			if(demo != null)
			{
				demo.setQuitFlag(true);
				dw.setQuitFlag(true);	
				t1.join();
				System.out.println("Thread1 Quit!");
				t2.join();			
				System.out.println("Thread2 Quit!");
			}
			
			if(demoStock != null)
			{
				demoStock.setQuitFlag(true);
				dwStock.setQuitFlag(true);	
				t1Stock.join();
				System.out.println("Thread1 for Stock Quit!");
				t2Stock.join();			
				System.out.println("Thread2 for Stock Quit!");			
			}
			if(clientThread != null) {
				windDataClient.stop();
				clientThread.join();				
			}
			
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();			
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException
	{
		DOMConfigurator.configure("conf/windGatewaylog4j.xml");
				
		String current = null;
		try {
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	        System.out.println("Current dir:"+current);		
	
	    //Reading properties file in Java example
        Properties props = new Properties();
        FileInputStream fis; 
        String serverPort = "10049";
      
        //loading properites from properties file
        try {
        	fis = new FileInputStream("conf/windGateway.xml");
			props.loadFromXML(fis);
	        //reading proeprty
	        serverPort = props.getProperty("Server Port");
	        WindGateway.windMFServerIP = props.getProperty("Wind Merchandise Server IP");
	        WindGateway.windMFServerPort = props.getProperty("Wind Merchandise Server Port");
	        WindGateway.windMFServerUserId = props.getProperty("Wind Merchandise User Id");
	        WindGateway.windMFServerUserPwd = props.getProperty("Wind Merchandise User Pwd");
	        
	        WindGateway.windSFServerIP = props.getProperty("Wind Stock Server IP");
	        WindGateway.windSFServerPort = props.getProperty("Wind Stock Server Port");
	        WindGateway.windSFServerUserId = props.getProperty("Wind Stock User Id");
	        WindGateway.windSFServerUserPwd = props.getProperty("Wind Stock User Pwd");
	        
	        WindGateway.cascading = props.getProperty("Cascading","true").equalsIgnoreCase("true");  
	        WindGateway.upstreamIp = props.getProperty("Upstream IP","202.55.14.140");
	        WindGateway.upstreamPort = Integer.parseInt(props.getProperty("Upstream Port","100"));
	        
	        WindGateway.typeFlags = Integer.parseInt(props.getProperty("Type Flags","17")); // DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX | DATA_TYPE_FLAG.DATA_TYPE_INDEX
	     		
		} catch (InvalidPropertiesFormatException e) {
			log.error(e.getMessage(),e);		
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
        
        instance = new WindGateway(Integer.parseInt(serverPort));
        instance.run();
	}

		
}
