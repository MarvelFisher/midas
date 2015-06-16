package com.cyanspring.adaptor.future.ctp.trader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.client.CtpTraderProxy;
import com.cyanspring.adaptor.future.ctp.trader.client.ILtsTraderListener;
import com.cyanspring.adaptor.future.ctp.trader.client.TraderHelper;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInputOrderActionField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;
import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcTradeField;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.ISymbolConverter;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;

public class CtpTradeConnection implements IDownStreamConnection, ILtsTraderListener {
	private static final Logger log = LoggerFactory
			.getLogger(CtpTradeConnection.class);

	private String url;
	private String conLog;
	private String id;
	private String broker;
	private boolean state; 
	private IDownStreamListener listener;
	private DownStreamSender downStreamSender = new DownStreamSender();
	
	private Map<Long, ChildOrder> serialToOrder = new ConcurrentHashMap<Long, ChildOrder>();
	
	// client to delegate ctp
	private CtpTraderProxy proxy;
	
	public CtpTradeConnection(String id, String url, String broker, String conLog, String user, String password, ISymbolConverter symbolConverter) {
		this.id = id;
		this.url = url;
		this.broker = broker;
		this.conLog = conLog;
		proxy = new CtpTraderProxy(id, url, broker, conLog, user, password, symbolConverter);
	}
	
	@Override
	public void init() throws Exception {
		// TODO does login and initialization		
		proxy.init();
	}
	

	@Override
	public void uninit() {
		// TODO clean up

	}

	@Override
	public String getId() {
		return id;
	}

//	private void onOrder() {
//		listener.onOrder(execType, order, execution, message);
//	}
	
	@Override
	public boolean getState() {
		return proxy.getState();
	}

	class DownStreamSender implements IDownStreamSender {
		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			order = order.clone();
			long sn = proxy.getORDER_REF();
			serialToOrder.put(sn, order);			
			String snStr = String.valueOf(sn);			
			order.setClOrderId(snStr);
			proxy.newOrder(snStr, order);
			log.info("Send Order: " + snStr);
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			
		}

		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			proxy.cancelOrder(order);
			log.info("Cancel Order: " + order.getClOrderId());
		}

		@Override
		public boolean getState() {
			return CtpTradeConnection.this.getState();
		}
		
	}
	
	@Override
	public IDownStreamSender setListener(IDownStreamListener listener)
			throws DownStreamException {
		
		if(listener != null && this.listener != null) {
			throw new DownStreamException("Support only one listener");
		}
		// lintner chain
		this.listener = listener;
		proxy.addListener(this);
		return this.downStreamSender;
	}

	
	private long genSerialId() {
		AtomicLong id = new AtomicLong();
		return id.getAndIncrement();
	}

	public void onError(String message) {		
		log.error("Response Error:" + message);
	}

	@Override
	public void onConnectReady(boolean isReady) {
		proxy.setReady(isReady);
		this.listener.onState(isReady);	
	}
	
	/**
	 * Notify Order Status except TradeStatus
	 */
	@Override
	public void onOrder(CThostFtdcOrderField order) {
		String orderId = order.OrderRef().getCString();
		byte statusCode = order.OrderStatus();
		int volumeTraded = order.VolumeTraded();
		String msg = TraderHelper.toGBKString(order.StatusMsg().getBytes());
		OrdStatus status = TraderHelper.convert2OrdStatus(statusCode);
		ExecType execType = TraderHelper.OrdStatus2ExecType(status);		
		log.info("onOrder: " + orderId + " Type: " + status + "Volume: " + volumeTraded + " Message: " + msg );
		
		Long sn = Long.parseLong(orderId);
		ChildOrder childOrder = serialToOrder.get(sn);
		if ( null == childOrder ) {
			log.info("Order not found: " + sn);
			return;
		}
		if ( status != null ) {
			childOrder.setOrdStatus(status);
			double volTraded = childOrder.getCumQty();
			if ( !TraderHelper.isTradedStatus(statusCode) ) {				
				this.listener.onOrder(execType, childOrder, null, msg);
			}
			
		}
	}
	
	/**
	 * Notify Trade
	 */
	@Override
	public void onTrade(CThostFtdcTradeField trade) {
		log.info("Traded:");
		
	}

	@Override
	public void onCancel(CThostFtdcInputOrderActionField field) {
		log.info("Cancelled:");
	}

	@Override
	public void onError(String orderId, String msg) {
		log.error("Response Error On Order:" + orderId + " " + msg);
		Long sn = Long.parseLong(orderId);
		ChildOrder order = serialToOrder.get(sn);	
		if ( null == order ) {
			log.info("Order not found: " + sn);
			return;
		}
		this.listener.onError(order.getId(), msg);	
	}

	
}
