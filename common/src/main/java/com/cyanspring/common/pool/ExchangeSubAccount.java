package com.cyanspring.common.pool;

import java.io.Serializable;

public class ExchangeSubAccount implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String desc;
	private String exchangeAccount;

	public ExchangeSubAccount() {
	}

	public ExchangeSubAccount(String id, String desc, String exchangeAccount) {
		super();
		this.id = id;
		this.desc = desc;
		this.exchangeAccount = exchangeAccount;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public String getExchangeAccount() {
		return exchangeAccount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setExchangeAccount(String exchangeAccount) {
		this.exchangeAccount = exchangeAccount;
	}
}
