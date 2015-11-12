package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeSubAccountOperationEvent extends AsyncEvent {

	private static final long serialVersionUID = -2979845288242982425L;

	private ExchangeSubAccount exchangeSubAccount;

	public PmExchangeSubAccountOperationEvent(
			ExchangeSubAccount exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public ExchangeSubAccount getExchangeSubAccount() {
		return exchangeSubAccount;
	}
}
