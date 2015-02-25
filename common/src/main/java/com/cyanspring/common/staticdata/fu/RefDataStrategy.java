package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;

import com.cyanspring.common.staticdata.RefData;

public interface RefDataStrategy {
	public void init(Calendar cal);
	public void setExchangeRefData(RefData refData);
}
