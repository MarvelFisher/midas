package com.cyanspring.info.alert;

public class PremiumUser implements Comparable<PremiumUser>{
	private String UserId;
	private String EndDate;
	public PremiumUser(String user,String endDate)
	{
		this.setUserId(user);
		this.setEndDate(endDate);
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public String getEndDate() {
		return EndDate;
	}
	public void setEndDate(String endDate) {
		EndDate = endDate;
	}
	@Override
	public int compareTo(PremiumUser arg0) {
		int retrun = this.UserId.compareTo(arg0.UserId);
		return retrun;
	}
}
