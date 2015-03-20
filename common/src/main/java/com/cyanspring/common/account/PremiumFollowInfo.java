package com.cyanspring.common.account;

public class PremiumFollowInfo {
	private String fdUser;
	private String market;

	public PremiumFollowInfo(String fdUser, String market) {
		this.fdUser = fdUser;
		this.market = market;
	}

	public String getFdUser() {
		return fdUser;
	}

	public String getMarket() {
		return market;
	}

	public void setFdUser(String fdUser) {
		this.fdUser = fdUser;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	@Override
	public String toString() {
		return "[" + this.getFdUser() + "," + this.getMarket() + "]";
	}

}
