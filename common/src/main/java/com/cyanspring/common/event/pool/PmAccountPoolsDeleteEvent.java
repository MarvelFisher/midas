package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.AccountPool;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class PmAccountPoolsDeleteEvent extends AsyncEvent {

	private static final long serialVersionUID = -2636335802783130768L;

	private List<AccountPool> accountPools;

	public PmAccountPoolsDeleteEvent(List<AccountPool> accountPools) {
		this.accountPools = accountPools;
	}

	public List<AccountPool> getAccountPools() {
		return accountPools;
	}

}
