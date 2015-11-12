package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeAccountUpdateEvent extends
		PmExchangeAccountOperationEvent {

	private static final long serialVersionUID = -2048911663064520597L;

	public PmExchangeAccountUpdateEvent(ExchangeAccount exchangeAccount) {
		super(exchangeAccount);
	}
}
