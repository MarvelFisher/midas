package com.cyanspring.adaptor.future.wind.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.wind.td.tdf.*;
/*
import cn.com.wind.td.tdf.DATA_TYPE_FLAG;
import cn.com.wind.td.tdf.TDFClient;
import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_CODE_RESULT;
import cn.com.wind.td.tdf.TDF_CONNECT_RESULT;
import cn.com.wind.td.tdf.TDF_ERR;
import cn.com.wind.td.tdf.TDF_LOGIN_RESULT;
import cn.com.wind.td.tdf.TDF_MSG;
import cn.com.wind.td.tdf.TDF_MSG_DATA;
import cn.com.wind.td.tdf.TDF_MSG_ID;
import cn.com.wind.td.tdf.TDF_OPEN_SETTING;
import cn.com.wind.td.tdf.TDF_OPTION_CODE;
import cn.com.wind.td.tdf.TDF_PROXY_SETTING;
import cn.com.wind.td.tdf.TDF_PROXY_TYPE;
*/
//import demo.tdfapi.DataInfo;

public class Demo {
	private final  boolean outputToScreen = true;  
	/***********************configuration***************************************/
	private final String openMarket = ""; 
	private final int openData = 0;
	private final int openTime = 0;
	private final String subscription = "";
	private final int openTypeFlags = DATA_TYPE_FLAG.DATA_TYPE_ALL;
	private TDF_CONNECT_RESULT connectResult = null;
	private TDF_LOGIN_RESULT loginResult = null;
	private TDF_CODE_RESULT codeTableResult = null;
	private TDF_MARKET_CLOSE marketClose = null;
	private TDF_QUOTATIONDATE_CHANGE dateChange = null;
	private WindGateway windGateway = null;
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.Demo.class);	
	/***********************configuration***************************************/
	private String ip,username,password;
	private int port;
	TDFClient client = new TDFClient();
	
	public int getPort() {
		return this.port;
	}
	Demo(String ip, int port, String username, String password , WindGateway gateWay) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.LastPrintTime = System.currentTimeMillis();
		this.windGateway = gateWay;
		this.quitFlag = true;
		/*
		int err = reconnect();
		if( err != TDF_ERR.TDF_ERR_SUCCESS)
		{
			//System.out.printf("Can't connect to %s:%d. �����˳���\n", ip, port);
			//System.exit(err);			
		}
		*/
	}
	
	public int reconnect()
	{
		TDF_OPEN_SETTING setting = new TDF_OPEN_SETTING();
		setting.setIp(ip);
		setting.setPort( Integer.toString(port));
		setting.setUser(username);
		setting.setPwd(password);

		setting.setReconnectCount(99999999);
		setting.setReconnectGap(10);
		setting.setProtocol(0);
		setting.setMarkets(openMarket);
		setting.setDate(openData);
		setting.setTime(openTime);
		setting.setSubScriptions(subscription);
		setting.setTypeFlags(openTypeFlags);
		setting.setConnectionID(0);
		
		log.info("try to connect : " + this.ip + " " + this.port );
		int err = client.open(setting);
		if (err == TDF_ERR.TDF_ERR_SUCCESS) {
			this.quitFlag = false;
		}		
		else
		{
			String logstr = "Can't connect to " + ip + ":" + port  + " , err code : " + err;
			//System.out.println(logstr); 
			log.warn(logstr);
			client.close();
		}
		return err;
	}	
	Demo(String ip, int port, String username, String password,
			String proxy_ip, int proxy_port, String proxy_user, String proxy_pwd) {
		
		this.quitFlag = false;
		this.LastPrintTime = System.currentTimeMillis();
		TDF_OPEN_SETTING setting = new TDF_OPEN_SETTING();
		setting.setIp(ip);
		setting.setPort( Integer.toString(port));
		setting.setUser(username);
		setting.setPwd(password);

		setting.setReconnectCount(99999999);
		setting.setReconnectGap(10);
		setting.setProtocol(0);
		setting.setMarkets(openMarket);
		setting.setDate(openData);
		setting.setTime(openTime);
		setting.setSubScriptions(subscription);
		setting.setTypeFlags(openTypeFlags);
		setting.setConnectionID(0);
		
		TDF_PROXY_SETTING proxySetting = new TDF_PROXY_SETTING();
		proxySetting.setProxyHostIp(proxy_ip);
		proxySetting.setProxyPort(Integer.toString(proxy_port));
		proxySetting.setProxyUser(proxy_user);
		proxySetting.setProxyPwd(proxy_pwd);
		proxySetting.setProxyType(TDF_PROXY_TYPE.TDF_PROXY_HTTP11);		

		int err = client.openProxy(setting, proxySetting);
		if (err!=TDF_ERR.TDF_ERR_SUCCESS) {
			System.out.printf("Can't connect to %s:%d. �����˳���\n", ip, port);
			System.exit(err);
		}		
	}
	
	protected Boolean quitFlag;
	private long LastPrintTime;
	
	public void setQuitFlag(Boolean para){
		this.quitFlag = para;
	}
	public TDF_OPTION_CODE getOptionCodeInfo(String szWindCode)
	{
		return client.getOptionCodeInfo(szWindCode);
	}
	void printCodeTable() {		
		TDF_CODE[] codes = client.getCodeTable("CZC");		
		PrintHelper.printCodeTable(codes);		
	}
	void run() {
		int err;
		while (!quitFlag) {
			TDF_MSG msg = client.getMessage(10);
			if (msg==null)
				continue;
			
			switch(msg.getDataType()) {
			//ϵͳ��Ϣ
			case TDF_MSG_ID.MSG_SYS_HEART_BEAT :	
				//System.out.println("Heart Beat");
				if(windGateway != null)
				{
					windGateway.receiveHeartBeat();
				}					
				break;
			case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK:				
				System.out.println("NETWORK DISCONNECT");
				log.info("Receive Wind NETWORK DISCONNECT");
				quitFlag = true;
				break;
			case TDF_MSG_ID.MSG_SYS_CONNECT_RESULT:{
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				System.out.println("CONNECT RESULT");
				log.info("Receive Wind CONNECT RESULT");
				connectResult = data.getConnectResult();
				PrintHelper.printConnectResult(data.getConnectResult());
				break;
			}
			case TDF_MSG_ID.MSG_SYS_LOGIN_RESULT:{
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				loginResult = data.getLoginResult();
				PrintHelper.printLoginResult(data.getLoginResult());				
				break;
			}
			case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT:{
				System.out.println("CODE TABLE RESULT");
				log.info("Receive Wind CODE TABLE RESULT");
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				codeTableResult = data.getCodeTableResult();
				PrintHelper.printCodeTableResult(data.getCodeTableResult());
				if(windGateway != null)
				{
					for(String market : data.getCodeTableResult().getMarket())
					{
						windGateway.receiveCodeTable(market, client.getCodeTable(market));
					}
				}
				//printCodeTable();
				//err = client.setSubscription("AG1506.SHF", 1);
				//System.out.println("Subscription Result : " + err);
				break;
			}
			case TDF_MSG_ID.MSG_SYS_MARKET_CLOSE:{
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				marketClose = data.getMarketClose();
				//PrintHelper.printMarketClose(data.getMarketClose());
				if(windGateway != null)
				{
					windGateway.receiveMarketClose(marketClose);
				}				
				break;
			}
			case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {				
				TDF_MSG_DATA data = TDFClient.getMessageData(msg, 0);
				dateChange = data.getDateChange();
				//PrintHelper.printDateChange(data.getDateChange());
				if(windGateway != null)
				{
					windGateway.receiveQuotationDateChange(dateChange);
				}
				break;
			}
			//�����Ϣ
			case TDF_MSG_ID.MSG_DATA_MARKET:
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i); 
						//PrintHelper.printDataMarket(data.getMarketData());
						if(windGateway != null)
						{
							windGateway.receiveMarketData(data.getMarketData());
						}							
					}				
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printDataMarket(TDFClient.getMessageData(msg, 0).getMarketData());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()) );
				*/
								
				break;
			case TDF_MSG_ID.MSG_DATA_INDEX:				
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
						if(windGateway != null)
						{
							windGateway.receiveIndexData(data.getIndexData());
						}
						//PrintHelper.printIndexData(data.getIndexData());
					}					
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printIndexData(TDFClient.getMessageData(msg, 0).getIndexData());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()) );
				*/				
				break;
			case TDF_MSG_ID.MSG_DATA_FUTURE:
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
						//PrintHelper.printFutureData(data.getFutureData());
						if(windGateway != null)
						{
							windGateway.receiveFutureData(data.getFutureData());
						}						
					}					
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printFutureData(TDFClient.getMessageData(msg, 0).getFutureData());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()));
				*/				
				break;
			case TDF_MSG_ID.MSG_DATA_TRANSACTION:
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
						PrintHelper.printTransaction(data.getTransaction());
					}					
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printTransaction(TDFClient.getMessageData(msg, 0).getTransaction());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()) );
				*/				
				break;
			case TDF_MSG_ID.MSG_DATA_ORDERQUEUE:
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
						PrintHelper.printOrderQueue(data.getOrderQueue());
					}					
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printOrderQueue(TDFClient.getMessageData(msg, 0).getOrderQueue());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()) );
				*/				
				break;
			case TDF_MSG_ID.MSG_DATA_ORDER:
				if (outputToScreen){					
					for (int i=0; i<msg.getAppHead().getItemCount(); i++) {
						TDF_MSG_DATA data = TDFClient.getMessageData(msg, i);
						PrintHelper.printOrder(data.getOrder());
					}				
				}
				/*
				if(System.currentTimeMillis() - LastPrintTime  > 10 * 1000 && msg.getAppHead().getItemCount()>0){
					PrintHelper.printOrder(TDFClient.getMessageData(msg, 0).getOrder());
					System.gc();
					LastPrintTime = System.currentTimeMillis();
				}
				PrintHelper.SaveData( new DataInfo(msg,PrintHelper.GetCurrentTime()) );
				*/				
				break;
			default:
				break;
			}
		}
		client.close();
	}
			
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length!=4 ) {
			System.out.println("usage:  Demo ip port user password");
			System.exit(1);
		}
		// Proxy Mode
		//Demo d = new Demo("10.100.7.18", 10001, "dev_test", "dev_test", 
		//			"10.100.6.125", 3128, "", "");
		Demo demo = new Demo(args[0], Integer.parseInt(args[1]), args[2], args[3],null);
		DataHandler dh = new DataHandler (demo);
		Thread t1 = new Thread(dh);
		t1.start();	
		DataWrite dw = new DataWrite (demo);
		Thread t2 = new Thread ( dw );
		t2.start();
		System.out.println("press anything to quit the program.");
		try {
			System.in.read();	
			demo.setQuitFlag(true);
			dw.setQuitFlag(true);	
			t1.join();
			System.out.println("Thread1 Quit!");
			t2.join();
			System.out.println("Thread2 Quit!");
		} catch (Exception e) {
			e.printStackTrace();
		}		
		System.out.println("Quit the program!");
		System.exit(0);
	}
}





class DataHandler  implements Runnable {
	protected Demo demo;
	protected boolean quitFlag = false;
	
	private static final Logger log = LoggerFactory
			.getLogger(com.cyanspring.adaptor.future.wind.gateway.DataHandler.class);
	
	public DataHandler ( Demo  d) {
	    this.demo = d;
	}
	public void run ( ) {
		while(quitFlag == false)
		{
			try {
				demo.reconnect();
				while(demo.quitFlag == false)
				{
					demo.run();
				}
				log.warn("Disconnect with Wind , try to reconnect after 5 seconds. port : " +  demo.getPort());
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
}
class DataWrite  implements Runnable { //д���������ʱ����>WRITE_GAP && �����>LIST_LEN
	private static final int WRITE_GAP = 5;
	private static final int LIST_LEN = 20000;	
	
	public DataWrite ( Demo  d) {
		quitFlag = false;
		lastWriteTime = System.currentTimeMillis();
		this.demo = d;
	}
	private  Demo demo;
	protected Boolean quitFlag;	
	private long lastWriteTime;
	public void setQuitFlag(Boolean para){
		this.quitFlag = para;
		
	}
	public void run ( ) {
		while(!quitFlag ){
			if (System.currentTimeMillis() - lastWriteTime < WRITE_GAP * 1000){
				try{
				    Thread.sleep(2*1000);//�л������˳��ź�
				    continue;
				}catch(Exception e){
					e.printStackTrace();
					System.exit(0);
				}
			}
			if (PrintHelper.IsListFull(LIST_LEN)){
			    PrintHelper.WriteData(demo);
			}
			lastWriteTime = System.currentTimeMillis();
		}
		PrintHelper.WriteData(demo);
		PrintHelper.CloseDataFiles();
	}
}
