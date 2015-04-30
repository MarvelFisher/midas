package com.cyanspring.common.account;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;

public interface ICommissionManager {
	double getCommission(RefData refData, AccountSetting settings, double value, OrderSide side);
	double getCommission(RefData refData, AccountSetting settings, OrderSide side);
}
