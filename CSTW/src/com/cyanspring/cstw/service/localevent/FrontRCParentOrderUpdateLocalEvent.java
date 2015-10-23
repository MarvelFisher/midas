package com.cyanspring.cstw.service.localevent;

import java.util.Map;

import com.cyanspring.common.business.ParentOrder;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/07
 *
 */
public class FrontRCParentOrderUpdateLocalEvent extends BasicRCParentOrderUpdateLocalEvent {

	private static final long serialVersionUID = 1L;

	public FrontRCParentOrderUpdateLocalEvent(Map<String, ParentOrder> orderMap) {
		super(orderMap);
	}

}
