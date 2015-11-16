package com.cyanspring.common.pool;

import java.io.Serializable;
import java.util.Set;

public class InstrumentPool implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String exchangeSubAccount;
	// k=Account id
	private Set<String> accounts;

	public InstrumentPool() {
	}

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
