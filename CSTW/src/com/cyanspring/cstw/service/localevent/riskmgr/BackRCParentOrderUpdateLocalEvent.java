/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public class BackRCParentOrderUpdateLocalEvent extends BasicRCParentOrderUpdateLocalEvent {

	private static final long serialVersionUID = 1L;

	public BackRCParentOrderUpdateLocalEvent(Map<String, ParentOrder> orderMap) {
		super(orderMap);
	}

}
