package com.cyanspring.cstw.model.admin;


/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public final class SubAccountModel {

	private String id;

	private String name;

	private ExchangeAccountModel exchangeAccountModel;

	private double useableMoney;

	private double commissionRate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ExchangeAccountModel getExchangeAccountModel() {
		return exchangeAccountModel;
	}

	public void setExchangeAccountModel(
			ExchangeAccountModel exchangeAccountModel) {
		this.exchangeAccountModel = exchangeAccountModel;
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

}
