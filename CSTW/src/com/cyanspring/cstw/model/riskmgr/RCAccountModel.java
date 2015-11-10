/**
 * 
 */
package com.cyanspring.cstw.model.riskmgr;

/**
 * @author Yu-Junfeng
 * @create 19 Aug 2015
 */
public class RCAccountModel {
	
	// 交易账号
	private String accountId;
	
	// 总交易额
	private Double turnover;
	
	// 已实现盈利
	private Double realizedProfit;
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Double getTurnover() {
		return turnover;
	}

	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}

	public Double getRealizedProfit() {
		return realizedProfit;
	}

	public void setRealizedProfit(double realizedProfit) {
		this.realizedProfit = realizedProfit;
	}

	public static class Builder {
		private String accountId;
		private Double turnover;
		private Double realizedProfit;
		
		public Builder accountId(String val) {
			accountId = val;		return this;
		}
		
		public Builder turnover(double val) {
			turnover = val;			return this;
		}
		
		public Builder realizedProfit(double val) {
			realizedProfit = val;		return this;
		}
		
		public RCAccountModel build() {
			return new RCAccountModel(this);
		}
	}
	
	private RCAccountModel(Builder builder) {
		accountId			= builder.accountId;
		turnover			= builder.turnover;
		realizedProfit		= builder.realizedProfit;
	}

}
