/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr.caching;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 * designed for CachingManager
 */
public class BackRCParentOrderUpdateCachingLocalEvent extends BasicRCParentOrderUpdateCachingLocalEvent {

	private static final long serialVersionUID = 1L;

	public BackRCParentOrderUpdateCachingLocalEvent(Map<String, ParentOrder> orderMap) {
		super(orderMap);
	}

}
