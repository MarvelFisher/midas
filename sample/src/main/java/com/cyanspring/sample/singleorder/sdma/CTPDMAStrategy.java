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

	@Override
	public ChildOrder createChildOrder(String parentId, String symbol, OrderSide side, double quantity, double price, ExchangeOrderType type) {
		ChildOrder order = super.createChildOrder(parentId, symbol, side, quantity, price, type);
		if(parentOrder.getOrderType().equals(OrderType.Market))
			order.put(OrderField.TIF.value(), TimeInForce.FILL_OR_KILL);
		
		return order;
	}
	
}
