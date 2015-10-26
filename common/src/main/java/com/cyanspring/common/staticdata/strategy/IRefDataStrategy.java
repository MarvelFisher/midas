package com.cyanspring.common.staticdata.strategy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;

public interface IRefDataStrategy {
	public void init(Calendar cal);
    public List<RefData> updateRefData(RefData refData);
    public void setRequireData(Object... objects);
}
