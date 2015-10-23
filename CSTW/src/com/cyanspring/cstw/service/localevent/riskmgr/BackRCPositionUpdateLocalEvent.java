/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.account.OverallPosition;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 * designed for CachingManager
 */
public class BackRCPositionUpdateLocalEvent extends BasicRCPositionUpdateLocalEvent {
	
	private static final long serialVersionUID = 1L;

	public BackRCPositionUpdateLocalEvent(
			Map<String, Map<String, OverallPosition>> accountPositionMap) {
		super(accountPositionMap);
	}

}
