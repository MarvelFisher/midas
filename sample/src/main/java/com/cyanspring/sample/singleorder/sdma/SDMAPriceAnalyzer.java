/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.sample.singleorder.sdma;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

/**
 * @author dennis
 *
 */
public class SDMAPriceAnalyzer extends AbstractPriceAnalyzer {
	private boolean supportMarketOrderType;
	private boolean aggressiveWithTime;
	private int aggressiveTicks = 5;
	private int retryCount = 0;
	
	@Override
	public PriceInstruction calculate( QuantityInstruction qtyInstruction, 
			SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		PriceInstruction pi = new PriceInstruction();
		if(PriceUtils.Equal(qtyInstruction.getAggresiveQty(), 0))
			return pi;
		
		OrderType orderType = order.getOrderType();
		double price = 0.0;
		
		if(supportMarketOrderType) {
			ExchangeOrderType exOrderType = ExchangeOrderType.defaultMap(orderType);
			price = order.getPrice();
			pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getAggresiveQty(), 
					exOrderType, strategy.getId()));
		} else {
			if(orderType.equals(OrderType.Market)) {
				Quote quote = strategy.getAdjQuote();
				if(null == quote)
					return pi;
				if(order.getSide().equals(OrderSide.Buy)) {
					price = quote.getAsk();
					if(!PriceUtils.validPrice(price)) { //if no ask get on top of depth
						price = quote.getBid();
						price = strategy.getTickTable().tickUp(price, false);
					} else if(aggressiveWithTime && retryCount > 0) {
						price = strategy.getTickTable().tickUp(price, retryCount * aggressiveTicks, false);
					}
				} else {
					price = quote.getBid();
					if(!PriceUtils.validPrice(price)) { //if no bid get on top of depth
						price = quote.getAsk();
						price = strategy.getTickTable().tickDown(price, false);
					} else if(aggressiveWithTime && retryCount > 0) {
						price = strategy.getTickTable().tickDown(price, retryCount * aggressiveTicks, false);
					}
				}
				retryCount++;
			} else {
				price = order.getPrice();
			}
			if(PriceUtils.validPrice(price))
				pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getAggresiveQty(), 
						ExchangeOrderType.LIMIT, strategy.getId()));
		}
			
		return pi;
	}

	public boolean isSupportMarketOrderType() {
		return supportMarketOrderType;
	}

	public void setSupportMarketOrderType(boolean supportMarketOrderType) {
		this.supportMarketOrderType = supportMarketOrderType;
	}

	public boolean isAggressiveWithTime() {
		return aggressiveWithTime;
	}

	public void setAggressiveWithTime(boolean aggressiveWithTime) {
		this.aggressiveWithTime = aggressiveWithTime;
	}

	public int getAggressiveTicks() {
		return aggressiveTicks;
	}

	public void setAggressiveTicks(int aggressiveTicks) {
		this.aggressiveTicks = aggressiveTicks;
	}
	
	
}
