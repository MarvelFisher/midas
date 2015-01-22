package com.cyanspring.common.staticdata;

import java.util.List;

public interface IRefDataManager {
	public void init() throws Exception;
	public RefData getRefData(String symbol);
	public String getRefDataFile();
	public List<RefData> getRefDataList();
	public String getMarket();
	public void setRefDataFile(String refDataFile);
}
