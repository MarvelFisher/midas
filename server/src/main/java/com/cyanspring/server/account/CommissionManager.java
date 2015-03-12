package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.Map;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ICommissionManager;
import com.cyanspring.common.staticdata.RefData;

public class CommissionManager implements ICommissionManager{
	private Map<String, Double> commissionByMarket = new HashMap<>();
	private Map<String, Double> commissionByExchange = new HashMap<>();	
	private final double minCommission = 2;

	@Override
	public double getCommission(RefData refData, AccountSetting settings) {
		double accountCom = 1;
		if(settings != null && !PriceUtils.isZero(settings.getCommission()))
			accountCom = settings.getCommission();		
		if(refData == null)
			return Default.getCommission() * accountCom;
		
		Double com = refData.getCommissionFee();
		if(!PriceUtils.isZero(settings.getCommission()))
			return com * accountCom;
		
		String market = refData.getMarket();
		com = commissionByMarket.get(market);
		if(com != null)
			return com * accountCom;
		
		String exchange = refData.getExchange();
		com = commissionByExchange.get(exchange);
		if(com != null)
			return com * accountCom;
		
		return Default.getCommission() * accountCom;		
	}
	
	@Override
	public double getCommission(RefData refData, AccountSetting settings, double value) {
		double commission = getCommission(refData, settings);
		return calCommission(commission * value);
	}

	private double calCommission(double value) {
		if(PriceUtils.EqualLessThan(value, minCommission))
			return minCommission;
		else
			return value;
	}
	
	public Map<String, Double> getCommissionByMarket() {
		return commissionByMarket;
	}


	public void setCommissionByMarket(Map<String, Double> commissionByMarket) {
		this.commissionByMarket = commissionByMarket;
	}


	public Map<String, Double> getCommissionByExchange() {
		return commissionByExchange;
	}


	public void setCommissionByExchange(Map<String, Double> commissionByExchange) {
		this.commissionByExchange = commissionByExchange;
	}
}
