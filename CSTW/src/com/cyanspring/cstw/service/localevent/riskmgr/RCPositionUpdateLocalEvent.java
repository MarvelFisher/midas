package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.Map;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class RCPositionUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = -896293955768549192L;
	
	// 交易员id -> OverallPosition id -> OverallPosition
	private Map<String, Map<String, OverallPosition>> accountPositionMap;
	
	private UserRole roleType;
	
	public RCPositionUpdateLocalEvent(UserRole roleType) {
		this.roleType = roleType;
	}

	public Map<String, Map<String, OverallPosition>> getAccountPositionMap() {
		return accountPositionMap;
	}

	public void setAccountPositionMap(
			Map<String, Map<String, OverallPosition>> accountPositionMap) {
		this.accountPositionMap = accountPositionMap;
	}
	
	public UserRole getRoleType() {
		return roleType;
	}

}
