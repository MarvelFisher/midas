package com.cyanspring.cstw.service.localevent.riskmgr.caching;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/07
 * designed for CachingManager
 */
public class FrontRCParentOrderUpdateCachingLocalEvent extends BasicRCParentOrderUpdateCachingLocalEvent {

	private static final long serialVersionUID = 1L;

	public FrontRCParentOrderUpdateCachingLocalEvent(Map<String, ParentOrder> orderMap) {
		super(orderMap);
	}

}
