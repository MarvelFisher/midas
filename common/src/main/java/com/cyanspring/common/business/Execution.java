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
package com.cyanspring.common.business;

import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;

public class Execution extends BaseOrder {
	private static final long serialVersionUID = 1L;

	public Execution(String symbol, OrderSide side, double quantity,
			double price, String orderId, String parentOrderId, 
			String strategyId, String execId,
			String user, String account, String route) throws OrderException {
		super(symbol, side, quantity, price);
		if (PriceUtils.EqualLessThan(quantity, 0)) {
			throw new OrderException("Execution quantity must great than zero");
		}
		put(OrderField.ORDER_ID.value(), orderId);
		put(OrderField.PARENT_ORDER_ID.value(), parentOrderId);
		put(OrderField.STRATEGY_ID.value(), strategyId);
		put(OrderField.EXECID.value(), execId);
		put(OrderField.USER.value(), user);
		put(OrderField.ACCOUNT.value(), account);
		this.setRoute(route);
	}

	private Execution() {
		super();
	}
	
	@Override
	protected String generateId() {
		return IdGenerator.getInstance().getNextID() + "E";
	}

	public String getOrderId() {
		return get(String.class, OrderField.ORDER_ID.value());
	}

	public String getParentOrderId() {
		return get(String.class, OrderField.PARENT_ORDER_ID.value());
	}

	public String getExecId() {
		return get(String.class, OrderField.EXECID.value());
	}

	public String getStrategyId() {
		return get(String.class, OrderField.STRATEGY_ID.value());
	}
	
	public Execution clone() {
		return (Execution)super.clone();
	}


	public void setOrderId(String orderId) {
		put(OrderField.ORDER_ID.value(), orderId);
	}

	public void setParentOrderId(String parentOrderId) {
		put(OrderField.PARENT_ORDER_ID.value(), parentOrderId);
	}

	public void setStrategyId(String strategyId) {
		put(OrderField.STRATEGY_ID.value(), strategyId);
	}
	
	
	public void setExecId(String execId) {
		put(OrderField.EXECID.value(), execId);
	}
	
	public double toPostion() {
		return getSide().isBuy()?getQuantity():-getQuantity();
	}
}
