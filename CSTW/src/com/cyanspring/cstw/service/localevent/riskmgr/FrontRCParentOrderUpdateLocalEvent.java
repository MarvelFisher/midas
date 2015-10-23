package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/07
 * designed for CachingManager
 */
public class FrontRCParentOrderUpdateLocalEvent extends BasicRCParentOrderUpdateLocalEvent {

	private static final long serialVersionUID = 1L;

	public FrontRCParentOrderUpdateLocalEvent(Map<String, ParentOrder> orderMap) {
		super(orderMap);
	}

}
