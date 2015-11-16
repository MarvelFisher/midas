package com.cyanspring.common.pool;

import java.io.Serializable;

public class AccountPool implements Serializable {

	private static final long serialVersionUID = 1L;

	private String account;
	private String instrumentPool;

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
