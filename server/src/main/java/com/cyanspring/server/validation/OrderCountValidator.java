package com.cyanspring.server.validation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.PositionKeeper;

public class OrderCountValidator implements IOrderValidator {
	private static final Logger log = LoggerFactory
			.getLogger(OrderCountValidator.class);

	private int limit = 500;
	private Date currentDate;
	private Map<String, Integer> countMap = new HashMap<String, Integer>();

	@Autowired
	private PositionKeeper positionKeeper;
	
	private Date getToday() {
		Date now = Clock.getInstance().now();
		return TimeUtil.getOnlyDate(now);
	}
	
	public OrderCountValidator() {
		currentDate = getToday();
	}
	
	private boolean checkThreshold(String account) {
		Date now = getToday();
		if(!currentDate.equals(now)) {
			countMap.clear();
			countMap.put(account, 1);
			currentDate = now;
			return true;
		}
		
		Integer count = countMap.get(account);
		if(null == count) {
			countMap.put(account, 1);
			return true;
		} 
		
		if(count >= limit)
			return false;
		
		countMap.put(account, count+1);
		return true;
	}
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if(null == order) {
			String account = (String) map.get(OrderField.ACCOUNT.value());
			if(null == account)
				return;
			
			if(!checkThreshold(account)) {
				throw new OrderValidationException("Daily allowed number of orders exceeded limit " + limit,ErrorMessage.DAILY_ORDERS_EXCEED_LIMIT);
			}
			
		} else {
			if(!checkThreshold(order.getAccount())) {
				Double qty = (Double)map.get(OrderField.QUANTITY.value());
				if(null != qty && PriceUtils.GreaterThan(qty, order.getQuantity()))
					throw new OrderValidationException("Daily allowed number of orders exceeded limit " + limit + ", can only amend down qty",ErrorMessage.DAILY_ORDERS_EXCEED_LIMIT_CAN_AMEND_QTY);
			}
			
		}

	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
