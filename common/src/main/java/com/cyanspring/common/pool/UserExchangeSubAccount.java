package com.cyanspring.common.pool;

import java.io.Serializable;

public class UserExchangeSubAccount implements Serializable {

	private static final long serialVersionUID = -395487495460995664L;

	private String user;

	private String exchangeSubAccount;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setExchangeSubAccount(String exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}
}
