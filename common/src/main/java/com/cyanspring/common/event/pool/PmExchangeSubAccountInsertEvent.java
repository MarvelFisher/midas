package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeSubAccountInsertEvent extends
		PmExchangeSubAccountOperationEvent {

	private static final long serialVersionUID = 2407264795540380887L;

	public PmExchangeSubAccountInsertEvent(ExchangeSubAccount exchangeSubAccount) {
		super(exchangeSubAccount);
	}
}
