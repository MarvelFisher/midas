package com.cyanspring.common.staticdata;

public interface IRefDataManager {
	public void init() throws Exception;
	public RefData getRefData(String symbol);
	public String getRefDataFile();
	public void setRefDataFile(String refDataFile);
}
