package com.cyanspring.adaptor.future.ctp.trader.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQryInvestorPositionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcQryOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcReqUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspUserLoginField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcSettlementInfoConfirmField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTraderApi;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary;
import com.cyanspring.adaptor.future.ctp.trader.generated.TraderLibrary.THOST_TE_RESUME_TYPE;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.ISymbolConverter;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;

public class CtpTraderProxy implements ILtsLoginListener {
	
	private final static Logger log = LoggerFactory.getLogger(CtpTraderProxy.class);
	
	private CThostFtdcTraderApi traderApi;
	private LtsTraderSpiAdaptor traderSpi;
	private String clientId;
	private String frontUrl;
	private String brokerId;
	private String user;
	private String password;
	private String traderFlow;
	
	private int FRONT_ID;
	private int SESSION_ID;
	private AtomicInteger ORDER_REF = new AtomicInteger();
	
	protected AtomicInteger seqId = new AtomicInteger();
	
	protected ILtsTraderListener tradeListener;
	
	private boolean ready = false;
	private ISymbolConverter symbolConverter;
	
	public CtpTraderProxy(String id, String frontUrl, String brokerId, String traderFlow, 
			String user, String password, ISymbolConverter symbolConverter) {
		this.clientId = id;
		this.frontUrl = frontUrl;
		this.brokerId = brokerId;
		this.user = user;
		this.password = password;
		this.traderFlow = traderFlow;
		this.symbolConverter = symbolConverter;
	}
	
	// connect to front	
	// join the thread
	public void init() {
		// setNativeLibraryFile
		//initNativeLibrary();
		
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
		tradeListener = null;
		seqId.set(0);
	}
	
	public boolean getState() {
		return ready;
	}
	
	public void newOrder(String sn, ChildOrder order) throws DownStreamException {
		byte priceType = 0;
		if ( ExchangeOrderType.MARKET == order.getType() ) {
			priceType = TraderLibrary.THOST_FTDC_OPT_AnyPrice;
		} else if ( ExchangeOrderType.LIMIT == order.getType() ) {
			priceType = TraderLibrary.THOST_FTDC_OPT_LimitPrice;
		} else {
			throw new DownStreamException("ExchangeOrderType not support: " + order);
		}
		byte direction = 0;
		if ( OrderSide.Buy == order.getSide() ) {
			direction = TraderLibrary.THOST_FTDC_D_Buy;
		} else if ( OrderSide.Sell == order.getSide() ) {
			direction = TraderLibrary.THOST_FTDC_D_Sell;
		} else {
			throw new DownStreamException("Order side not support: " + order);
		}
		
		Byte flag = order.get(Byte.class, OrderField.FLAG.value());
		if(null == flag)
			throw new DownStreamException("Flag is empty: " + order);
			
		
		CThostFtdcInputOrderField req = new CThostFtdcInputOrderField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		String symbol = order.getSymbol();
		if(null != symbolConverter)
			symbol = symbolConverter.convertDown(symbol);
		req.InstrumentID().setCString(symbol);
		//
		req.OrderRef().setCString(String.valueOf(sn));		
		req.OrderPriceType(priceType);
		req.Direction(direction);
		req.CombOffsetFlag().set(0, flag);
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
		// get from order		
		String orderRef = order.getClOrderId();
		String symbol = order.getSymbol();
		if(null != symbolConverter)
			symbol = symbolConverter.convertDown(symbol);
		
		CThostFtdcInputOrderActionField req = new CThostFtdcInputOrderActionField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		req.OrderRef().setCString(orderRef);
		req.FrontID(FRONT_ID);
		req.SessionID(SESSION_ID);
		req.ActionFlag(TraderLibrary.THOST_FTDC_AF_Delete);
		req.InstrumentID().setCString(symbol);
		
		traderApi.ReqOrderAction(Pointer.getPointer(req), getNextSeq());
	}
	
	public void cancelOrder( CtpOrderToCancel order ) {
		// get from order		
		String orderRef = order.getOrderRef();
		String symbol = order.getSymbol();
		int front = order.getFront();
		int session = order.getSession();
		
		CThostFtdcInputOrderActionField req = new CThostFtdcInputOrderActionField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		req.OrderRef().setCString(orderRef);
		req.FrontID(front);
		req.SessionID(session);
		req.ActionFlag(TraderLibrary.THOST_FTDC_AF_Delete);
		req.InstrumentID().setCString(symbol);
		
		traderApi.ReqOrderAction(Pointer.getPointer(req), getNextSeq());
	}
	
	private Thread workThread1;
	protected void connect() {
		if ( workThread1 == null ) {
			workThread1 = new Thread() {
				@Override
				public void run() {
					startThreadProcess();
				}
			};
			workThread1.setDaemon(true);
			workThread1.start();
		} else {
			log.info("Already connecting, disconnect first");
		}
	}
	
	private Thread workThread2;
	protected void disConnect() {
		if ( workThread2 == null ) {
			workThread2 = new Thread() {
				@Override
				public void run() {
					stopThreadProcess();
				}
			};
			workThread2.setDaemon(true);
			workThread2.start();
		} else {
			log.info("Already disconnected, connect first");
		}
	}
	
	protected void startThreadProcess() {
		Pointer<CThostFtdcTraderApi > pTraderApi = CThostFtdcTraderApi.CreateFtdcTraderApi(BridjUtils.stringToBytePointer(traderFlow));
		traderApi = pTraderApi.get();
		traderSpi = new LtsTraderSpiAdaptor();
		traderSpi.setCtpTraderProxy(this);
		if( tradeListener != null ) {
			traderSpi.addTraderListener(tradeListener);
		}
		traderSpi.addLoginListener(this);
		traderApi.RegisterSpi(Pointer.getPointer(traderSpi));
		traderApi.RegisterFront(BridjUtils.stringToBytePointer(frontUrl));
		traderApi.SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		traderApi.SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
		traderApi.Init();
		traderApi.Join();
	}
	
	protected void stopThreadProcess() {
		traderSpi.removeTraderListerner(tradeListener);
		traderSpi.removeLoginListener(this);
		traderApi.Release();
	}
	
	private boolean loginSend = false;
	protected void doLogin() {		
		CThostFtdcReqUserLoginField req = new CThostFtdcReqUserLoginField();
		req.BrokerID().setCString(brokerId);
		req.UserID().setCString(user);
		req.Password().setCString(password);
		Pointer<CThostFtdcReqUserLoginField> pReq = Pointer.getPointer(req);
		traderApi.ReqUserLogin(pReq, getNextSeq());
		
	}
	
	private boolean settlementInfoConfirmSend = false;
	protected void doReqSettlementInfoConfirm() {	
		CThostFtdcSettlementInfoConfirmField req = new CThostFtdcSettlementInfoConfirmField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		traderApi.ReqSettlementInfoConfirm(Pointer.getPointer(req), getNextSeq());
		log.info("Send SettlementInfoConfirm");
			
	}
	
	public void cancelHistoryOrder() {
		Thread work = new Thread() {
			public void run() {
				List<CtpOrderToCancel> order2Cancel = null;
				try {
					order2Cancel = doQryHistoryOrders();
					log.info("Get Orders to cancel " + order2Cancel);
				} catch (InterruptedException e) {
					log.info("Query History Order Exception: "  + e.getMessage());
				}
				for ( CtpOrderToCancel order : order2Cancel ) {
					cancelOrder(order);
				}
			}
		};
		work.start();
	}
	
	// get order can be cancelled
	private List<CtpOrderToCancel> doQryHistoryOrders() throws InterruptedException {
		final boolean qryEnd[] = new boolean[1];
		qryEnd[0] = false;	
		final List<CtpOrderToCancel> array = new Vector<CtpOrderToCancel>();
		traderSpi.addQryOrdListener(new ILtsQryOrderListener() {
			@Override
			public void onQryOrder(CThostFtdcOrderField field, boolean isLast) {
				if ( field != null ) {
					String symbol = field.InstrumentID().getCString();
					String ordRef = field.OrderRef().getCString();
					int front = field.FrontID();
					int session = field.SessionID();
					byte status = field.OrderStatus();
					if ( status == TraderLibrary.THOST_FTDC_OST_NoTradeQueueing ||
							status == TraderLibrary.THOST_FTDC_OST_Unknown ) {
						CtpOrderToCancel order = new CtpOrderToCancel(symbol, ordRef, front, session, status);
						array.add(order);
					}
				}
				if ( isLast ) {
					qryEnd[0] = true;
				}
			}			
		});
		final CThostFtdcQryOrderField req = new CThostFtdcQryOrderField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);	
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int ret = traderApi.ReqQryOrder(Pointer.getPointer(req), getNextSeq());
				log.info("Send QueryOrder: " + ret);
			}			
		};	
		Timer timer = new Timer() ;
		timer.schedule(task, 2000);
		int timeout = 100;
		while( (!qryEnd[0]) && (timeout != 0) ) {
			Thread.sleep(50);
			timeout--;
		}
		
		return array;
	}
	
	protected void doQryPosition() {
		final CThostFtdcQryInvestorPositionField req = new CThostFtdcQryInvestorPositionField();
		req.BrokerID().setCString(brokerId);
		req.InvestorID().setCString(user);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int ret = traderApi.ReqQryInvestorPosition(Pointer.getPointer(req), getNextSeq());
				log.info("Send QueryPosition: " + ret);
			}			
		};	
		Timer timer = new Timer() ;
		timer.schedule(task, 1000);
	}
	
	public void addTradeListener(ILtsTraderListener listener) throws DownStreamException {
		if(listener != null && this.tradeListener != null) {
			throw new DownStreamException("Support only one listener");	
		}
				
		this.tradeListener = listener;
		if ( traderSpi != null ) {
			traderSpi.addTraderListener(listener);
		}
	}
	
	public synchronized int getNextSeq() {
		return seqId.incrementAndGet();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	public int getFRONT_ID() {
		return FRONT_ID;
	}

	public int getSESSION_ID() {
		return SESSION_ID;
	}

	public int getORDER_REF() {
		return ORDER_REF.getAndIncrement();
	}
	
	@Override
	public void onLogin(CThostFtdcRspUserLoginField field) {
		FRONT_ID = field.FrontID();
		SESSION_ID = field.SessionID();
		int nextOrderRef = field.MaxOrderRef().getInt();
		ORDER_REF = new AtomicInteger(++nextOrderRef) ;
		log.info("FRONT_ID = " + FRONT_ID + "; SESSION_ID = " + SESSION_ID + "; ORDER_REF = " + ORDER_REF);
	}

	@Override
	public void onDisconnect() {
		loginSend = false;
	}
	
}
