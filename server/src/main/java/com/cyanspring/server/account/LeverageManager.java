package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.Map;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.staticdata.RefData;

public class LeverageManager implements ILeverageManager{
	Map<String, Double> leverageByMarket = new HashMap<String, Double>();
	Map<String, Double> leverageByExchange = new HashMap<String, Double>();
	
	@Override
	public double getLeverage(RefData refData, AccountSetting settings) {
		double factor = 1.0;
		if(null != settings) {
			double lev = settings.getLeverageRate();
			if(!PriceUtils.isZero(lev))
				factor = lev;
		}
		
		if(null == refData)
			return Default.getMarginTimes() * factor;
		
		Double marginRate = refData.getMarginRate();
		if(!PriceUtils.isZero(marginRate)) {
			return factor/marginRate;
		}
		
		String market = refData.getMarket();
		marginRate = leverageByMarket.get(market);
		if(null != marginRate && !PriceUtils.isZero(marginRate))
			return factor/marginRate;
		
		String exchange = refData.getExchange();
		marginRate = leverageByExchange.get(exchange);
		if(null != marginRate && !PriceUtils.isZero(marginRate))
			return factor/marginRate;
		
		
		return Default.getMarginTimes() * factor;
	}

	public Map<String, Double> getLeverageByMarket() {
		return leverageByMarket;
	}

	public void setLeverageByMarket(Map<String, Double> leverageByMarket) {
		this.leverageByMarket = leverageByMarket;
	}

	public Map<String, Double> getLeverageByExchange() {
		return leverageByExchange;
	}

	public void setLeverageByExchange(Map<String, Double> leverageByExchange) {
		this.leverageByExchange = leverageByExchange;
	}
	
}
