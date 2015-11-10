/**
 * 
 */
package com.cyanspring.cstw.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 20 Aug 2015
 */
public class RCCapitalInfoModel {
	
	// 交易账号
	private String account;
	
	// 可用资金
	private Double availCash;
	
	// 挂单资金
	private Double pendingOrderCash; 
		
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Double getAvailCash() {
		return availCash;
	}

	public void setAvailCash(double availCash) {
		this.availCash = availCash;
	}

	public Double getPendingOrderCash() {
		return pendingOrderCash;
	}

	public void setPendingOrderCash(double pendingOrderCash) {
		this.pendingOrderCash = pendingOrderCash;
	}

	public static class Builder {		
		private String account;
		private double availCash;
		private double pendingOrderCash; 
		
		public Builder account(String val) {
			account = val;		return this;
		}
		
		public Builder availCash(double val) {
			availCash = val;		return this;
		}
		
		public Builder pendingOrderCash(double val) {
			pendingOrderCash = val;		return this;
		}
		
		public RCCapitalInfoModel build() {
			return new RCCapitalInfoModel(this);
		}
	}

	private RCCapitalInfoModel(Builder builder) {
		account				 = builder.account;
		availCash 			 = builder.availCash;
		pendingOrderCash	 = builder.pendingOrderCash;
	}

}
