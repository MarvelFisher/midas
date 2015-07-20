package com.cyanspring.sample.singleorder.stop;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class StopPriceAnalyzer extends AbstractPriceAnalyzer {

	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		Quote quote = strategy.getQuote();
		PriceInstruction pi = new PriceInstruction();
		if(PriceUtils.Equal(qtyInstruction.getAggresiveQty(), 0))
			return pi;
		
		double stopLossPrice = order.get(Double.TYPE, OrderField.STOP_LOSS_PRICE.value());
		if(PriceUtils.isZero(stopLossPrice)) {
			strategy.logError("Stop loss price is missing");
		}		
		
		if(order.getSide().isBuy() && 
		   PriceUtils.validPrice(quote.getAsk()) && 
		   PriceUtils.EqualLessThan(stopLossPrice, quote.getAsk()) || 
		   !order.getSide().isBuy() && 
		   PriceUtils.validPrice(quote.getBid()) && 
		   PriceUtils.EqualGreaterThan(stopLossPrice, quote.getBid()) ) {
			ExchangeOrderType exOrderType = ExchangeOrderType.defaultMap(order.getOrderType());
			double price;
			if(order.getOrderType().equals(OrderType.Market) && strategy.isSimMarketOrder()) {
				exOrderType = ExchangeOrderType.LIMIT;
				price = getSimMarketOrderPrice(strategy);			
			} else {
				price = exOrderType.equals(ExchangeOrderType.MARKET)?0.0:order.getPrice();
			}
			pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, 
					qtyInstruction.getAggresiveQty(), 
					exOrderType, strategy.getId()));
		}
		
		return pi;
	}

}
