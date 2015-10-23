package com.cyanspring.cstw.service.localevent;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;

/**
 * Used to query open positions for all accounts controlled by risk manager
 * 
 * @author GuoWei
 * @since 08/03/2015
 */
public class OpenPositionSnapshotListRequestLocalEvent extends AsyncEvent {
	
	private static final long serialVersionUID = 1L;

	public OpenPositionSnapshotListRequestLocalEvent(String key) {
		super(key);
	}
	
	public UserRole getRoleType() {
		return UserRole.valueOf(getKey());
	}
}
