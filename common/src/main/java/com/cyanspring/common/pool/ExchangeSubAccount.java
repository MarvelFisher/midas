package com.cyanspring.common.pool;

public class ExchangeSubAccount {
	private String id;
	private String desc;
	private String exchangeAccount;

	public ExchangeSubAccount(String id, String desc, String exchangeAccount) {
		super();
		this.id = id;
		this.desc = desc;
		this.exchangeAccount = exchangeAccount;
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public String getExchangeAccount() {
		return exchangeAccount;
	}
}
