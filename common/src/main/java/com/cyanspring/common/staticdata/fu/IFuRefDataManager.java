package com.cyanspring.common.staticdata.fu;

import com.cyanspring.common.staticdata.RefData;

public interface IFuRefDataManager {
	public void init() throws Exception;
	public RefData getRefDataByRefSymbol(String refSymbol);
	public RefData getRefDataBySymbol(String symbol);
}
