package com.cyanspring.common.account;

import com.cyanspring.common.staticdata.RefData;

public interface ICommissionManager {
	double getCommission(RefData refData, AccountSetting settings, double value);
	double getCommission(RefData refData, AccountSetting settings);
}
