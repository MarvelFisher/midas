package com.cyanspring.common.pool;

public class AccountPool {
	String account;
	String pool;
	
	public AccountPool(String account, String pool) {
		super();
		this.account = account;
		this.pool = pool;
	}
	public String getAccount() {
		return account;
	}
	public String getPool() {
		return pool;
	}
}
