package com.cyanspring.common.account;

import com.cyanspring.common.staticdata.RefData;

public interface ILeverageManager {
	double getLeverage(RefData refData, AccountSetting settings);
}
