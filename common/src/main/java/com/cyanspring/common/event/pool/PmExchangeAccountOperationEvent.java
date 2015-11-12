package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeAccountOperationEvent extends AsyncEvent {

	private static final long serialVersionUID = 1210207700712794921L;

	private ExchangeAccount exchangeAccount;

	public PmExchangeAccountOperationEvent(ExchangeAccount exchangeAccount) {
		this.exchangeAccount = exchangeAccount;
	}

	public ExchangeAccount getExchangeAccount() {
		return exchangeAccount;
	}
}
