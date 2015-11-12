package com.cyanspring.common.event.pool;

import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class PmExchangeAccountInsertEvent extends
		PmExchangeAccountOperationEvent {

	private static final long serialVersionUID = 5350335856704437522L;

	public PmExchangeAccountInsertEvent(ExchangeAccount exchangeAccount) {
		super(exchangeAccount);
	}
}
