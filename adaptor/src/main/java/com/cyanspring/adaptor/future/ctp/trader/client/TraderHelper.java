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
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;

public class TraderHelper {
	private final static Logger log = LoggerFactory.getLogger(TraderHelper.class);	
	
	public static OrdStatus convert2OrdStatus(byte code) {
		OrdStatus ltsOrdStatus = null;		
		switch (code) {
			case TraderLibrary.THOST_FTDC_OST_AllTraded: 
				ltsOrdStatus = OrdStatus.FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedQueueing:
				ltsOrdStatus = OrdStatus.PARTIALLY_FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedNotQueueing:
				ltsOrdStatus = OrdStatus.PENDING_NEW;
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeQueueing:
				ltsOrdStatus = OrdStatus.NEW;
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeNotQueueing:
				ltsOrdStatus = OrdStatus.CANCELED;
				break;
			case TraderLibrary.THOST_FTDC_OST_Canceled:
				ltsOrdStatus = OrdStatus.CANCELED;
				break;
			case TraderLibrary.THOST_FTDC_OST_Unknown:
				ltsOrdStatus = OrdStatus.PENDING_NEW;
				break;
			case TraderLibrary.THOST_FTDC_OST_NotTouched:
				ltsOrdStatus = null;
				break;
			case TraderLibrary.THOST_FTDC_OST_Touched:
				ltsOrdStatus = null;
				break;
		}
		if(null == ltsOrdStatus)
			log.warn("Status not map: " + code);
		return ltsOrdStatus;
	}
	
	public static boolean isTradedStatus( byte code ) {
		if ( (code == TraderLibrary.THOST_FTDC_OST_AllTraded) || 
				(code == TraderLibrary.THOST_FTDC_OST_PartTradedQueueing) || 
				(code == TraderLibrary.THOST_FTDC_OST_PartTradedNotQueueing) ) {
			return true;
		}
		return false;
	}
	
	public static ExecType OrdStatus2ExecType( OrdStatus status ) {
		return ExecType.getType(status.value());
	}
	
	public static String toGBKString(byte[] bytes) {
		try {
			return new String(bytes, "GB2312");
		} catch (UnsupportedEncodingException e) {
			log.info(e.getMessage());
		}
		return "";
	}
	
	public static String toGBKString2(byte[] bytes) {
		try {
			return new String(bytes, "GBK");
		} catch (UnsupportedEncodingException e) {
			log.info(e.getMessage());
		}
		return "";
	}
	
	public static String genClOrderId(int front, int session, String orderRef) {		
		return "" + front + ":" + session + ":" + orderRef;
	}
	
	/**
	 * 获得Field
	 * 
	 * 对可能出现的null值做处理
	 * @param <T>
	 * @param field field的指针对象
	 * @return
	 */
	public static <T extends StructObject> T getStructObject(Pointer<T> field) {
		return field == null ? null : field.get();
	}

}
