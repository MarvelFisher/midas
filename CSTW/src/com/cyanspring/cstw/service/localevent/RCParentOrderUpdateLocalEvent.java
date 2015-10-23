package com.cyanspring.cstw.service.localevent;

import java.util.Map;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/07
 *
 */
public class RCParentOrderUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 879191569943210763L;

	private Map<String, ParentOrder> orderMap;
	
	private UserRole roleType;
	
	public RCParentOrderUpdateLocalEvent(UserRole roleType) {
		this.roleType = roleType;
	}
	
	public Map<String, ParentOrder> getOrderMap() {
		return orderMap;
	}

	public void setOrderMap(Map<String, ParentOrder> orderMap) {
		this.orderMap = orderMap;
	}

	public UserRole getRoleType() {
		return roleType;
	}
	
}
