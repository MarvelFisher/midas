/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.event.AsyncEvent;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public class BasicRCPositionUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;
	// 交易员id -> OverallPosition id -> OverallPosition
	private Map<String, Map<String, OverallPosition>> accountPositionMap;
	
	public BasicRCPositionUpdateLocalEvent(Map<String, Map<String, OverallPosition>> accountPositionMap) {
		this.accountPositionMap = accountPositionMap;
	}

	public Map<String, Map<String, OverallPosition>> getAccountPositionMap() {
		return accountPositionMap;
	}

	public void setAccountPositionMap(
			Map<String, Map<String, OverallPosition>> accountPositionMap) {
		this.accountPositionMap = accountPositionMap;
	}

}
