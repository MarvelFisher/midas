package com.cyanspring.common.pool;

public class AccountPool {
	String account;
	String instrumentPool;

	public AccountPool(String account, String instrumentPool) {
		super();
		this.account = account;
		this.instrumentPool = instrumentPool;
	}

	public String getAccount() {
		return account;
	}

	public String getInstrumentPool() {
		return instrumentPool;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setInstrumentPool(String instrumentPool) {
		this.instrumentPool = instrumentPool;
	}
}
