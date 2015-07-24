package com.cyanspring.adaptor.future.wind.gateway;

import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.Network.Transport.FDTFields;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import cn.com.wind.td.tdf.*;

public class WindGateway implements Runnable {
	
	private static WindGatewayInitializer windGatewayInitializer = null; 
	public static ConcurrentHashMap<String,TDF_FUTURE_DATA> mapFutureData = new ConcurrentHashMap<String,TDF_FUTURE_DATA>(); 
	public static ConcurrentHashMap<String,TDF_MARKET_DATA> mapMarketData = new ConcurrentHashMap<String,TDF_MARKET_DATA>();
	public static ConcurrentHashMap<String,TDF_INDEX_DATA>  mapIndexData  = new ConcurrentHashMap<String,TDF_INDEX_DATA>();
	public static ConcurrentHashMap<String,BuySellVolume>   mapBuySell = new ConcurrentHashMap<String,BuySellVolume>();
	public static ConcurrentHashMap<String,TDF_TRANSACTION> mapTransaction = new ConcurrentHashMap<String,TDF_TRANSACTION>();
	public static ConcurrentHashMap<String,ConcurrentHashMap<String,TDF_CODE>> mapCodeTable = new ConcurrentHashMap<String,ConcurrentHashMap<String,TDF_CODE>>();
	

	


	public static WindGateway instance = null;
	private static int stockTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_TRANSACTION | DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	private static int merchandiseTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_FUTURE_CX | DATA_TYPE_FLAG.DATA_TYPE_INDEX;
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.WindGateway.class);
	
	private int serverPort;
	private String windMFServerIP = "114.80.154.34";
	private int windMFServerPort = 10050;
	private String windMFServerUserId = "TD1001888002";
	private String windMFServerUserPwd = "35328058";
	
	private String windSFServerIP = "114.80.154.34";
	private int windSFServerPort = 10051;
	private String windSFServerUserId = "TD1001888001";
	private String windSFServerUserPwd = "62015725";
	
	public static boolean cascading = false;
	public static String upstreamIp = "202.55.14.140";
	public static int upstreamPort = 10049;
	
	public static boolean mpCascading = false;
	public static String mpUpstreamIp = "202.55.14.140";
	public static int mpUpstreamPort = 10048;
	
	public static MsgPackLiteServer msgPackLiteServer = null;
	public static boolean dedicatedWindThread = false;
	Demo demo = null,demoStock = null;
	
	
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
	static public HashMap<Integer,Object> publishFutureChangesToMap(TDF_FUTURE_DATA dirty,TDF_FUTURE_DATA data) {	
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
		if(dirty == null || dirty.getPrefix() != data.getPrefix())
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
		if(transactionData == null) {
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
		if(windGatewayInitializer != null && WindGatewayHandler.isRegisteredByClient(symbol)) {		
			String str = publishFutureChanges(data,futureData);
			publishWindData(str,symbol);
		}
		if(MsgPackLiteDataServerHandler.isRegisteredByClient(symbol)) {			
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishFutureChangesToMap(data,futureData), symbol);
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
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishMarketDataChangesToMap(data,marketData), symbol);
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
			MsgPackLiteDataServerHandler.sendMssagePackToAllClientByRegistration(publishIndexDataChangesToMap(data,indexData), symbol);
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
	
	public void receiveCodeTable(String strMarket,TDF_CODE[] codes) {	
		ConcurrentHashMap<String,TDF_CODE> mapCode;
		if(mapCodeTable.containsKey(strMarket))
		{
			mapCode = mapCodeTable.get(strMarket);
		}	else	{
			mapCode = new ConcurrentHashMap<String,TDF_CODE>();
			mapCodeTable.put(strMarket, mapCode);
		}
		try {
			if(codes != null) {
					mapCode.clear();
					for(TDF_CODE code : codes) {			
						String wc = code.getWindCode();
						mapCode.put(wc,code);
						/*
						String cnName = code.getCNName();
						byte[] cn = code.getCNName().getBytes("UTF8");
						String result = new String(cnName.getBytes(),"GB2312");
						if(result != null) {
							result = null;
						}
						if(cn != null) {
							cn = null;
						}
						*/						
					}			
			}
		} catch(Exception e)  {
			
		}
		
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
						receiveCodeTable(market,null);
					}
				}
			}
		}
	}
	
	public void convertMarketsMP(ArrayList<String> markets) {

		for(String market : markets) {
			if(market != null && market.isEmpty() == false) {
				receiveCodeTable(market,null);
			}
		}
	}	
	
	public void requestSymbol(String sym) {
		if(demoStock != null)
		{
			demoStock.AddRequest(new WindRequest(WindRequest.Subscribe,sym.toUpperCase()));
		}	
	}
	public void requestSymbolMF(String sym) {
		if(demo != null)
		{
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
	
	public void run()
	{	
		try {
			//Demo demo = null,demoStock = null;
			Thread t1 = null,t2 = null,t1Stock = null,t2Stock = null,clientThread = null,mpServerThread = null,mpClientThread = null;
			DataWrite dw = null,dwStock = null;
			WindDataClient windDataClient = null;
			MsgPackLiteDataClient mpDataClient = null;
			
	
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
				}
			} else {
				if(windMFServerIP != null && windMFServerIP != "")
				{
					demo = new Demo(windMFServerIP, windMFServerPort, windMFServerUserId, windMFServerUserPwd , merchandiseTypeFlags, this , dedicatedWindThread);
					DataHandler dh = new DataHandler (demo);
					t1 = new Thread(dh,"windMerchandise");
					t1.start();
					dw = new DataWrite (demo);   // 用來做 demo 的 斷線 reconnect
					t2 = new Thread ( dw , "windMerchandiseConnectionCheck" );
					t2.start();
				}
				
				if(windSFServerIP != null && windSFServerIP != "")
				{
					demoStock = new Demo(windSFServerIP, windSFServerPort , windSFServerUserId, windSFServerUserPwd , stockTypeFlags, this,dedicatedWindThread);
					DataHandler dhStock = new DataHandler (demoStock);
					t1Stock = new Thread(dhStock,"windFutureAndStock");
					t1Stock.start();
					dwStock = new DataWrite (demoStock);   // 用來做 demo 的 斷線 reconnect
					t2Stock = new Thread ( dwStock,"windStockConnectionCheck" );
					t2Stock.start();
				}
			}
			
			if(msgPackLiteServer != null) {
				mpServerThread = new Thread(msgPackLiteServer,"MsgPackLiteServer");
				mpServerThread.start();
			}
	
		
			EventLoopGroup bossGroup   = new NioEventLoopGroup(2);
			EventLoopGroup workerGroup = new NioEventLoopGroup(16);
			
			try
			{
				if(serverPort != 0) {
					windGatewayInitializer = new WindGatewayInitializer();
					ServerBootstrap  bootstrap = new ServerBootstrap()
					.group(bossGroup,workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)			
					.childHandler(windGatewayInitializer);
				
					bootstrap.bind(serverPort).sync().channel().closeFuture().sync();
				}
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
				if(mpClientThread != null) {
					mpDataClient.stop();
					mpClientThread.join();				
				}				
				if(mpServerThread != null) {
					msgPackLiteServer.stop();
					mpServerThread.join();
				}
				
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();			
			}
		} 
		catch(Exception e)
		{
			log.error("Exception at WindGateway " + e.getMessage(),e);
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException
	{	
		DOMConfigurator.configure("conf/windGatewaylog4j.xml");
			
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) {
			log.info("OS : " + OS);
			System.load("libWHNetWork.so");
			System.load("libTDBAPI_v2.so");
		}
		
		String current = null;
		try {
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	    log.info("Current dir : "+current);		
	
        ApplicationContext context = 
	             new FileSystemXmlApplicationContext("conf/WindGateway.xml");
        if(context.containsBean("MsgPackLiteServer")) {
        	msgPackLiteServer = (MsgPackLiteServer)context.getBean("MsgPackLiteServer");
        }
        instance = (WindGateway)context.getBean("WindGateway");
        Thread serverThread = new Thread(instance,"windDataServer");
        serverThread.start();
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
