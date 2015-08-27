package com.cyanspring.adaptor.future.wind.gateway;

import java.sql.Date;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.InvalidPropertiesFormatException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.Network.Transport.FDTFields;
import com.cyanspring.Network.Transport.FDTFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import cn.com.wind.td.tdf.*;

public class WindGateway implements Runnable {
	
	private static WindGatewayInitializer windGatewayInitializer = null; 
	public static ConcurrentHashMap<String,TDF_FUTURE_DATA> mapFutureData = new ConcurrentHashMap<String,TDF_FUTURE_DATA>(); 
	public static ConcurrentHashMap<String,TDF_MARKET_DATA> mapMarketData = new ConcurrentHashMap<String,TDF_MARKET_DATA>();
	public static ConcurrentHashMap<String,TDF_INDEX_DATA>  mapIndexData  = new ConcurrentHashMap<String,TDF_INDEX_DATA>();
	public static ConcurrentHashMap<String,BuySellVolume>   mapBuySell = new ConcurrentHashMap<String,BuySellVolume>();
	public static ConcurrentHashMap<String,TDF_TRANSACTION> mapTransaction = new ConcurrentHashMap<String,TDF_TRANSACTION>();
	public static ConcurrentHashMap<String,CodeTable> mapCodeTable = new ConcurrentHashMap<String,CodeTable>();
	public static ConcurrentHashMap<String,FutureTurnover> mapFutureTurnover = new ConcurrentHashMap<String,FutureTurnover>();
	private LinkedBlockingQueue<Object> msgpackQueue = new LinkedBlockingQueue<Object>();
	

	


	public static WindGateway instance = null;
	private static int stockTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_TRANSACTION | DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	private static int merchandiseTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX | DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);
	
	private int serverPort;  // delimiter string packet port
	private String windMFServerIP = "114.80.154.34";
	private int windMFServerPort = 10050;
	private String windMFServerUserId = "TD1001888002";
	private String windMFServerUserPwd = "35328058";
	private boolean windMFWholeMarket = false;
	private boolean windMFWindReconnect = false;
	private String windMFMarkets = "";
	
	private String windSFServerIP = "114.80.154.34";
	private int windSFServerPort = 10051;
	private String windSFServerUserId = "TD1001888001";
	private String windSFServerUserPwd = "62015725";
	private boolean windSFWholeMarket = false;
	private boolean windSFWindReconnect = false;	
	private String windSFMarkets = "";
	
	private String windSSServerIP = "114.80.154.34";
	private int windSSServerPort = 10051;
	private String windSSServerUserId = "TD1001888001";
	private String windSSServerUserPwd = "62015725";
	private boolean windSSWholeMarket = false;
	private boolean windSSWindReconnect = false;
	private String windSSMarkets = "";	
	
	public static boolean cascading = false;
	public static String upstreamIp = "202.55.14.140";
	public static int upstreamPort = 10049;
	
	public static boolean mpCascading = false;
	public static String mpUpstreamIp = "202.55.14.140";
	public static int mpUpstreamPort = 10048;
	
	public static MsgPackLiteServer msgPackLiteServer = null;
	public static boolean dedicatedWindThread = false;
	Demo demo = null,demoStock = null,demoSpare = null;
	public static int autoTermination = 0;
	
	
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getWindMFServerIP() {
		return this.windMFServerIP;
	}
	public void setWindMFServerIP(String ip) {
		this.windMFServerIP = ip;
	}
	
	public int getWindMFServerPort() {
		return this.windMFServerPort;
	}
	public void setWindMFServerPort(int port) {
		this.windMFServerPort = port;
	}
	
	public String getWindMFServerUserId() {
		return this.windMFServerUserId;
	}
	public void setWindMFServerUserId(String userId) {
		this.windMFServerUserId = userId;
	}
	
	public String getWindMFServerUserPwd() {
		return this.windMFServerUserPwd;
	}
	public void setWindMFServerUserPwd(String pwd) {
		this.windMFServerUserPwd = pwd;
	}	
	
	public boolean getWindMFWholeMarket() {
		return this.windMFWholeMarket;
	}
	public void setWindMFWholeMarket(boolean b) {
		this.windMFWholeMarket = b;
	}	
	
	public boolean getWindMFWindReconnect() {
		return this.windMFWindReconnect;
	}
	public void setWindMFWindReconnect(boolean b) {
		this.windMFWindReconnect = b;
	}
	
	public String getWindMFMarkets() {
		return this.windMFMarkets;
	}
	public void setWindMFMarkets(String s) {
		this.windMFMarkets = s;
	}
			
	public String getWindSFServerIP() {
		return this.windSFServerIP;
	}
	public void setWindSFServerIP(String ip) {
		this.windSFServerIP = ip;
	}
	
	public int getWindSFServerPort() {
		return this.windSFServerPort;
	}
	public void setWindSFServerPort(int port) {
		this.windSFServerPort = port;
	}
	
	public String getWindSFServerUserId() {
		return this.windSFServerUserId;
	}
	public void setWindSFServerUserId(String userId) {
		this.windSFServerUserId = userId;
	}
	
	public String getWindSFServerUserPwd() {
		return this.windSFServerUserPwd;
	}
	public void setWindSFServerUserPwd(String pwd) {
		this.windSFServerUserPwd = pwd;
	}		
	
	public boolean getWindSFWholeMarket() {
		return this.windSFWholeMarket;
	}
	public void setWindSFWholeMarket(boolean b) {
		this.windSFWholeMarket = b;
	}
	
	public boolean getWindSFWindReconnect() {
		return this.windSFWindReconnect;
	}
	public void setWindSFWindReconnect(boolean b) {
		this.windSFWindReconnect = b;
	}	
	
	public String getWindSFMarkets() {
		return this.windSFMarkets;
	}
	public void setWindSFMarkets(String s) {
		this.windSFMarkets = s;
	}		
	
	public String getWindSSServerIP() {
		return this.windSSServerIP;
	}
	public void setWindSSServerIP(String ip) {
		this.windSSServerIP = ip;
	}
	
	public int getWindSSServerPort() {
		return this.windSSServerPort;
	}
	public void setWindSSServerPort(int port) {
		this.windSSServerPort = port;
	}
	
	public String getWindSSServerUserId() {
		return this.windSSServerUserId;
	}
	public void setWindSSServerUserId(String userId) {
		this.windSSServerUserId = userId;
	}
	
	public String getWindSSServerUserPwd() {
		return this.windSSServerUserPwd;
	}
	public void setWindSSServerUserPwd(String pwd) {
		this.windSSServerUserPwd = pwd;
	}		
	
	public boolean getWindSSWholeMarket() {
		return this.windSSWholeMarket;
	}
	public void setWindSSWholeMarket(boolean b) {
		this.windSSWholeMarket = b;
	}
	
	public boolean getWindSSWindReconnect() {
		return this.windSSWindReconnect;
	}
	public void setWindSSWindReconnect(boolean b) {
		this.windSSWindReconnect = b;
	}		
	
	public String getWindSSMarkets() {
		return this.windSSMarkets;
	}
	public void setWindSSMarkets(String s) {
		this.windSSMarkets = s;
	}	
	
	public boolean getCascading() {
		return cascading;
	}
	public void setCascading(boolean b) {
		cascading = b;
	}
	
	public String getUpstreamIp() {
		return upstreamIp;
	}
	public void setUpstreamIp(String ip) {
		upstreamIp = ip;
	}
	
	public int getUpstreamPort() {
		return upstreamPort;
	}
	public void setUpstreamPort(int port) {
		upstreamPort = port;
	}
	
	
	public boolean getMpCascading() {
		return mpCascading;
	}
	public void setMpCascading(boolean b) {
		mpCascading = b;
	}

	public String getMpUpstreamIp() {
		return mpUpstreamIp;
	}
	public void setMpUpstreamIp(String ip) {
		mpUpstreamIp = ip;
	}
	
	public int getMpUpstreamPort() {
		return mpUpstreamPort;
	}
	public void setMpUpstreamPort(int port) {
		mpUpstreamPort = port;
	}
	
	public int getStockTypeFlags() {
		return stockTypeFlags;
	}
	public void setStockTypeFlags(int flag) {
		stockTypeFlags = flag;
	}

	public int getMerchandiseTypeFlags() {
		return merchandiseTypeFlags;
	}
	public void setMerchandiseTypeFlags(int flag) {
		merchandiseTypeFlags = flag;
	}
	
	public boolean getDedicatedWindThread() {
		return dedicatedWindThread;
	}
	public void setDedicatedWindThread(boolean b) {
		dedicatedWindThread = b;
	}
	
	public int getAutoTermination() {
		return autoTermination;
	}
	public void setAutoTermination(int val) {
		autoTermination = val;
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
	
	public static ArrayList<Long> convertLongArrayToList(long[] array) {
		ArrayList<Long>lst = new ArrayList<Long>();
		for(int i = 0; i < array.length; i++) {
			lst.add(array[i]);
		}
		return lst;
	}
	
	static public String publishTransactionChanges(TDF_TRANSACTION dirty,TDF_TRANSACTION data) {
		StringBuilder sb = new StringBuilder("API=TRANSACTION|Symbol=" + data.getWindCode());
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {
			sb.append("|AD=" + data.getActionDay());
		}
		if(dirty == null || dirty.getTime() != data.getTime()) {
			sb.append("|Tm=" + data.getTime());
		}
		if(dirty == null || dirty.getIndex() != data.getIndex()) {
			sb.append("|Id=" + data.getIndex());
		}
		if(dirty == null || dirty.getPrice() != data.getPrice()) {
			sb.append("|Pr=" + data.getPrice());
		}
		if(dirty == null || dirty.getVolume() != data.getVolume()) {
			sb.append("|Vl=" + data.getVolume());
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover()) {	
			sb.append("|To=" + data.getTurnover());
		}
		if(dirty == null || dirty.getBSFlag() != data.getBSFlag()) {
			sb.append("|BS=" + data.getBSFlag());
		}
		if(dirty == null || dirty.getOrderKind() != data.getOrderKind()) {
			sb.append("|OK=" + data.getOrderKind());
		}
		if(dirty == null || dirty.getFunctionCode() != data.getFunctionCode()) {
			sb.append("|FC=" + data.getFunctionCode());
		}

		return sb.toString();
	}
	
	static public HashMap<Integer,Object> publishTransactionChangesToMap(TDF_TRANSACTION dirty,TDF_TRANSACTION data) {	
		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.WindTransaction);
		map.put(FDTFields.WindSymbolCode, data.getWindCode());
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {
			map.put(FDTFields.ActionDay, data.getActionDay());
		}
		if(dirty == null || dirty.getTime() != data.getTime()) {
			map.put(FDTFields.Time,data.getTime());
		}
		if(dirty == null || dirty.getIndex() != data.getIndex()) {
			map.put(FDTFields.IndexNumber,data.getIndex());
		}
		if(dirty == null || dirty.getPrice() != data.getPrice()) {
			map.put(FDTFields.Last,data.getPrice());
		}
		if(dirty == null || dirty.getVolume() != data.getVolume()) {
			map.put(FDTFields.Volume,data.getVolume());
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover()) {			
			map.put(FDTFields.Turnover,data.getTurnover());
		}
		if(dirty == null || dirty.getBSFlag() != data.getBSFlag()) {
			map.put(FDTFields.BuySellFlag,data.getBSFlag());		
		}
		
		return map;
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
	static public HashMap<Integer,Object> publishFutureChangesToMap(TDF_FUTURE_DATA dirty,TDF_FUTURE_DATA data,FutureTurnover ft) {	
		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.WindFutureData);
		map.put(FDTFields.WindSymbolCode, data.getWindCode());			
		
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {		
			map.put(FDTFields.ActionDay, data.getActionDay());
		}
		if(dirty == null || false == compareArrays(dirty.getAskPrice(),data.getAskPrice()))	{			
			map.put(FDTFields.AskPriceArray,convertLongArrayToList(data.getAskPrice()) );
		}		
		if(dirty == null || false == compareArrays(dirty.getAskVol(),data.getAskVol()))	{
			map.put(FDTFields.AskVolumeArray, convertLongArrayToList(data.getAskVol()));
		}			
		if(dirty == null || false == compareArrays(dirty.getBidPrice(),data.getBidPrice())) {				
			map.put(FDTFields.BidPriceArray, convertLongArrayToList(data.getBidPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidVol(),data.getBidVol())) {			
			map.put(FDTFields.BidVolumeArray,convertLongArrayToList(data.getBidVol()));			
		}		
		if(dirty == null || dirty.getClose() != data.getClose()) {		
			map.put(FDTFields.Close,data.getClose());								
		}
		if(dirty == null || dirty.getHigh() != data.getHigh()) {		
			map.put(FDTFields.High,data.getHigh());		
		}
		if(dirty == null || dirty.getHighLimited() != data.getHighLimited()) {		
			map.put(FDTFields.HighLimit,data.getHighLimited());		
		}
		if(dirty == null || dirty.getLow() != data.getLow()) {		
			map.put(FDTFields.Low,data.getLow());
		}
		if(dirty == null || dirty.getLowLimited() != data.getLowLimited()) {		
			map.put(FDTFields.LowLimit,data.getLowLimited());
		}
		if(dirty == null || dirty.getMatch() != data.getMatch()) {		
			map.put(FDTFields.Last,data.getMatch());
		}
		if(dirty == null || dirty.getOpen() != data.getOpen()) {		
			map.put(FDTFields.Open,data.getOpen());
		}
		if(dirty == null || dirty.getOpenInterest() != data.getOpenInterest()) {		
			map.put(FDTFields.OpenInterest,data.getOpenInterest());
		}
		if(dirty == null || dirty.getPreClose() != data.getPreClose()) {		
			map.put(FDTFields.PreClose,data.getPreClose());
		}
		if(dirty == null || dirty.getSettlePrice() != data.getSettlePrice()) {		
			map.put(FDTFields.SettlePrice,data.getSettlePrice());
		}
		if(dirty == null || dirty.getPreSettlePrice() != data.getPreSettlePrice()) {		
			map.put(FDTFields.PreSettlePrice,data.getPreSettlePrice());
		}		
		if(dirty == null || dirty.getStatus() != data.getStatus()) {		
			map.put(FDTFields.Status,data.getStatus());
		}
		if(dirty == null || dirty.getTime() != data.getTime()) {		
			map.put(FDTFields.Time,data.getTime());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay()) {		
			map.put(FDTFields.TradingDay,data.getTradingDay());			
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover()) {	
			map.put(FDTFields.Turnover,data.getTurnover());			
		}
		if(dirty == null || dirty.getVolume() != data.getVolume()) {		
			map.put(FDTFields.Volume,data.getVolume());			
			if(ft == null) {
				ft = mapFutureTurnover.get(data.getWindCode());
			}			
			if(ft != null) {
				map.put(FDTFields.FTurnover, ft.lFTurnover);
			}
		}		
		return map;
	}
	
	static public String publishMarketDataChanges(TDF_MARKET_DATA dirty,TDF_MARKET_DATA data)
	{
		String symbol = data.getWindCode();
		StringBuilder sb = new StringBuilder("API=DATA_MARKET|Symbol=" + symbol);
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
			BuySellVolume bs = mapBuySell.get(symbol);
			if(bs != null) {
				sb.append("|BuyVolume=" + bs.lBuyVolume);
				sb.append("|BuyTurnover=" + bs.lBuyTurnover);
				sb.append("|SellVolume=" + bs.lSellVolume);
				sb.append("|SellTurnover=" + bs.lSellTurnover);
				sb.append("|UnclassifiedVolume=" + bs.lUnclassifiedVolume);
				sb.append("|UnclassifiedTurnover=" + bs.lUnclassifiedTurnover);
			}			
		}		
		if(dirty == null || dirty.getNumTrades() != data.getNumTrades())
		{
			sb.append("|NumTrades=" + data.getNumTrades());
		}
		if(dirty == null || dirty.getTotalAskVol() != data.getTotalAskVol())
		{
			sb.append("|TotalAskVol=" + data.getTotalAskVol());
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
	
	static public HashMap<Integer,Object> publishMarketDataChangesToMap(TDF_MARKET_DATA dirty,TDF_MARKET_DATA data)
	{		
		String symbol = data.getWindCode();
		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.WindMarketData);
		map.put(FDTFields.WindSymbolCode, symbol);		
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {		
			map.put(FDTFields.ActionDay,data.getActionDay());
		}
		if(dirty == null || false == compareArrays(dirty.getAskPrice(),data.getAskPrice())) {					
			map.put(FDTFields.AskPriceArray,convertLongArrayToList(data.getAskPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getAskVol(),data.getAskVol()))	{		
			map.put(FDTFields.AskVolumeArray,convertLongArrayToList(data.getAskVol()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidPrice(),data.getBidPrice())) {					
			map.put(FDTFields.BidPriceArray,convertLongArrayToList(data.getBidPrice()));			
		}
		if(dirty == null || false == compareArrays(dirty.getBidVol(),data.getBidVol())) {					
			map.put(FDTFields.BidVolumeArray,convertLongArrayToList(data.getBidVol()));			
		}		
		if(dirty == null || dirty.getHigh() != data.getHigh()) {		
			map.put(FDTFields.High,data.getHigh());	
		}
		if(dirty == null || dirty.getHighLimited() != data.getHighLimited()) {		
			map.put(FDTFields.HighLimit,data.getHighLimited());		
		}
		if(dirty == null || dirty.getLow() != data.getLow()) {		
			map.put(FDTFields.Low,data.getLow());
		}
		if(dirty == null || dirty.getLowLimited() != data.getLowLimited())
		{
			map.put(FDTFields.LowLimit,data.getLowLimited());
		}
		if(dirty == null || dirty.getMatch() != data.getMatch()) {		
			map.put(FDTFields.Last,data.getMatch());
		}
		if(dirty == null || dirty.getOpen() != data.getOpen()) {		
			map.put(FDTFields.Open,data.getOpen());
		}		
		if(dirty == null || dirty.getPreClose() != data.getPreClose()) {		
			map.put(FDTFields.PreClose,data.getPreClose());
		}
		if(dirty == null || dirty.getStatus() != data.getStatus()) {		
			map.put(FDTFields.Status,data.getStatus());
		}
		if(dirty == null || dirty.getTime() != data.getTime()) {		
			map.put(FDTFields.Time,data.getTime());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay()) {		
			map.put(FDTFields.TradingDay,data.getTradingDay());			
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover()) {		
			map.put(FDTFields.Turnover,data.getTurnover());			
		}
		if(dirty == null || dirty.getVolume() != data.getVolume())
		{
			map.put(FDTFields.Volume,data.getVolume());
			BuySellVolume bs = mapBuySell.get(symbol);
			if(bs != null) {
				map.put(FDTFields.BuyVolume, bs.lBuyVolume);
				map.put(FDTFields.BuyTurnover, bs.lBuyTurnover);
				map.put(FDTFields.SellVolume, bs.lSellVolume);
				map.put(FDTFields.SellTurnover, bs.lSellTurnover);
				map.put(FDTFields.UnclassifiedVolume, bs.lUnclassifiedVolume);
				map.put(FDTFields.UnclassifiedTurnover, bs.lUnclassifiedTurnover);
			}
		}		
		if(dirty == null || dirty.getNumTrades() != data.getNumTrades()) {		
			map.put(FDTFields.NumberOfTrades,data.getNumTrades());
		}
		if(dirty == null || dirty.getTotalAskVol() != data.getTotalAskVol()) {		
			map.put(FDTFields.TotalAskVolume,data.getTotalAskVol());
		}		
		if(dirty == null || dirty.getTotalBidVol() != data.getTotalBidVol()) {		
			map.put(FDTFields.TotalBidVolume,data.getTotalBidVol());
		}
		if(dirty == null || dirty.getWeightedAvgAskPrice() != data.getWeightedAvgAskPrice())
		{
			map.put(FDTFields.WgtAvgAskPrice,data.getWeightedAvgAskPrice());
		}
		if(dirty == null || dirty.getWeightedAvgBidPrice() != data.getWeightedAvgBidPrice())
		{
			map.put(FDTFields.WgtAvgBidPrice,data.getWeightedAvgBidPrice());
		}		
		if(dirty == null || dirty.getYieldToMaturity() != data.getYieldToMaturity())
		{
			map.put(FDTFields.YieldToMaturity,data.getYieldToMaturity());
		}
		if(dirty == null || dirty.getPrefix().compareTo(data.getPrefix()) != 0)
		{
			map.put(FDTFields.Prefix,data.getPrefix());
		}
		if(dirty == null || dirty.getSyl1() != data.getSyl1())
		{
			map.put(FDTFields.Syl1,data.getSyl1());
		}
		if(dirty == null || dirty.getSyl2() != data.getSyl2())
		{
			map.put(FDTFields.Syl2,data.getSyl2());
		}		
		if(dirty == null || dirty.getSD2() != data.getSD2())
		{
			map.put(FDTFields.SD2,data.getSD2());
		}		
		if(map.size() == 2) {
			log.debug("Data No Change : " + symbol);
			return null;
		}
		return map;
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
	
	static public HashMap<Integer,Object> publishIndexDataChangesToMap(TDF_INDEX_DATA dirty,TDF_INDEX_DATA data) {	
		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.WindIndexData);
		map.put(FDTFields.WindSymbolCode, data.getWindCode());	
		if(dirty == null || dirty.getActionDay() != data.getActionDay()) {		
			map.put(FDTFields.ActionDay,data.getActionDay());
		}
		if(dirty == null || dirty.getHighIndex() != data.getHighIndex()) {		
			map.put(FDTFields.High,data.getHighIndex());
		}
		if(dirty == null || dirty.getLastIndex() != data.getLastIndex()) {		
			map.put(FDTFields.Last,data.getLastIndex());
		}
		if(dirty == null || dirty.getLowIndex() != data.getLowIndex()) {		
			map.put(FDTFields.Low,data.getLowIndex());
		}
		if(dirty == null || dirty.getOpenIndex() != data.getOpenIndex()) {		
			map.put(FDTFields.Open,data.getOpenIndex());
		}
		if(dirty == null || dirty.getPreCloseIndex() != data.getPreCloseIndex()) {		
			map.put(FDTFields.PreClose,data.getPreCloseIndex());
		}
		if(dirty == null || dirty.getTime() != data.getTime()) {		
			map.put(FDTFields.Time,data.getTime());
		}
		if(dirty == null || dirty.getTotalVolume() != data.getTotalVolume()) {		
			map.put(FDTFields.Volume,data.getTotalVolume());
		}
		if(dirty == null || dirty.getTradingDay() != data.getTradingDay()) {	
			map.put(FDTFields.TradingDay,data.getTradingDay());
		}
		if(dirty == null || dirty.getTurnover() != data.getTurnover()) {		
			map.put(FDTFields.Turnover,data.getTurnover());
		}
		return map;		
	}	
	
	void publishWindData(String str,String symbol) {	
		if(windGatewayInitializer != null )	{
			WindGatewayHandler.publishWindData(str,symbol,true);
		}		
	}
	
	void publishWindTransaction(String str,String symbol) {	
		if(windGatewayInitializer != null )	{
			WindGatewayHandler.publishWindTransaction(str,symbol,true);
		}		
	}	
	
	void publishWindDataNoHash(String str,String symbol) {	
		if(windGatewayInitializer != null )	{
			WindGatewayHandler.publishWindData(str,symbol,false);
		}		
	}	
		
	
	
	public void receiveHeartBeat() {	
		publishWindData("API=Heart Beat",null);

		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.WindHeartBeat);
		MsgPackLiteDataServerHandler.sendMessagePackToAllClient(map);
	}
	
	public void receiveTransaction(TDF_TRANSACTION transactionData) {
		if(transactionData == null || transactionData.getPrice() == 0) {
			return;
		}
		String symbol = transactionData.getWindCode();
		BuySellVolume bs = mapBuySell.get(symbol);
		if(bs == null) {
			mapBuySell.put(symbol, new BuySellVolume(transactionData));
		} else {
			bs.Calculate(transactionData);
		}
		TDF_TRANSACTION data = mapTransaction.get(symbol);
		mapTransaction.put(symbol,transactionData);
		if(windGatewayInitializer != null && WindGatewayHandler.isRegisteredTransactionByClient(symbol)) {
			String str = publishTransactionChanges(data,transactionData);
			publishWindTransaction(str,symbol);
		}
		if(MsgPackLiteDataServerHandler.isRegisteredTransactionByClient(symbol)) {			
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistrationTransaction(publishTransactionChangesToMap(data,transactionData), symbol);
		}		
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
		
		FutureTurnover ft = mapFutureTurnover.get(symbol);
		if(ft == null) {
			ft = new FutureTurnover();
			mapFutureTurnover.put(symbol, ft);
		}
		ft.Calculate(futureData);
		
		if(windGatewayInitializer != null && WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishFutureChanges(data,futureData);
			publishWindData(str,symbol);
		}
		if(MsgPackLiteDataServerHandler.isRegisteredByClient(symbol)) {			
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishFutureChangesToMap(data,futureData,ft), symbol,true);
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
		if(marketData.getVolume() == 0)
		{
			BuySellVolume bs = mapBuySell.get(symbol);
			if(bs != null) {
				bs.Reset();	
			}
		}
			
		if(windGatewayInitializer != null && WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishMarketDataChanges(data,marketData);
			publishWindData(str,symbol);
		}
		if(MsgPackLiteDataServerHandler.isRegisteredByClient(symbol)) {			
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishMarketDataChangesToMap(data,marketData), symbol,true);
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
		String symbol = indexData.getWindCode().toUpperCase();  // 配合 DCE SHF 以小寫註冊商品,但回來是大寫
		TDF_INDEX_DATA data = mapIndexData.get(symbol);
		mapIndexData.put(symbol,indexData);
		if(windGatewayInitializer != null && WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishIndexDataChanges(data,indexData);
			publishWindData(str,symbol);
		}
		if(MsgPackLiteDataServerHandler.isRegisteredByClient(symbol)) {			
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishIndexDataChangesToMap(data,indexData), symbol,true);
		}
		if(data != null) {		
			data = null;
		}		
	}
	
	public void receiveMarketClose(TDF_MARKET_CLOSE marketClose) {
		String str = "API=MarketClose|Market=" + marketClose.getMarket() + 				
				"|Time=" + marketClose.getTime() + "|Info=" + marketClose.getInfo();
		log.info(str);
		if(windGatewayInitializer != null) {
			publishWindData(str,null);
		}
		MsgPackLiteDataServerHandler.sendMarketClose(marketClose.getMarket(),marketClose.getTime(),marketClose.getInfo());
		
	}
	
	public void receiveQuotationDateChange(TDF_QUOTATIONDATE_CHANGE dateChange) {
		String str = "API=QDateChange|Market=" + dateChange.getMarket() +
				"|OldDate=" + dateChange.getOldDate() + "|NewDate=" + dateChange.getNewDate();
		log.info(str);
		if(windGatewayInitializer != null) {
			publishWindData(str,null);	
		}
		MsgPackLiteDataServerHandler.sendQuotationDateChange(dateChange.getMarket(),dateChange.getOldDate(),dateChange.getNewDate());
	}
	
	public void resetCodeTable(String strMarket) {
		mapCodeTable.put(strMarket, new CodeTable(strMarket,0,0,null));
	}
	
    public static HashMap<Integer, Object> publishCodeTableResult(String market,int codeDate,int dataCount,long hc) {
    	HashMap<Integer, Object> map = new HashMap<Integer, Object>();
    	map.put(FDTFields.PacketType, FDTFields.WindCodeTableResult);
    	map.put(FDTFields.SecurityExchange, market);
    	map.put(FDTFields.ActionDay,codeDate);
    	map.put(FDTFields.DataCount,dataCount);
    	map.put(FDTFields.HashCode, hc);  
    	return map;
    }	
	
	public void receiveCodeTable(String strMarket,TDF_CODE[] codes,int codeDate) {	
		CodeTable ct = null;
		long codesHashCode = 0;
		
		if(codes == null || codes.length == 0) {
			return;
		}
		
		for(TDF_CODE code : codes) {						
			codesHashCode += ((long)code.getWindCode().hashCode() + (long)code.getCNName().hashCode());
		}
		
		if(mapCodeTable.containsKey(strMarket)) {		
			ct = mapCodeTable.get(strMarket);
			if(ct != null) {
				if(ct.CodeDate == codeDate) {
					log.info("Code Table Same Date , Market : " + strMarket + " , Date : " + codeDate);									
					if(codesHashCode == ct.codesHashCode) {
						log.info("Code Table No Change , Market : " + strMarket);
						return;
					}
				}
			}
			mapCodeTable.remove(strMarket);
			ct = null;
		}	
		
		HashMap<Integer, Object> map = publishCodeTableResult(strMarket,codeDate,codes.length,codesHashCode);
		ct = new CodeTable(strMarket,codeDate,codesHashCode,map);

		for(TDF_CODE code : codes) {						
			ct.mapCode.put(code.getWindCode(),code);					
		}				
		
		mapCodeTable.put(strMarket, ct);
		MsgPackLiteDataServerHandler.sendMessagePackToAllClient(map);

	}
	
	public static ConcurrentHashMap<String,TDF_CODE> getCodeTableByMarket(String market) {
		if(mapCodeTable.containsKey(market)) {
			return mapCodeTable.get(market).mapCode;
		}
		return null;
	}
	
	
	public void connectedWithWind(String[] markets) {
		MsgPackLiteDataServerHandler.connectedWithWind(markets);
	}
	
	public void flushAllClientMsgPack() {
		MsgPackLiteDataServerHandler.flushAllClientMsgPack();
	}
	
	public void convertMarkets(String[] in_arr) {
		for(String str : in_arr) {
			if(str.contains("Markets=")) {
				String[] markets = str.substring(8).split(",");
				for(String market : markets) {
					if(market != null && market.isEmpty() == false) {
						resetCodeTable(market);
					}
				}
			}
		}
	}
	
	public void convertMarketsMP(ArrayList<String> markets) {

		for(String market : markets) {
			if(market != null && market.isEmpty() == false) {
				resetCodeTable(market);
			}
		}
	}	
	
	public void requestSymbol(String sym) {
		if(demoStock != null && windSFWholeMarket == false) {		
			demoStock.AddRequest(new WindRequest(WindRequest.Subscribe,sym.toUpperCase()));
		}
		if(demoSpare != null && windSSWholeMarket == false) {		
			demoSpare.AddRequest(new WindRequest(WindRequest.Subscribe,sym.toUpperCase()));
		}		
	}
	public void requestSymbolMF(String sym) {
		if(demo != null && windMFWholeMarket == false) {	
			demo.AddRequest(new WindRequest(WindRequest.Subscribe,sym));
		}	
	}
	
	/*
	public void requestCodeTable(String market) {
		if(demoStock != null)
		{
			demoStock.AddRequest(new WindRequest(WindRequest.RequestCodeTable,market.toUpperCase()));
		}
	}
	*/	
	
	public void AddMessage(Object obj) {
		msgpackQueue.add(obj);
	}
	
	private void ThreadJoin(Thread trd) {
		try {
			trd.join(1000);		
			if(trd.isAlive()) {
				trd.interrupt();
			}
		} catch (InterruptedException e) {
			log.info(e.getMessage() + " : " + trd.getName() );
		}		
	}
	
	public void run()
	{	
		int exitCode = 0;
		try {
			Thread t1 = null,t1Stock = null, t1Spare = null,clientThread = null,mpServerThread = null,mpClientThread = null,mpProcessDataThread = null;
			DataHandler dh = null,dhStock = null,dhSpare = null;
			WindDataClient windDataClient = null;
			MsgPackLiteDataClient mpDataClient = null;
			ProcessMsgPackLiteData mpProcessData = null;
			
	
			if(cascading || mpCascading) {
				if(cascading) {
					windDataClient = new WindDataClient();
					clientThread = new Thread(windDataClient,"windDataClient");
					clientThread.start();
				}
				if(mpCascading) 
				{
					mpDataClient = new MsgPackLiteDataClient();
					mpClientThread = new Thread(mpDataClient,"MsgPackLiteDataClient");
					mpClientThread.start();
					
					mpProcessData = new ProcessMsgPackLiteData(msgpackQueue);
					mpProcessDataThread = new Thread(mpProcessData,"ProcessMsgPack");
					mpProcessDataThread.start();
				}
			} else {
				if(windMFServerIP != null && windMFServerIP != "") {			
					demo = new Demo(windMFServerIP, windMFServerPort, windMFServerUserId, windMFServerUserPwd , merchandiseTypeFlags, this , dedicatedWindThread,windMFWholeMarket,windMFMarkets,windMFWindReconnect);
					dh = new DataHandler (demo);  // 用來做 demo 的 斷線 reconnect
					t1 = new Thread(dh,"windMerchandise");
					t1.start();
				}
				
				if(windSFServerIP != null && windSFServerIP != "") {				
					demoStock = new Demo(windSFServerIP, windSFServerPort , windSFServerUserId, windSFServerUserPwd , stockTypeFlags, this,dedicatedWindThread,windSFWholeMarket,windSFMarkets,windSFWindReconnect);
					dhStock = new DataHandler (demoStock);  // 用來做 demo 的 斷線 reconnect
					t1Stock = new Thread(dhStock,"windFutureAndStock");
					t1Stock.start();
				}
				
				if(windSSServerIP != null && windSSServerIP != "") {				
					demoSpare = new Demo(windSSServerIP, windSSServerPort , windSSServerUserId, windSSServerUserPwd , stockTypeFlags, this,dedicatedWindThread,windSSWholeMarket,windSSMarkets,windSSWindReconnect);
					dhSpare = new DataHandler (demoSpare);  // 用來做 demo 的 斷線 reconnect
					t1Spare = new Thread(dhSpare,"windStockSpare");
					t1Spare.start();
				}				
			}
			
			if(msgPackLiteServer != null) {
				mpServerThread = new Thread(msgPackLiteServer,"MsgPackLiteServer");
				mpServerThread.start();
			}
	
		
			EventLoopGroup bossGroup = null;
			EventLoopGroup workerGroup = null;
			
			if(autoTermination >= 0) {
				log.info("will Auto Terminated at : " + autoTermination);
			}
			
			try
			{				
				Calendar cal;
				cal = Calendar.getInstance();
				int st = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
				int ct = st;
				
				
				if(serverPort != 0) {
					bossGroup   = new NioEventLoopGroup(2);
					workerGroup = new NioEventLoopGroup(16);
					windGatewayInitializer = new WindGatewayInitializer();
					ServerBootstrap  bootstrap = new ServerBootstrap()
					.group(bossGroup,workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)			
					.childHandler(windGatewayInitializer);
					
									
					if(autoTermination < 0) {
						bootstrap.bind(serverPort).sync().channel().closeFuture().sync();
					} else {
						ChannelFuture cf = bootstrap.bind(serverPort);
						while(st == autoTermination && ct == st) {
							Thread.sleep(1000);
							cal = Calendar.getInstance();
							ct = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
						}
						
						do {
							Thread.sleep(1000);
							cal = Calendar.getInstance();
							ct = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
						} while(ct != autoTermination);
						exitCode = 2;
						cf.channel().close();					
					}
					if(msgPackLiteServer != null) {
						msgPackLiteServer.stop();
						msgPackLiteServer = null;
						ThreadJoin(mpServerThread);
					}					
				} else {
					if(autoTermination < 0) {
						Thread.sleep(3000);  // sleep 一下,等 MsgPackServer 的 Thread 啟動
					} else {
						while(st == autoTermination && ct == st) {
							Thread.sleep(1000);
							cal = Calendar.getInstance();
							ct = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
						}
						
						do {
							Thread.sleep(1000);
							cal = Calendar.getInstance();
							ct = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);							
						} while(ct != autoTermination);						
						exitCode = 2;
						if(msgPackLiteServer != null) {
							msgPackLiteServer.stop();
							msgPackLiteServer = null;
						}
					}
					ThreadJoin(mpServerThread);	
				}
					
			}
			finally
			{
	
	
				if(demo != null) {			
					dh.Stop();
					demo.Stop();
					ThreadJoin(t1);
					log.info(t1.getName() + " Terminated.");
				}
				
				if(demoStock != null) {
					dhStock.Stop();
					demoStock.Stop();					
					ThreadJoin(t1Stock);
					log.info(t1Stock.getName() + " Terminated.");		
				}
				
				if(demoSpare != null) {				
					dhSpare.Stop();
					demoSpare.Stop();
					ThreadJoin(t1Spare);
					log.info(t1Spare.getName() + " Terminated.");		
				}				
				
				if(clientThread != null) {
					windDataClient.stop();
					ThreadJoin(clientThread);
				}
				if(mpClientThread != null) {
					mpDataClient.stop();
					ThreadJoin(mpClientThread);
				}		
				if(mpProcessDataThread != null) {
					mpProcessData.stop();
					ThreadJoin(mpProcessDataThread);
				}
				
				if(bossGroup != null) {
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}
		} 
		catch(Exception e)
		{
			log.error("Exception at WindGateway " + e.getMessage(),e);
		}
		if(exitCode == 1) {			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("Exit Code : " + exitCode);
		System.exit(exitCode);		
	}
	
	
	public static void main(String[] args) throws InterruptedException
	{	
		
		DOMConfigurator.configure("conf/windGatewaylog4j.xml");
			
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
			log.info("OS : " + OS);
			System.loadLibrary("libtdfapi_jni.so");
			System.loadLibrary("libWHNetWork.so");
			System.loadLibrary("libTDFAPI_v2.5.so");
		}
		
		String current = null;
		try {
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		//System.setProperty("file.encoding","GBK");
	    log.info("Current dir : "+current);
	    log.info("Current CodePage : " + System.getProperty("file.encoding"));
	
        ApplicationContext context = 
	             new FileSystemXmlApplicationContext("conf/WindGateway.xml");
        if(context.containsBean("MsgPackLiteServer")) {
        	msgPackLiteServer = (MsgPackLiteServer)context.getBean("MsgPackLiteServer");        	
        }
        instance = (WindGateway)context.getBean("WindGateway");
        //Thread serverThread = new Thread(instance,"windDataServer");
        //serverThread.start();
        instance.run();
	}
		
}

class BuySellVolume {
	long lBuyVolume,lSellVolume,lUnclassifiedVolume;
	long lBuyTurnover,lSellTurnover,lUnclassifiedTurnover;
	
	public BuySellVolume() {
		Reset();
	}
	
	public BuySellVolume(TDF_TRANSACTION t) {
		Reset();
		Calculate(t);
		
	}
	
	public void Calculate(TDF_TRANSACTION t) {
		int bs = t.getBSFlag();
		if( bs == 66) { // Buy
			lBuyVolume   += t.getVolume();
			lBuyTurnover += t.getTurnover();
		} else if(bs == 83) { // Sell
			lSellVolume   += t.getVolume();
			lSellTurnover += t.getTurnover();			
		} else {
			lUnclassifiedVolume   += t.getVolume();
			lUnclassifiedTurnover += t.getTurnover();			
		}
		
	}
	
	public void Reset() {
		lBuyVolume = lSellVolume = lUnclassifiedVolume = 
		lBuyTurnover = lSellTurnover = lUnclassifiedTurnover = 0;
		
	}
}

class ProcessMsgPackLiteData implements Runnable {
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.ProcessMsgPackLiteData.class);
	
	LinkedBlockingQueue<Object> queue = null;
	boolean quitFlag = false;
	private long ticks;	
	
	public ProcessMsgPackLiteData(LinkedBlockingQueue<Object> q) {
		queue = q;
	}
	
	public void run() {		
		//Object msg;
		int maxq = 0;
		ArrayList<Object> msgList = new ArrayList<Object>();
		int cnt = 0,maxQueued = 0;
		while(!quitFlag) {
			try {
				/*
				do {
					msg = queue.poll();
					if(msg != null) {					
						ProcessMessage(msg);
					}
					if(System.currentTimeMillis() >= ticks + 5000) {
						ticks = System.currentTimeMillis();
						if(queue.size() > maxq) {
							log.info("Max Queue Size : " + queue.size());
							maxq = queue.size();
						}
					}					

				} while(msg != null);
				*/
				msgList.clear();
				//msgList.add(queue.take());  // 如果 Blocking 住的話,會有來不及傳送資料給 Client 的狀況.  所以改成 non-blocking
				cnt = queue.drainTo(msgList);
				if(cnt > 0) {
					for(Object msg : msgList) {
						ProcessMessage(msg);
					}
					if(cnt > maxQueued) {
						maxQueued = cnt;
						log.info("Data Client Max Queued : " + maxQueued);
					}						
				} else {
					Thread.sleep(5);
				}
			} catch (Exception e) {
				log.error(e.getMessage(),e);				
			}
		}		
	}
	
	private void ProcessMessage(Object arg1) {
		try {
			if(arg1 instanceof HashMap<?,?>) {
				@SuppressWarnings("unchecked")
				HashMap<Integer,Object> in = (HashMap<Integer,Object>)arg1;
				if(in != null) {
					MsgPackLiteDataClientHandler.processData(in,false);
					MsgPackLiteDataServerHandler.flushAllClientMsgPack();
				}
			} 
	    } finally {
	        ReferenceCountUtil.release(arg1);
	    }
	}
	
	public void stop() {
		quitFlag = true;
	}	
}

class CodeTable {
	String strMarket = "";
	int CodeDate;
	long codesHashCode;
	public ConcurrentHashMap<String, TDF_CODE> mapCode = new ConcurrentHashMap<String,TDF_CODE>();
	public HashMap<Integer, Object> mpCodeTableResult = null;
	
	public CodeTable(String m,int d,long h,HashMap<Integer, Object> p) {
		strMarket = m;
		CodeDate = d;
		codesHashCode = h;
		mpCodeTableResult = p;
	}
	
	public int CodeCount(){
		return mapCode.size();
	}
}

class FutureTurnover
{
    long TotalVolume = 0;
    public long lFTurnover = 0;

    public void Calculate(TDF_FUTURE_DATA f) 
    {
        if (f.getVolume() == 0)
        {
            TotalVolume = 0; 
            lFTurnover = 0;
            return;
        }
        else if (f.getVolume() > TotalVolume)
        {
            lFTurnover += f.getMatch() * (f.getVolume() - TotalVolume);
            TotalVolume = f.getVolume();
        }
    }
}