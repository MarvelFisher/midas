package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;

public interface IRefDataStrategy {
	public void init(Calendar cal);
    public void updateRefData(RefData refData);
    public void setRequireData(Object... objects);
}
