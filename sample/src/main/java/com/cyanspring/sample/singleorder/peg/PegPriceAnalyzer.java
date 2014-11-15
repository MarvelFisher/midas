package com.cyanspring.sample.singleorder.peg;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class PegPriceAnalyzer extends AbstractPriceAnalyzer {

	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		Quote quote = strategy.getQuote();
		PriceInstruction pi = new PriceInstruction();
		if(PriceUtils.Equal(qtyInstruction.getAggresiveQty(), 0))
			return pi;
		
		double price = order.getSide().isBuy()?quote.getBid():quote.getAsk();

		if(PriceUtils.validPrice(price)) {
			pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, 
					qtyInstruction.getAggresiveQty(), 
					ExchangeOrderType.LIMIT, strategy.getId()));
		}
		
		return pi;
	}

}
