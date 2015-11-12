package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeSubAccountUpdateEvent extends
		PmExchangeSubAccountOperationEvent {

	private static final long serialVersionUID = -2842036058776637058L;

	public PmExchangeSubAccountUpdateEvent(ExchangeSubAccount exchangeSubAccount) {
		super(exchangeSubAccount);
	}
}
