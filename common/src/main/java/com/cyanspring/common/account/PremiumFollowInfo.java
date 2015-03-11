package com.cyanspring.common.account;

import java.util.Date;

public class PremiumFollowInfo {
	private String frAccount;
	private String fdAccount;
	private Date expiry;
	
	public PremiumFollowInfo(String frAccount,
			String fdAccount, Date expiry) {
		super();
		this.frAccount = frAccount;
		this.fdAccount = fdAccount;
		this.expiry = expiry;
	}
	public String getFrAccount() {
		return frAccount;
	}
	public String getFdAccount() {
		return fdAccount;
	}
	public Date getExpiry() {
		return expiry;
	}
	
	
}
