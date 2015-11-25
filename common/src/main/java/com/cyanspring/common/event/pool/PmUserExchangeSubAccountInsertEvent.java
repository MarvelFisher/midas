package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.UserExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/25/2015
 */
public class PmUserExchangeSubAccountInsertEvent extends AsyncEvent {

	private static final long serialVersionUID = 3507109544902841247L;

	private List<UserExchangeSubAccount> userExchangeSubAccounts;

	public PmUserExchangeSubAccountInsertEvent(
			List<UserExchangeSubAccount> userExchangeSubAccounts) {
		this.userExchangeSubAccounts = userExchangeSubAccounts;
	}

	public List<UserExchangeSubAccount> getUserExchangeSubAccounts() {
		return userExchangeSubAccounts;
	}

}
