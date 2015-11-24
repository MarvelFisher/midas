package com.cyanspring.common.staticdata.strategy;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefData;

public interface IRefDataStrategy {
	public void init(Calendar cal, Map<String, Quote> map) throws Exception;
    public List<RefData> updateRefData(RefData refData) throws ParseException;
    public void setRequireData(Object... objects);
}
