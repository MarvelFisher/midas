package com.cyanspring.adaptor.future.ctp.trader.client;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspInfoField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSettlementInfoConfirmField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradeField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderApi;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderSpi;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary.THOST_TE_RESUME_TYPE;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;

public class CtpTraderProxy extends AbstractTraderProxy {
	
	private final static Logger log = LoggerFactory.getLogger(CtpTraderProxy.class);
	
	private CThostFtdcTraderApi traderApi;
	private CThostFtdcTraderSpi traderSpi;
	private String clientId;
	private String frontUrl;
	private String brokerId;
	private String user;
	private String password;
	private String traderFlow;
	
	private int FRONT_ID;
	private int SESSION_ID;
	private int ORDER_REF;
	
	private boolean clientReady = false;
	
	
	public CtpTraderProxy(String id, String frontUrl, String brokerId, String traderFlow, String user, String password ) {
		this.clientId = id;
		this.frontUrl = frontUrl;
		this.brokerId = brokerId;
		this.user = user;
		this.password = password;
		this.traderFlow = traderFlow;
	}
	
	// connect to front	
	// join the thread
	public void init() {
		// setNativeLibraryFile
		initNativeLibrary();
		
		// mkdir traderFlow
		File conPath = new File(traderFlow);
		if ( !conPath.exists() ) {
			conPath.mkdirs();
		}
		// connect to front
		connect();
	}	
	
	public void unInit() {
		disConnect();
		traderSpi = null;
		traderApi = null;
		listener = null;
		seqId.set(0);
	}
	
	public void newOrder ( String sn, ChildOrder order ) throws DownStreamException {
		byte priceType = 0;
		if ( ExchangeOrderType.MARKET == order.getType() ) {
			priceType = TraderLibrary.THOST_FTDC_OPT_AnyPrice;
		} else if ( ExchangeOrderType.LIMIT == order.getType() ) {
			priceType = TraderLibrary.THOST_FTDC_OPT_LimitPrice;
		} else {
			throw new DownStreamException("No Recognized OrderType");
		}
		byte direction = 0;
		if ( OrderSide.Buy == order.getSide() ) {
			direction = TraderLibrary.THOST_FTDC_D_Buy;
		} else if ( OrderSide.Sell == order.getSide() ) {
			direction = TraderLibrary.THOST_FTDC_D_Sell;
		}
//		order.setClOrderId(String.valueOf(orderRef));
		
		CThostFtdcInputOrderField req = new CThostFtdcInputOrderField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		req.InstrumentID().setCString(order.getSymbol());
		//
		req.OrderRef().setCString(String.valueOf(sn));		
		req.OrderPriceType(priceType);
		req.Direction(direction);
		req.CombOffsetFlag().set(0, TraderLibrary.THOST_FTDC_OF_Open);
		req.CombHedgeFlag().set(0, TraderLibrary.THOST_FTDC_HF_Speculation);
		req.LimitPrice(order.getPrice());
		req.VolumeTotalOriginal((int) order.getQuantity());
		req.VolumeCondition(TraderLibrary.THOST_FTDC_VC_AV);
		req.TimeCondition(TraderLibrary.THOST_FTDC_TC_GFD);
		req.MinVolume(1);
		req.ContingentCondition(TraderLibrary.THOST_FTDC_CC_Immediately);
		req.ForceCloseReason(TraderLibrary.THOST_FTDC_FCC_NotForceClose);
		req.IsAutoSuspend(0);
		req.UserForceClose(0);
		
		int ret = traderApi.ReqOrderInsert(Pointer.getPointer(req), getNextSeq());
		if ( ret != 0 ) {
			throw new DownStreamException("Order Send Fail");
		}
		
	}
	
	public void amendOrder( ChildOrder order, Map<String, Object> fields ) throws DownStreamException {
		throw new DownStreamException("Not yet implement");
	}
	
	public void cancelOrder( ChildOrder order ) throws DownStreamException {
		
	}
	
	@Override
	protected void startThreadProcess() {
		Pointer<CThostFtdcTraderApi > pTraderApi = CThostFtdcTraderApi.CreateFtdcTraderApi(BridjUtils.stringToBytePointer(traderFlow));
		traderApi = pTraderApi.get();
		traderSpi = new LtsTraderSpi(clientId);
		traderApi.RegisterSpi(Pointer.getPointer(traderSpi));
		TraderHelper.registClient(((LtsTraderSpi)traderSpi).getConnectId(), this);
		traderApi.RegisterFront(BridjUtils.stringToBytePointer(frontUrl));
		traderApi.SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		traderApi.SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		traderApi.Init();
		traderApi.Join();
	}
	
	@Override
	protected void stopThreadProcess() {
		traderApi.Release();
	}
	
	private boolean loginSend = false;
	@Override
	protected void doLogin() {
		if ( !loginSend ) {
			CThostFtdcReqUserLoginField req = new CThostFtdcReqUserLoginField();
			req.BrokerID().setCString(brokerId);
			req.UserID().setCString(user);
			req.Password().setCString(password);
			Pointer<CThostFtdcReqUserLoginField> pReq = Pointer.getPointer(req);
			traderApi.ReqUserLogin(pReq, getNextSeq());
		}
		loginSend = true;
	}
	
	private boolean settlementInfoConfirmSend = false;
	protected void doReqSettlementInfoConfirm() {
		if ( !settlementInfoConfirmSend ) {
			CThostFtdcSettlementInfoConfirmField req = new CThostFtdcSettlementInfoConfirmField();
			req.BrokerID().setCString(brokerId);
			req.InvestorID().setCString(user);
			traderApi.ReqSettlementInfoConfirm(Pointer.getPointer(req), getNextSeq());
			log.info("Send SettlementInfoConfirm");
		}		
	}

	@Override
	protected void responseLogin(CThostFtdcRspUserLoginField event) {
		FRONT_ID = event.FrontID();
		SESSION_ID = event.SessionID();
		int iNextOrderRef = event.MaxOrderRef().getInt();
		ORDER_REF = iNextOrderRef++ ;
		log.info("FRONT_ID = " + FRONT_ID + "; SESSION_ID = " + SESSION_ID + "; ORDER_REF = " + ORDER_REF);
	}

	@Override
	protected void responseSettlementInfoConfirm(
			CThostFtdcSettlementInfoConfirmField event) {		
		clientReady = true;
			
		log.info("CTP Connection: " +  clientId + " Ready!");
		if ( listener != null ) {
			listener.onState(true);
		}		
	}
	
	@Override
	protected void responseOnOrder(StructObject event) {
		responseOnOrder(event, null);
	}
	
	@Override
	protected void responseOnOrder(StructObject event, CThostFtdcRspInfoField rspInfo) {
		if ( (event instanceof CThostFtdcInputOrderField) && (rspInfo != null) ) {
			CThostFtdcInputOrderField rsp = (CThostFtdcInputOrderField)event;
			String orderRef = rsp.OrderRef().getCString();
			
			listener.onError(orderRef, rspInfo.ErrorMsg().getCString());
		}
		else if ( event instanceof CThostFtdcOrderField) {
			CThostFtdcOrderField rsp = (CThostFtdcOrderField)event;
			String orderRef = rsp.OrderRef().getCString();
			byte status = rsp.OrderStatus();
			ExecType execType = CtpOrderStatus2ExecType(status);
			OrdStatus ltsOrdStatus = CtpOrderStatus2Lts(status);
			String msg = TraderHelper.toGBKString(rsp.StatusMsg().getBytes());
			log.info("CThostFtdcOrderField Status: " + msg);
			if ( listener != null ) {
				listener.onOrder(orderRef, execType, ltsOrdStatus, msg);
			}
		}
		else if ( event instanceof CThostFtdcTradeField) {
			CThostFtdcTradeField rsp = (CThostFtdcTradeField)event;
			String orderRef = rsp.OrderRef().getCString();
			ExecType execType = ExecType.FILLED;
			OrdStatus ltsOrdStatus = OrdStatus.FILLED;
			//String msg = TraderHelper.toGBKString(rsp.StatusMsg().getBytes());
			if ( listener != null ) {
				listener.onOrder(orderRef, execType, ltsOrdStatus, null);
			}
		}
		else if ( event instanceof CThostFtdcInputOrderActionField) {
			
		}
		
	}

	private ExecType CtpOrderStatus2ExecType(byte status) {
		ExecType execType = null;		
		switch (status) {
			case TraderLibrary.THOST_FTDC_OST_AllTraded: 
				execType = ExecType.FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedQueueing:
				execType = ExecType.PARTIALLY_FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedNotQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeNotQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_Canceled:
				execType = ExecType.CANCELED;
				break;
			case TraderLibrary.THOST_FTDC_OST_Unknown:
				execType = ExecType.PENDING_NEW;
				break;
			case TraderLibrary.THOST_FTDC_OST_NotTouched:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_Touched:
				
				break;
			default:
				execType = null;
		}
		return execType;
	}
	
	private OrdStatus CtpOrderStatus2Lts(byte status) {
		OrdStatus ltsOrdStatus = null;		
		switch (status) {
			case TraderLibrary.THOST_FTDC_OST_AllTraded: 
				ltsOrdStatus = OrdStatus.FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedQueueing:
				ltsOrdStatus = OrdStatus.PARTIALLY_FILLED;
				break;
			case TraderLibrary.THOST_FTDC_OST_PartTradedNotQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_NoTradeNotQueueing:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_Canceled:
				ltsOrdStatus = OrdStatus.CANCELED;
				break;
			case TraderLibrary.THOST_FTDC_OST_Unknown:
				ltsOrdStatus = OrdStatus.PENDING_NEW;
				break;
			case TraderLibrary.THOST_FTDC_OST_NotTouched:
				
				break;
			case TraderLibrary.THOST_FTDC_OST_Touched:
				
				break;
			default:
				ltsOrdStatus = null;
		}
		return ltsOrdStatus;
	}
	
	protected void responseOnError(CThostFtdcRspInfoField rspInfo) {		
		if ( rspInfo.ErrorID() != 0 ) {
			String msg = TraderHelper.toGBKString(rspInfo.ErrorMsg().getBytes());
			listener.onError(msg);
		}			
	}
	
	private void initNativeLibrary() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		if ( os.toLowerCase().contains("win") ) {
			if ( arch.contains("x64") ) {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\win64\\thosttraderapi.dll"));
			} else {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\win32\\thosttraderapi.dll"));
			}			
		} else if ( os.toLowerCase().contains("linux") ) {
			if ( arch.contains("x64") ) {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\linux64\\thosttraderapi.so"));
			} else {
				BridJ.setNativeLibraryFile("Trader", new File(".\\ctplib\\linux32\\thosttraderapi.so"));
			}
		}
	}
	
	public boolean getClientReady() {
		return clientReady;
	}

	public int getFRONT_ID() {
		return FRONT_ID;
	}

	public int getSESSION_ID() {
		return SESSION_ID;
	}

	public int getORDER_REF() {
		return ORDER_REF;
	}
	
}
