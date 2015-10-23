/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 * designed for CachingManager
 */
public class BasicRCParentOrderUpdateLocalEvent extends AsyncEvent {

private static final long serialVersionUID = 1L;
	
	private Map<String, ParentOrder> orderMap;
	
	public BasicRCParentOrderUpdateLocalEvent(Map<String, ParentOrder> orderMap) {
		this.orderMap = orderMap;
	}
	
	public Map<String, ParentOrder> getOrderMap() {
		return orderMap;
	}

}
