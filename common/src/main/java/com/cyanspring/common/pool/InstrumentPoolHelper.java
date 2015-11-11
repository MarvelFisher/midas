package com.cyanspring.common.pool;

import com.cyanspring.common.event.OperationType;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class InstrumentPoolHelper {

	public static void updateExchangeAccount(
			InstrumentPoolKeeper instrumentPoolKeeper,
			ExchangeAccount exchangeAccount, OperationType type) {
		switch (type) {
		case CREATE:
			instrumentPoolKeeper.add(exchangeAccount);
			break;
		case UPDATE:
			instrumentPoolKeeper.update(exchangeAccount);
			break;
		case DELETE:
			instrumentPoolKeeper.delete(exchangeAccount);
			break;
		}
	}
}
