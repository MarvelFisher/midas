package com.cyanspring.common.account;

import java.util.Date;

public class PremiumFollowInfo {
	private String frUser;
	private String frAccount;
	private String fdUser;
	private String fdAccount;
	private Date expiry;
	
	public PremiumFollowInfo(String frUser, String frAccount, String fdUser,
			String fdAccount, Date expiry) {
		this.frUser = frUser;
		this.frAccount = frAccount;
		this.fdUser = fdUser;
		this.fdAccount = fdAccount;
		this.expiry = expiry;
	}

	public String getFrUser() {
		return frUser;
	}

	public String getFrAccount() {
		return frAccount;
	}

	public String getFdUser() {
		return fdUser;
	}

	public String getFdAccount() {
		return fdAccount;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setFrUser(String frUser) {
		this.frUser = frUser;
	}

	public void setFrAccount(String frAccount) {
		this.frAccount = frAccount;
	}

	public void setFdUser(String fdUser) {
		this.fdUser = fdUser;
	}

	public void setFdAccount(String fdAccount) {
		this.fdAccount = fdAccount;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}
	
	@Override
	public String toString() {
		return "[" + this.getFrUser() + "," + this.getFrAccount() + "," + this.getFdUser() + "," + this.getFdAccount() + "]";
	}
	
}
