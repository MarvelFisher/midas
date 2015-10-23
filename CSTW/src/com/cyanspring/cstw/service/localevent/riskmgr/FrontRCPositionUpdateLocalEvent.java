package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.account.OverallPosition;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class FrontRCPositionUpdateLocalEvent extends BasicRCPositionUpdateLocalEvent {

	private static final long serialVersionUID = 1L;

	public FrontRCPositionUpdateLocalEvent(
			Map<String, Map<String, OverallPosition>> accountPositionMap) {
		super(accountPositionMap);
	}
	
}
