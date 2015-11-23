package com.cyanspring.common.pool;

import java.io.Serializable;

public class UserExchangeSubAccount implements Serializable {

	private static final long serialVersionUID = -395487495460995664L;

	private String account;

	private String exchangeSubAccount;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setExchangeSubAccount(String exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}
}
