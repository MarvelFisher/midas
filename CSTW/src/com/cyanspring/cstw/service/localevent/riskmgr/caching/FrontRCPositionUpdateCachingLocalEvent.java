package com.cyanspring.cstw.service.localevent.riskmgr.caching;

import java.util.Map;

import com.cyanspring.common.account.OverallPosition;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 * designed for CachingManager
 */
public final class FrontRCPositionUpdateCachingLocalEvent extends BasicRCPositionUpdateCachingLocalEvent {

	private static final long serialVersionUID = 1L;

	public FrontRCPositionUpdateCachingLocalEvent(
			Map<String, Map<String, OverallPosition>> accountPositionMap) {
		super(accountPositionMap);
	}
	
}
