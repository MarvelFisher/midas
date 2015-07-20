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
	
	@Override
	public PriceInstruction calculate( QuantityInstruction qtyInstruction, 
			SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		PriceInstruction pi = new PriceInstruction();
		if(PriceUtils.Equal(qtyInstruction.getAggresiveQty(), 0))
			return pi;
		
		OrderType orderType = order.getOrderType();
		double price = 0.0;
		
		if(orderType.equals(OrderType.Market) && strategy.isSimMarketOrder()) {
			price = getSimMarketOrderPrice(strategy);
			if(PriceUtils.validPrice(price))
				pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getAggresiveQty(), 
						ExchangeOrderType.LIMIT, strategy.getId()));
			
		} else {
			ExchangeOrderType exOrderType = ExchangeOrderType.defaultMap(orderType);
			price = order.getPrice();
			pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getAggresiveQty(), 
					exOrderType, strategy.getId()));
		}			

		return pi;
	}
	
}
