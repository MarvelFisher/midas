package com.cyanspring.common.account;

public class PremiumFollowInfo {
	private String fdUser;
	private String fdAccount;

	public PremiumFollowInfo(String fdUser, String fdAccount) {
		this.fdUser = fdUser;
		this.fdAccount = fdAccount;
	}

	public String getFdUser() {
		return fdUser;
	}

	public String getFdAccount() {
		return fdAccount;
	}

	public void setFdUser(String fdUser) {
		this.fdUser = fdUser;
	}

	public void setFdAccount(String fdAccount) {
		this.fdAccount = fdAccount;
	}

	@Override
	public String toString() {
		return "[" + this.getFdUser() + "," + this.getFdAccount() + "]";
	}

}
