package com.cyanspring.common;

public class Default2 {
	private String user = "DEFAULT";
	private String account = "DEFAULT";
	private String currency = "USD";
	private String accountPrefix = "A";
	private double accountCash = 100000.0;
	private double marginTimes = 40.0;
	
	private static Default2 instance;
	// singleton implementation
	public Default2() {
	}
	
	public static Default2 getInstance() {
		return instance;
	}

	public static void setInstance(Default2 def) {
		instance = def;
	}
	
	protected Default2(
			String user,
			String account,
			String currency,
			String accountPrefix,
			double accountCash,
			double marginTimes) {
		this.user = user;
		this.account = account;
		this.currency = currency;
		this.accountPrefix = accountPrefix;
		this.accountCash = accountCash;
		this.marginTimes = marginTimes;
		
	}

	public String getUser() {
		return user;
	}

	public String getAccount() {
		return account;
	}

	public String getCurrency() {
		return currency;
	}

	public String getAccountPrefix() {
		return accountPrefix;
	}

	public double getAccountCash() {
		return accountCash;
	}

	public double getMarginTimes() {
		return marginTimes;
	}

}
