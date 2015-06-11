package com.cyanspring.adaptor.future.ctp.trader.client;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bridj.Pointer;
import org.bridj.StructObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.*;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary.THOST_TE_RESUME_TYPE;

public class TraderHelper {
	private final static Logger log = LoggerFactory.getLogger(TraderHelper.class);
	
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
	
	
	private static Map<String , AbstractTraderProxy> clientMaps = new ConcurrentHashMap<String , AbstractTraderProxy>();	
	
	
	static void registClient(String id, CtpTraderProxy client) {
		if ( (id != null) && (client != null ) ) {
			clientMaps.put(id, client);
		}	
	}
	
	static void handleRspMsg(String id, CThostFtdcRspInfoField rspInfo) {
		AbstractTraderProxy client = clientMaps.get(id);
		client.responseOnError(rspInfo);
	}
	
	static void fireEventChange(String id, StructObject event) {
		AbstractTraderProxy client = clientMaps.get(id);
		if ( event instanceof CThostFtdcRspUserLoginField ) {
			client.responseLogin((CThostFtdcRspUserLoginField) event);
			client.doReqSettlementInfoConfirm();
		}
		else if ( event instanceof CThostFtdcSettlementInfoConfirmField) {
			client.responseSettlementInfoConfirm((CThostFtdcSettlementInfoConfirmField) event);
		}		
		else if ( event instanceof CThostFtdcUserLogoutField ) {
			
		}
		else if ( event instanceof CThostFtdcInputOrderField) {
			client.responseOnOrder(event);
		}
		else if ( event instanceof CThostFtdcOrderField) {
			client.responseOnOrder(event);
		}
		else if ( event instanceof CThostFtdcTradeField ) {
			client.responseOnOrder(event);
		}
		else if ( event instanceof CThostFtdcInputOrderActionField ) {
			client.responseOnOrder(event);
		}
		else {
			
		}
	}
	
	static void notifyNetworkReady(String id) {
		AbstractTraderProxy client = clientMaps.get(id);
		client.doLogin();		
	}
	
	static String toGBKString(byte[] bytes) {
		try {
			return new String(bytes, "GB2312");
		} catch (UnsupportedEncodingException e) {
			log.info(e.getMessage());
		}
		return "";
	}

}
