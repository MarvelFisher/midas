package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeSubAccountDeleteEvent extends
		PmExchangeSubAccountOperationEvent {

	private static final long serialVersionUID = 98165407286826167L;

	public PmExchangeSubAccountDeleteEvent(ExchangeSubAccount exchangeSubAccount) {
		super(exchangeSubAccount);
	}
}
