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
	private Map<String, ChildOrder> localOrders = new ConcurrentHashMap<>();
	private Map<String, ChildOrder> exchangeOrders = new ConcurrentHashMap<>();
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
						if (TimeUtil.getTimePass(now, updated) >= checkIntrval && state) {
							log.info("Connection is timeout, id: " + id + ", exchangeAccount: " + exchangeAccount);
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
		listener.onState(state);
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
				log.warn("Down stream connection not ready");
				listener.onOrder(ExecType.REJECTED, order, null, "Down stream connection not ready");
				return;
			}
			localOrders.put(order.getId(), order);
			int side = WrapOrderSide.valueOf(order.getSide()).getCode();
			int type = WrapOrderType.valueOf(ExchangeOrderType.toOrderType(order.getType())).getCode();
			NewOrderRequest request = NewOrderRequest.newBuilder()
					.setOrderId(order.getId())
					.setClOrderId(order.getClOrderId())
					.setCreated(TimeUtil.formatDate(order.getCreated(), "yyyy-MM-dd HH:mm:ss.SSS"))
					.setExchangeAccount(exchangeAccount)
					.setOrderSide(side).setOrderType(type)
					.setPrice(order.getPrice())
					.setQuantity(order.getQuantity())
					.setSymbol(order.getSymbol())
					.setObjectType(WrapObjectType.NewOrderRequest.getCode())
					.setTxId(IdGenerator.getInstance().getNextID())
					.setTimeInForce(WrapTimeInForce.valueOf(order.get(TimeInForce.class, OrderField.TIF.value())).getCode())
					.build();
			order.setOrdStatus(OrdStatus.PENDING_NEW);
			downStreamEventSender.sendRemoteEvent(request, WrapObjectType.NewOrderRequest);
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			log.info("Amend order: " + order.getId());
			ChildOrder local = exchangeOrders.get(order.getExchangeOrderId());
			if(!checkOrderStatus(order, local))
				return;
			log.info(order.getId() + "->" + order.getExchangeOrderId());
			Builder request = AmendOrderRequest.newBuilder()
					.setObjectType(WrapObjectType.AmendOrderRequest.getCode())
					.setOrderId(order.getExchangeOrderId())
					.setExchangeAccount(exchangeAccount);
			Double qty = (Double) fields.get(OrderField.QUANTITY.value());		
			if (qty != null && PriceUtils.EqualGreaterThan(qty, 0))
				request.setQuantity(qty);
			Double price = (Double) fields.get(OrderField.PRICE.value());
			if (price != null && PriceUtils.EqualGreaterThan(price, 0))
				request.setQuantity(price);
			request.setTxId(IdGenerator.getInstance().getNextID());
			local.setOrdStatus(OrdStatus.PENDING_REPLACE);
			downStreamEventSender.sendRemoteEvent(request.build(), WrapObjectType.AmendOrderRequest);
		}
		
		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			log.info("Cancel order: " + order.getId());
			ChildOrder local = exchangeOrders.get(order.getExchangeOrderId());
			if(!checkOrderStatus(order, local))
				return;
			log.info(order.getId() + "->" + order.getExchangeOrderId());
			CancelOrderRequest request = CancelOrderRequest.newBuilder()
					.setObjectType(WrapObjectType.CancelOrderRequest.getCode())
					.setOrderId(order.getExchangeOrderId())
					.setExchangeAccount(exchangeAccount)
					.setTxId(IdGenerator.getInstance().getNextID())
					.build();
			local.setOrdStatus(OrdStatus.PENDING_CANCEL);
			downStreamEventSender.sendRemoteEvent(request, WrapObjectType.CancelOrderRequest);
		}
		
		private boolean checkOrderStatus(ChildOrder order, ChildOrder local) {
			if (!state) {
				listener.onOrder(ExecType.REJECTED, order, null, "Down stream connection not ready");
				return false;
			}
			if (order.getExchangeOrderId() == null) {
				log.error("null exchange order id, id: " + order.getId());
				listener.onOrder(ExecType.REJECTED, order, null, "null exchange order id");
				return false;
			}
			
			if (local == null) {
				log.error("Can't locate order, id: " + order.getId());
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
		if (this.state != update.getOnline())
			this.setState(update.getOnline());
	}
	
	private void onNewOrderReply(NewOrderReply reply) throws Exception {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		if (!reply.getResult()) {
			ChildOrder order = getChildOrderFromLocal(reply.getOrderId());
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
		}
	}
	
	private void onAmendOrderReply(AmendOrderReply reply) throws Exception {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		if (!reply.getResult()) {
			ChildOrder order = getChildOrderFromLocal(reply.getOrderId());
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
		}
	}
	
	private void onCancelOrderReply(CancelOrderReply reply) throws Exception {
		if (!checkExchangeAccount(reply.getExchangeAccount()))
			return;
		if (!reply.getResult()) {
			ChildOrder order = getChildOrderFromLocal(reply.getOrderId());
			log.info("order: " + reply.getOrderId() + ", msg" + reply.getMessage());
			listener.onOrder(ExecType.REJECTED, order, null, reply.getMessage());
		}
	}
	
	private void onOrderUpdate(OrderUpdate update) throws Exception {
		if (!checkExchangeAccount(update.getExchangeAccount()))
			return;
		ChildOrder order = exchangeOrders.get(update.getExchangeOrderId());
		if (order == null){
			order = localOrders.get(update.getOrderId());
			if (order == null){
				log.error("Can't find order, id: " + update.getOrderId() + ", exchange id: " + update.getExchangeOrderId());
				return;
			}
			if (update.getExchangeOrderId() != null) {
				order.setExchangeOrderId(update.getExchangeOrderId());
				exchangeOrders.put(update.getExchangeOrderId(), order);			
			}
		}
		ExecType type = WrapExecType.valueOf(update.getExecType()).getCommonExecType();
		OrdStatus status = WrapOrdStatus.valueOf(update.getOrdStatus()).getCommonOrdStatus();
		log.info("Order update, type:" + type + ", status: " + status + 
				", id:" + update.getOrderId() + ", exchangeOrderId: " + update.getExchangeOrderId());
		
		double delta = update.getCumQty() - order.getCumQty();
		if (PriceUtils.GreaterThan(delta, 0)) {
			order.setOrdStatus(status);
			double price = (update.getAvgPx() * update.getCumQty() - order.getAvgPx() * order.getCumQty()) / delta;
			order.setCumQty(update.getCumQty());
			order.setAvgPx(update.getAvgPx());
			Execution exe = new Execution(order.getSymbol(), order.getSide(), delta,
					price, order.getId(), order.getParentOrderId(), 
					order.getStrategyId(), IdGenerator.getInstance()
                    .getNextID() + "E",
					order.getUser(), order.getAccount(), order.getRoute());
			listener.onOrder(type, order, exe, update.getMsg());
		} else if (PriceUtils.Equal(delta, 0)) {
			order.setOrdStatus(status);
			listener.onOrder(type, order, null, update.getMsg());
		} else {
			log.error("Wrong order qty, order id: " + update.getOrderId() + ", type: " + type + ", status: " + status);
			return;
		}
	}
	
	private boolean checkExchangeAccount(String exchangeAccount) {
		if (this.exchangeAccount == null)
			return true;
		return this.exchangeAccount.equals(exchangeAccount);
	}
	
	private ChildOrder getChildOrderFromLocal(String id) throws Exception {
		ChildOrder order = localOrders.get(id);
		if (order == null)
			throw new Exception("Order not found");
		return order;
	}
}
