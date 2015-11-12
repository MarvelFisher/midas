package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeAccountDeleteEvent extends
		PmExchangeAccountOperationEvent {

	private static final long serialVersionUID = 4409142521000082829L;

	public PmExchangeAccountDeleteEvent(ExchangeAccount exchangeAccount) {
		super(exchangeAccount);
	}
}
