package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.Map;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.staticdata.RefData;

public class LeverageManager implements ILeverageManager{
	
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
		
		return factor/refData.getMarginRate();
	}
}
