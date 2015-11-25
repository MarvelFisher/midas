package com.cyanspring.common.pool;

import java.util.List;

import com.cyanspring.common.account.AccountKeeper;
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

	public static void updateExchangeSubAccount(
			InstrumentPoolKeeper instrumentPoolKeeper,
			ExchangeSubAccount exchangeSubAccount, OperationType type) {
		switch (type) {
		case CREATE:
			instrumentPoolKeeper.add(exchangeSubAccount);
			break;
		case UPDATE:
			instrumentPoolKeeper.update(exchangeSubAccount);
			break;
		case DELETE:
			instrumentPoolKeeper.delete(exchangeSubAccount);
			break;
		}
	}

	public static void updateInstrumentPool(
			InstrumentPoolKeeper instrumentPoolKeeper,
			InstrumentPool instrumentPool, OperationType type) {
		switch (type) {
		case CREATE:
			instrumentPoolKeeper.add(instrumentPool);
			break;
		case UPDATE:
			break;
		case DELETE:
			instrumentPoolKeeper.delete(instrumentPool);
			break;
		}
	}

	public static void updateInstrumentPoolRecords(
			InstrumentPoolKeeper instrumentPoolKeeper,
			List<InstrumentPoolRecord> records, OperationType type) {
		switch (type) {
		case CREATE:
			instrumentPoolKeeper.add(records);
			break;
		case UPDATE:
			break;
		case DELETE:
			instrumentPoolKeeper.delete(records);
			break;
		}
	}

	public static void updateAccountPools(AccountKeeper accountKeeper,
			InstrumentPoolKeeper instrumentPoolKeeper,
			List<AccountPool> accountPools, OperationType type) {
		switch (type) {
		case CREATE:
			for (AccountPool accountPool : accountPools) {
				accountKeeper.getAccount(accountPool.getAccount()).add(
						accountPool);
				instrumentPoolKeeper.getInstrumentPool(
						accountPool.getInstrumentPool()).add(accountPool);
			}
			break;
		case UPDATE:
			break;
		case DELETE:
			for (AccountPool accountPool : accountPools) {
				accountKeeper.getAccount(accountPool.getAccount()).delete(
						accountPool);
				instrumentPoolKeeper.getInstrumentPool(
						accountPool.getInstrumentPool()).delete(accountPool);
			}
			break;
		}
	}

	public static void updateUserExchangeSubAccounts(
			InstrumentPoolKeeper instrumentPoolKeeper,
			List<UserExchangeSubAccount> userExchangeSubAccounts,
			OperationType type) {
		switch (type) {
		case CREATE:
			for (UserExchangeSubAccount userExchangeSubAccount : userExchangeSubAccounts) {
				instrumentPoolKeeper.add(userExchangeSubAccount);
			}
			break;
		case UPDATE:
			break;
		case DELETE:
			for (UserExchangeSubAccount userExchangeSubAccount : userExchangeSubAccounts) {
				instrumentPoolKeeper.delete(userExchangeSubAccount);
			}
			break;
		}
	}
}
