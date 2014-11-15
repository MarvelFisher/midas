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
package com.cyanspring.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.strategy.IExecutionAnalyzer;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderAction;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.OrderUtils;
import com.cyanspring.common.util.PriceUtils;

public class DefaultExecutionAnalyzer implements IExecutionAnalyzer {
	boolean priceAmendable = false;

	class QuantityAllocation extends PriceAllocation {
		ArrayList<ChildOrder> openOrders;

		public QuantityAllocation(String symbol, OrderSide side, double price,
				double qty, ExchangeOrderType orderType, String parentId,
				ArrayList<ChildOrder> openOrders) {
			super(symbol, side, price, qty, orderType, parentId);
			this.openOrders = openOrders;
		}

	}

	public ArrayList<ChildOrder> extractOrdersAtPrice(PriceAllocation pa,
			Collection<ChildOrder> openOrders) {
		ArrayList<ChildOrder> result = new ArrayList<ChildOrder>();
		for (ChildOrder order : openOrders) {
			if (pa.matches(order))
				result.add(order);
		}

		for (ChildOrder order : result) {
			openOrders.remove(order);
		}
		return result;
	}

	@Override
	public List<ExecutionInstruction> analyze(
			PriceInstruction priceInstruction, IStrategy strategy) {
		if(null == priceInstruction)
			return null;

		List<ExecutionInstruction> result = new ArrayList<ExecutionInstruction>();
//		ParentOrder parentOrder = strategy.getParentOrder();

		ArrayList<QuantityAllocation> existingAllocation = new ArrayList<QuantityAllocation>();

		Collection<ChildOrder> openOrders = strategy.getChildOrders();
		// add existing child orders(can be multiple) that matches the prices
		// defined in PriceInstruction
		for (Entry<String, TreeSet<PriceAllocation>> entry : priceInstruction.getAllocations().entrySet()) {
			for (PriceAllocation pa : entry.getValue()) {
				// if quantity is 0 at this price, it means we want to remove all child orders
				// at that price level
				if(PriceUtils.EqualLessThan(pa.getQty(), 0))
					continue;
				ArrayList<ChildOrder> cos = extractOrdersAtPrice(pa, openOrders);
				existingAllocation.add(new QuantityAllocation(pa.getSymbol(), pa.getSide(),
						pa.getPrice(), pa.getQty(), pa.getOrderType(), pa.getParentId(), cos));
			}
		}
		
		// cancel the open orders which prices are not in the price instruction
		for (ChildOrder order : openOrders) {
			result.add(new ExecutionInstruction(OrderAction.CANCEL, order, null));
		}

		// go through quantity allocations to work out order actions
		for (QuantityAllocation qa : existingAllocation) {
			// if found no existing child orders at this price level of
			// allocation, add new orders
			if (qa.openOrders == null || qa.openOrders.size() == 0) {
				ChildOrder child = strategy.createChildOrder(qa.getParentId(), qa.getSymbol(), 
						qa.getSide(), qa.getQty(), qa.getPrice(), qa.getOrderType());
				result.add(new ExecutionInstruction(OrderAction.NEW, child, null));
			}
			// work out what the actions it needs if there are some existing
			// allocations already
			else {
				double vol = 0;
				// when the sum of existing orders' remaining quantity is
				// greater than allocation
				Set<ChildOrder> sortedChildren = OrderUtils.getSortedOpenChildOrders(qa.openOrders);
				for (ChildOrder order : sortedChildren) {
					if (PriceUtils.EqualGreaterThan(vol,qa.getQty()))
						result.add(new ExecutionInstruction(OrderAction.CANCEL, order, null));
					else if (PriceUtils.GreaterThan(vol + order.getRemainingQty(), qa.getQty())) {// amend down
						double delta = vol + order.getRemainingQty()
								- qa.getQty();
						
						Map<String, Object> changeFields = new HashMap<String, Object>();
						changeFields.put(OrderField.QUANTITY.value(), order.getQuantity() - delta);
						result.add(new ExecutionInstruction(OrderAction.AMEND, order, changeFields));
					}

					vol += order.getRemainingQty();
				}

				// when the sum of existing order's remaining quantity is less
				// than allocation, send a new order
				if (PriceUtils.LessThan(vol, qa.getQty())) {
					ChildOrder child = strategy.createChildOrder(qa.getParentId(), qa.getSymbol(), qa.getSide(), qa.getQty()-vol, qa.getPrice(), qa.getOrderType());
					result.add(new ExecutionInstruction(OrderAction.NEW, child, null));
				}
			}
		}

		return result;
	}

}
