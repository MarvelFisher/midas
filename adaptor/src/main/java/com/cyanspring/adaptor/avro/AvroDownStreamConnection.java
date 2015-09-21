package com.cyanspring.adaptor.avro;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.avro.AvroSerializableObject;
import com.cyanspring.avro.generate.base.StateUpdate;
import com.cyanspring.avro.generate.trade.bean.AmendOrderReply;
import com.cyanspring.avro.generate.trade.bean.AmendOrderRequest;
import com.cyanspring.avro.generate.trade.bean.AmendOrderRequest.Builder;
import com.cyanspring.avro.generate.trade.bean.CancelOrderReply;
import com.cyanspring.avro.generate.trade.bean.CancelOrderRequest;
import com.cyanspring.avro.generate.trade.bean.NewOrderReply;
import com.cyanspring.avro.generate.trade.bean.NewOrderRequest;
import com.cyanspring.avro.generate.trade.bean.OrderUpdate;
import com.cyanspring.avro.wrap.WrapExecType;
import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.avro.wrap.WrapOrdStatus;
import com.cyanspring.avro.wrap.WrapOrderSide;
import com.cyanspring.avro.wrap.WrapOrderType;
import com.cyanspring.avro.wrap.WrapTimeInForce;
import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.TimeInForce;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public class AvroDownStreamConnection implements IDownStreamConnection, IObjectListener {
	private static final Logger log = LoggerFactory.getLogger(AvroDownStreamConnection.class);
	private String id = "avro";
	private String exchangeAccount;
	private boolean state;
	private IDownStreamSender avroDSSender = new AvroDownStreamSender();
	private IDownStreamEventSender downStreamEventSender;
	private IDownStreamListener listener;
	private Map<String, ChildOrder> orders = new ConcurrentHashMap<>();
	private Thread stateCheck;
	private long checkIntrval = 5000;
	private Date updated;
	private boolean stop = false;
	
	@Override
	public void init() throws Exception {
		downStreamEventSender.setEventListener(this);
		downStreamEventSender.init();
		updated = Clock.getInstance().now();
		stateCheck = new Thread() {
			@Override
			public void run() {
				try {
					while(!stop) {
						Thread.sleep(checkIntrval);
						Date now = Clock.getInstance().now();
						if (TimeUtil.getTimePass(now, updated) >= checkIntrval) {
							log.info("Connection is down, id: " + id + ", exchangeAccount: " + exchangeAccount);
							state = false;
						}
					}					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};
		stateCheck.start();
	}

	@Override
	public void uninit() {
		try {
			stop = true;
			downStreamEventSender.uninit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean getState() {
		return state;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getExchangeAccount() {
		return exchangeAccount;
	}

	public void setExchangeAccount(String exchangeAccount) {
		this.exchangeAccount = exchangeAccount;
	}

	public void setDownStreamEventSender(IDownStreamEventSender downStreamEventSender) {
		this.downStreamEventSender = downStreamEventSender;
	}

	public long getCheckIntrval() {
		return checkIntrval;
	}

	public void setCheckIntrval(long checkIntrval) {
		this.checkIntrval = checkIntrval;
	}

	@Override
	public IDownStreamSender setListener(IDownStreamListener listener)
			throws DownStreamException {
		this.listener = listener;
		return avroDSSender;
	}
	
	private class AvroDownStreamSender implements IDownStreamSender {

		@Override
		public boolean getState() {
			return AvroDownStreamConnection.this.getState();
		}

		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			log.info("New order: " + order.getId());
			if (!state) {
				log.warn("Down stram connection not ready");
				return;
			}
			String txId = IdGenerator.getInstance().getNextID();
			orders.put(txId, order);
			int side = WrapOrderSide.valueOf(order.getSide()).getCode();
			int type = WrapOrderType.valueOf(ExchangeOrderType.toOrderType(order.getType())).getCode();
			NewOrderRequest request = NewOrderRequest.newBuilder().setOrderId(order.getId())
					.setClOrderId(order.getClOrderId())
					.setCreated(TimeUtil.formatDate(order.getCreated(), "yyyy-MM-dd HH:mm:ss.SSS"))
					.setExchangeAccount(exchangeAccount).setOrderSide(side).setOrderType(type)
					.setPrice(order.getPrice()).setQuantity(order.getQuantity()).setSymbol(order.getSymbol())
					.setObjectType(WrapObjectType.NewOrderRequest.getCode()).setTxId(txId)
					.setTimeInForce(WrapTimeInForce.valueOf(order.get(TimeInForce.class, OrderField.TIF.value())).getCode())
					.build();
			downStreamEventSender.sendRemoteEvent(request, WrapObjectType.NewOrderRequest);
			order.setOrdStatus(OrdStatus.PENDING_NEW);
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			log.info("Amend order: " + order.getId());
			if (!state) {
				log.warn("Down stram connection not ready");
				return;
			}
			ChildOrder local = orders.remove(order.getExchangeOrderId());
			if(!checkOrderStatus(order, local))
				return;
			String txId = IdGenerator.getInstance().getNextID();
			orders.put(txId, local);
			Builder request = AmendOrderRequest.newBuilder()
					.setObjectType(WrapObjectType.AmendOrderRequest.getCode()).setOrderId(order.getClOrderId())
					.setExchangeAccount(exchangeAccount);
			Double qty = (Double) fields.get(OrderField.QUANTITY.value());		
			if (qty != null && PriceUtils.EqualGreaterThan(qty, 0))
				request.setQuantity(qty);
			Double price = (Double) fields.get(OrderField.PRICE.value());
			if (price != null && PriceUtils.EqualGreaterThan(price, 0))
				request.setQuantity(price);
			request.setTxId(txId);
			downStreamEventSender.sendRemoteEvent(request.build(), WrapObjectType.AmendOrderRequest);
			local.setOrdStatus(OrdStatus.PENDING_REPLACE);
		}
		
		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			log.info("Cancel order: " + order.getId());
			if (!state) {
				log.warn("Down stram connection not ready");
				return;
			}
			ChildOrder local = orders.remove(order.getExchangeOrderId());
			if(!checkOrderStatus(order, local))
				return;
			String txId = IdGenerator.getInstance().getNextID();
			orders.put(txId, local);
			CancelOrderRequest request = CancelOrderRequest.newBuilder()
					.setObjectType(WrapObjectType.CancelOrderRequest.getCode()).setOrderId(order.getClOrderId())
					.setExchangeAccount(exchangeAccount).setTxId(txId).build();
			downStreamEventSender.sendRemoteEvent(request, WrapObjectType.CancelOrderRequest);
			local.setOrdStatus(OrdStatus.PENDING_CANCEL);
		}
		
		private boolean checkOrderStatus(ChildOrder order, ChildOrder local) {
			if (local == null) {
				log.error("Can't locate order, id: " + order.getClOrderId());
				listener.onOrder(ExecType.REJECTED, order, null, "Can't locate order");
				return false;
			}
			
			if (local.getOrdStatus().isCompleted()) {
				listener.onOrder(ExecType.REJECTED, order, null, "Order completed");
				return false;
			}
			return true;
		}
	}

	@Override
	public void onMessage(Object obj) {
		try {
			AvroSerializableObject deObj = (AvroSerializableObject)obj;
			WrapObjectType type = deObj.getObjectType();
			if (type.equals(WrapObjectType.StateUpdate)) {
				onStateUpdate((StateUpdate)deObj.getRecord());
			} else if (type.equals(WrapObjectType.NewOrderReply)){
				onNewOrderReply((NewOrderReply)deObj.getRecord());
			} else if (type.equals(WrapObjectType.AmendOrderReply)) {
				onAmendOrderReply((AmendOrderReply)deObj.getRecord());
			} else if (type.equals(WrapObjectType.CancelOrderReply)) {
				onCancelOrderReply((CancelOrderReply)deObj.getRecord());
			} else if (type.equals(WrapObjectType.OrderUpdate)) {
				onOrderUpdate((OrderUpdate)deObj.getRecord());
			} else {
				log.error("Unhandle event type: " + type.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void onStateUpdate(StateUpdate update) {
		if (!checkExchangeAccount(update.getExchangeAccount()))
			return;
		updated = Clock.getInstance().now();
		state = update.getOnline();
		exchangeAccount = update.getExchangeAccount();
	}
	
	private void onNewOrderReply(NewOrderReply reply) {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		ChildOrder order = orders.remove(reply.getTxId());
		if (reply.getResult()) {
			listener.onOrder(ExecType.NEW, order, null, reply.getMessage());
		} else {
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
		}
		orders.put(order.getId(), order); // not get exchange order id yet
	}
	
	private void onAmendOrderReply(AmendOrderReply reply) {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		ChildOrder order = orders.remove(reply.getTxId());
		if (reply.getResult()) {
			listener.onOrder(ExecType.REPLACE, order, null, reply.getMessage());
		} else {
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
		}
		orders.put(order.getExchangeOrderId(), order);
	}
	
	private void onCancelOrderReply(CancelOrderReply reply) {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		ChildOrder order = orders.remove(reply.getTxId());
		if (reply.getResult()) {
			listener.onOrder(ExecType.CANCELED, order, null, reply.getMessage());
		} else {
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
			orders.put(order.getExchangeOrderId(), order);
		}
	}
	
	private void onOrderUpdate(OrderUpdate update) throws Exception {
		if (!checkExchangeAccount(update.getExchangeAccount()))
			return;
		ChildOrder order = orders.get(update.getExchangeOrderId());
		if (order == null){
			order = orders.remove(update.getOrderId());
			if (order == null)
				throw new Exception("Order not found");
			orders.put(update.getExchangeOrderId(), order);
		}
		if (order.getExchangeOrderId() == null)
			order.setExchangeOrderId(update.getExchangeOrderId());
		ExecType type = WrapExecType.valueOf(update.getExecType()).getCommonExecType();
		OrdStatus status = WrapOrdStatus.valueOf(update.getOrdStatus()).getCommonOrdStatus();
		double comQty = update.getCumQty() - order.getCumQty();
		if (PriceUtils.GreaterThan(comQty, 0)) {
			double price = (update.getAvgPx() * update.getCumQty() - order.getAvgPx() * order.getCumQty()) / comQty;
			order.setCumQty(update.getCumQty());
			order.setAvgPx(update.getAvgPx());
			Execution exe = new Execution(order.getSymbol(), order.getSide(), comQty,
					price, order.getId(), order.getParentOrderId(), 
					order.getStrategyId(), IdGenerator.getInstance()
                    .getNextID() + "E",
					order.getUser(), order.getAccount(), order.getRoute());
			listener.onOrder(type, order, exe, update.getMsg());
		} else if (PriceUtils.Equal(comQty, 0)) {
			listener.onOrder(type, order, null, update.getMsg());
		} else {
			log.error("Wrong order qty, order id: " + update.getOrderId() + ", type: " + type + ", status: " + status);
			return;
		}
		order.setOrdStatus(status);
	}
	
	private boolean checkExchangeAccount(String exchangeAccount) {
		if (this.exchangeAccount == null)
			return true;
		return this.exchangeAccount.equals(exchangeAccount);
	}
}
