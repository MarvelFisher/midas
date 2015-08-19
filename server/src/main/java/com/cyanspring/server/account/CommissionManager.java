package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.Map;

import com.cyanspring.common.type.OrderSide;
import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ICommissionManager;
import com.cyanspring.common.staticdata.RefData;

public class CommissionManager implements ICommissionManager{
	private Map<String, Double> commissionByMarket = new HashMap<>();
	private Map<String, Double> commissionByExchange = new HashMap<>();	
	private double minCommission = 2;

	@Override
	public double getCommission(RefData refData, AccountSetting settings, OrderSide side) {
		double accountCom = 1;
		if(settings != null && !PriceUtils.isZero(settings.getCommission()))
			accountCom = settings.getCommission();		
		if(refData == null)
			return Default.getCommission() * accountCom;
		
		Double com = refData.getCommissionFee();
		if(!nullOrZero(com))
			return com * accountCom;
		
		com = refData.getMinimalCommissionFee();
		if(!nullOrZero(com))
			return com * accountCom;
		
		com = refData.getLotCommissionFee();
		if(!nullOrZero(com))
			return com * accountCom;
		
		String market = refData.getMarket();
		com = commissionByMarket.get(market);
		if(!nullOrZero(com))
			return com * accountCom;
		
		String exchange = refData.getExchange();
		com = commissionByExchange.get(exchange);
		if(!nullOrZero(com))
			return com * accountCom;
		
		return Default.getCommission() * accountCom;		
	}
	
	@Override
	public double getCommission(RefData refData, AccountSetting settings, double value, OrderSide side) {
		double commission = getCommission(refData, settings, side);
		return calCommission(commission , value, refData);
	}

	private double calCommission(double commission, double value, RefData refData) {
		if(!nullOrZero(refData.getLotCommissionFee())) 
			return commission;

		Double min = refData.getMinimalCommissionFee();
		if ( min != null && !PriceUtils.isZero(min))
			minCommission = min;
		
		double price = commission * value;
		if (PriceUtils.EqualLessThan(price, minCommission))
			return minCommission;
		else
			return Math.ceil(price);
	}
	
	private boolean nullOrZero(Double commission) {
		return commission == null || PriceUtils.isZero(commission);
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
