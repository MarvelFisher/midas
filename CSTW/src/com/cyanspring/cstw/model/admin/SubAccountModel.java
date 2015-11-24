package com.cyanspring.cstw.model.admin;

import com.cyanspring.cstw.model.BasicModel;


/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public final class SubAccountModel extends BasicModel{

	private String id;

	private String name;

	private String exchangeAccountName;
	
	private String relativeExchAccount;

	private double useableMoney;

	private double commissionRate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExchangeAccountName() {
		return exchangeAccountName;
	}

	public void setExchangeAccountName(
			String exchangeAccount) {
		this.exchangeAccountName = exchangeAccount;
	}
	
	public String getRelativeExchAccount() {
		return relativeExchAccount;
	}

	public void setRelativeExchAccount(String relativeExchAccount) {
		this.relativeExchAccount = relativeExchAccount;
	}

	public double getUseableMoney() {
		return useableMoney;
	}

	public void setUseableMoney(double useableMoney) {
		this.useableMoney = useableMoney;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCommissionRate() {
		return commissionRate;
	}

	public void setCommissionRate(double commissionRate) {
		this.commissionRate = commissionRate;
	}
	
	public static class Builder {
		private String id;
		private String name;
		private String exchangeAccountName;
		private String relativeExchAccount;
		private double useableMoney;
		private double commissionRate;
		
		public Builder id(String val) {
			id = val;		return this;
		}
		
		public Builder name(String val) {
			name = val;		return this;
		}
		
		public Builder relativeExchAccount(String val) {
			relativeExchAccount = val;		return this;
		}
		
		public Builder exchangeAccountName(String val) {
			exchangeAccountName = val;		return this;
		}
		
		public Builder useableMoney(double val) {
			useableMoney = val; 	return this;
		}
		
		public Builder commissionRate(double val) {
			commissionRate = val; 	return this;
		}
		
		public SubAccountModel build() {
			return new SubAccountModel(this);
		}
	}
	
	private SubAccountModel(Builder builder) {
		id = builder.id;
		name = builder.name;
		exchangeAccountName = builder.exchangeAccountName;
		relativeExchAccount = builder.relativeExchAccount;
		useableMoney = builder.useableMoney;
		commissionRate = builder.commissionRate;
	}

}
