package com.cyanspring.adaptor.future.ctp.trader.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.bridj.Pointer;
import org.bridj.StructObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderApi;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderSpi;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary.THOST_TE_RESUME_TYPE;

public class CtpTraderHelper {
	private final static Logger log = LoggerFactory.getLogger(CtpTraderHelper.class);
	
	private static CThostFtdcTraderApi traderApi;
	private static CThostFtdcTraderSpi traderSpi;
	private static AtomicInteger seqId;
	
	private static String FRONT_ADDR = "tcp://180.168.146.181:10200";
	private static String BROKER_ID = "0253";
	
	// Session Info
	private static int FRONT_ID;
	private static int SESSION_ID;
	private static String ORDER_REF;
	
	// test resource
	private static String INVESTOR_ID = "00071";
	private static String PASSWORD = "hkfdt1234";
	private static String INSTRUMENT_ID = "rb1510";
	private static byte DIRECTION = TraderLibrary.THOST_FTDC_D_Sell;
	private static double LIMIT_PRICE = 2350;
	
	
	private static boolean INITIALIZED = false;
	public static void init() {
		if( INITIALIZED ) {
			log.info("Already Initialized..");
			return;
		}		
		log.info("Initialising CTP Trader");
		seqId = new AtomicInteger();
		
		Pointer<CThostFtdcTraderApi> pApi = CThostFtdcTraderApi.CreateFtdcTraderApi(BridjUtils.stringToBytePointer(".\\trade_flow\\"));
		traderApi = pApi.get();
		traderSpi = new LtsTraderSpi();
		traderApi.RegisterSpi(Pointer.getPointer(traderSpi));
		traderApi.RegisterFront(BridjUtils.stringToBytePointer(FRONT_ADDR));
		traderApi.SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		traderApi.SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		log.info("Subscribe Private Topic:" + "QUICK");
		log.info("Subscribe Public Topic:" + "QUICK");
		
		traderApi.Init();		
	}
	
	
	public static int login(String username, String password) {
		CThostFtdcReqUserLoginField req = new CThostFtdcReqUserLoginField();
		req.BrokerID().setCString(BROKER_ID);
		req.UserID().setCString(username);
		req.Password().setCString(password);
		Pointer<CThostFtdcReqUserLoginField> pReq = Pointer.getPointer(req, CThostFtdcReqUserLoginField.class);	
		traderApi.ReqUserLogin(pReq, getNextSeq());
		
		return 0;
	}
	
	
	
	public CThostFtdcTraderApi getTraderApi() {
		return traderApi;
	}
	
	public CThostFtdcTraderSpi getTraderSpi() {
		return traderSpi;
	}
	
	public static int join() {
		return traderApi.Join();
	}
	
	public static void release() {
		traderApi.Release();
	}
	
	public static int getNextSeq() {
		return seqId.incrementAndGet();
	}
	
	static void handleRspMsg(CThostFtdcRspInfoField rspInfo) {
		
	}
	
	static void fireEventChange(StructObject event) {
		
	}

}
