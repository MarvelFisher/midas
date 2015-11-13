package com.cyanspring.common.pool;

import java.util.Set;

public class InstrumentPool {
	String id;
	String name;
	String exchangeSubAccount;
	// k=Account id
	Set<String> accounts;

	public InstrumentPool(String id, String exchangeSubAccount) {
		super();
		this.id = id;
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public String getId() {
		return id;
	}

	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExchangeSubAccount(String exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public Set<String> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<String> accounts) {
		this.accounts = accounts;
	}

	public void add(AccountPool accountPool) {
		accounts.add(accountPool.getAccount());
	}

	public void delete(AccountPool accountPool) {
		accounts.remove(accountPool.getAccount());
	}
}
