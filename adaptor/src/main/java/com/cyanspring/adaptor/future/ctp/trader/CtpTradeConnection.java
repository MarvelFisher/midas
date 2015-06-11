package com.cyanspring.adaptor.future.ctp.trader;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.adaptor.future.ctp.trader.client.CtpTraderProxy;
import com.cyanspring.adaptor.future.ctp.trader.client.IChainListener;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;

public class CtpTradeConnection implements IDownStreamConnection, IChainListener {
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
	private AtomicLong serial = new AtomicLong(0);
	
	
	// client to delegate ctp
	private CtpTraderProxy client;
	
	public CtpTradeConnection(String id, String url, String broker, String conLog, String user, String password) {
		this.id = id;
		this.url = url;
		this.broker = broker;
		this.conLog = conLog;
		client = new CtpTraderProxy(id, url, broker, conLog, user, password);
	}
	
	@Override
	public void init() throws Exception {
		// TODO does login and initialization		
		client.init();
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
		return false;
	}

	class DownStreamSender implements IDownStreamSender {
		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			long sn = serial.getAndIncrement();
			serialToOrder.put(sn, order);			
			String snStr = String.valueOf(sn);			
			order.setClOrderId(snStr);
			client.newOrder(snStr, order);
			log.info("Send Order: " + snStr);
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			
		}

		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			client.cancelOrder(order);
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
		client.addListener(this);
		return this.downStreamSender;
	}

	@Override
	public void onState(boolean on) {
		this.listener.onState(on);
	}

	@Override
	public void onOrder(String orderId, ExecType type , OrdStatus status, String message) {
		log.info("onOrder: " + orderId + " Type: " + type + " Message: " + message);
		Long sn = Long.parseLong(orderId);
		ChildOrder order = serialToOrder.get(sn);
		if ( null == order ) {
			log.info("Order not found: " + sn);
			return;
		}
		order.setOrdStatus(status);
		this.listener.onOrder(type, order, null, message);
	}

	@Override
	public void onError(String orderId, String message) {
		
		
		this.listener.onError(null, message);
	}

	private long genSerialId() {
		AtomicLong id = new AtomicLong();
		return id.getAndIncrement();
	}

	@Override
	public void onError(String message) {		
		log.error("Response Error:" + message);
	}
}
