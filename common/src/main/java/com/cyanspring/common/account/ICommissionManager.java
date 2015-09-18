package com.cyanspring.common.account;

import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;

public interface ICommissionManager {
	double getCommission(RefData refData, AccountSetting settings, double value, Execution execution);
	double getCommission(RefData refData, AccountSetting settings, Execution execution);
}
