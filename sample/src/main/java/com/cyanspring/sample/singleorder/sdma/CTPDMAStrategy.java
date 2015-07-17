package com.cyanspring.sample.singleorder.sdma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.order.UpdateChildOrderEvent;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.TimeInForce;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;


public class CTPDMAStrategy extends SingleOrderStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(CTPDMAStrategy.class);

	private int maxCancelRetry = 3;
	private int cancelRetry = 0;
	@Override
	protected Logger getLog() {
		return log;
	}

	@Override
	public void init() throws StrategyException {
		if(!parentOrder.getOrderType().equals(OrderType.Market))
			setQuoteRequired(false);
		
		setTimerEventRequired(false);
		super.init();
	}

	protected void postProcessUpdateChildOrderEvent(UpdateChildOrderEvent event) {
		executionManager.processUpdateChildOrderEvent(event);
		ChildOrder order = event.getOrder();
		if(order.getOrdStatus().equals(OrdStatus.CANCELED) && order.isUnsolicited() && cancelOnCancel) {
			log.debug("postProcessUpdateChildOrderEvent: " + parentOrder.getOrderType() + "," + cancelRetry + "," + maxCancelRetry);
			if(!parentOrder.getOrderType().equals(OrderType.Market) || cancelRetry >= maxCancelRetry) {
				log.debug("Received unsolicited cancel on child order, cancelling parent order: " + parentOrder);
				parentOrder.setOrdStatus(OrdStatus.CANCELED);
				parentOrder.touch();
				terminate();
				return;
			} else {
				cancelRetry++;
			}
			return;
		} else if(order.getOrdStatus().equals(OrdStatus.REJECTED) && rejectOnReject) {
			parentOrder.setOrdStatus(OrdStatus.REJECTED);
			parentOrder.touch();
			terminate();
			return;
		}
		
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}

	@Override
	public ChildOrder createChildOrder(String parentId, String symbol, OrderSide side, double quantity, double price, ExchangeOrderType type) {
		ChildOrder order = super.createChildOrder(parentId, symbol, side, quantity, price, type);
		if(parentOrder.getOrderType().equals(OrderType.Market))
			order.put(OrderField.TIF.value(), TimeInForce.FILL_OR_KILL);
		
		return order;
	}
	
	public int getMaxCancelRetry() {
		return maxCancelRetry;
	}

	public void setMaxCancelRetry(int maxCancelRetry) {
		this.maxCancelRetry = maxCancelRetry;
	}	
	
}
