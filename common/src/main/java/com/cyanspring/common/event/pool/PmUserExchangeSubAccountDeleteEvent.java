package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.UserExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmUserExchangeSubAccountDeleteEvent extends AsyncEvent {

	private static final long serialVersionUID = 5618782113213603809L;

	private List<UserExchangeSubAccount> userExchangeSubAccounts;

	public PmUserExchangeSubAccountDeleteEvent(
			List<UserExchangeSubAccount> userExchangeSubAccounts) {
		this.userExchangeSubAccounts = userExchangeSubAccounts;
	}

	public List<UserExchangeSubAccount> getUserExchangeSubAccounts() {
		return userExchangeSubAccounts;
	}
}
