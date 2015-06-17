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
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.ISymbolConverter;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;

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
	
	private Map<String, ChildOrder> serialToOrder = new ConcurrentHashMap<String, ChildOrder>();
	private Map<String, Double> tradePendings = new ConcurrentHashMap<String, Double>();
	private Map<String, Double> orderPendings = new ConcurrentHashMap<String, Double>();
	
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
			String ordRef = "" + proxy.getORDER_REF();
			serialToOrder.put(ordRef, order);			
			order.setClOrderId(ordRef);
			proxy.newOrder(ordRef, order);
			log.info("Send Order: " + ordRef);
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
	public void onOrder(CThostFtdcOrderField update) {
		log.debug("onOrder: " + update);
		String clOrderId = update.OrderRef().getCString();	
		byte statusCode = update.OrderStatus();
		int volumeTraded = update.VolumeTraded();
		String msg = TraderHelper.toGBKString(update.StatusMsg().getBytes());
		OrdStatus status = TraderHelper.convert2OrdStatus(statusCode);
		ExecType execType = TraderHelper.OrdStatus2ExecType(status);		
		log.info("onOrder: " + clOrderId + " Type: " + status + "Volume: " + volumeTraded + " Message: " + msg );
		
		ChildOrder childOrder = serialToOrder.get(clOrderId);
		if ( null == childOrder ) {
			log.info("Order not found: " + clOrderId);
			return;
		}

		if(status == OrdStatus.NEW && childOrder.getOrdStatus() == OrdStatus.NEW ||
		   status == OrdStatus.PENDING_NEW && childOrder.getOrdStatus() == OrdStatus.PENDING_NEW) {
			log.debug("Skipping update since ordStatus doesn't change: " + status);
			return;
		}

		if ( status != null ) {
			childOrder.setOrdStatus(status);
			if (PriceUtils.GreaterThan(volumeTraded, childOrder.getCumQty())) {
				tradePendings.put(clOrderId, new Double(volumeTraded)); // leave trade to do the update
			} else {				
				this.listener.onOrder(execType, childOrder, null, msg);
			}
			
		}
	}
	
	/**
	 * Notify Trade
	 */
	@Override
	public void onTrade(CThostFtdcTradeField trade) {
		log.info("Traded: " + trade);
		String clOrderId = trade.OrderRef().getCString();	
		ChildOrder order = serialToOrder.get(clOrderId);
		if ( null == order ) {
			log.info("Order not found: " + clOrderId);
			return;
		}
		Double volumeTraded = tradePendings.remove(clOrderId);
		if(null == volumeTraded) {
			log.error("Received trade without order update");
		} else if(!PriceUtils.Equal(volumeTraded, order.getCumQty() + trade.Volume())) {
			log.warn("Volume not match: " + volumeTraded + ", " + (order.getCumQty() + trade.Volume()));
		}
		
		if(trade.Volume() == 0 || PriceUtils.isZero(trade.Price())) {
			log.error("volume or price is 0");
			return;
		}
		
		double avgPx = (order.getAvgPx() * order.getCumQty() + trade.Volume() * trade.Price()) / (order.getCumQty() + trade.Volume());
		double volume = order.getCumQty() + trade.Volume();
		order.setCumQty(volume);
		order.setAvgPx(avgPx);
		ExecType execType = ExecType.PARTIALLY_FILLED;
		if(PriceUtils.Equal(volumeTraded, order.getQuantity())) {
			execType = ExecType.FILLED;
			order.setOrdStatus(OrdStatus.FILLED);
		} else {
			order.setOrdStatus(OrdStatus.PARTIALLY_FILLED);
		}

		Execution execution = new com.cyanspring.common.business.Execution(
                order.getSymbol(), order.getSide(), trade.Volume(),
                trade.Price(), order.getId(), order.getParentOrderId(),
                order.getStrategyId(), IdGenerator.getInstance()
                .getNextID() + "E", order.getUser(),
                order.getAccount(), order.getRoute());
		
		this.listener.onOrder(execType, order, execution, null);
	}

	@Override
	public void onCancel(CThostFtdcInputOrderActionField field) {
		log.info("Cancelled:");
	}

	@Override
	public void onError(String orderId, String msg) {
		log.error("Response Error On Order:" + orderId + " " + msg);
		ChildOrder order = serialToOrder.get(orderId);	
		if ( null == order ) {
			log.info("Order not found: " + orderId);
			return;
		}
		order.setOrdStatus(OrdStatus.REJECTED);
		this.listener.onOrder(ExecType.REJECTED, order, null, msg);
	}

	
}
