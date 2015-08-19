package com.cyanspring.common.staticdata;

import java.util.List;

public interface IRefDataManager {
	public void init() throws Exception;
    public boolean updateAll(String tradeDate) throws Exception;
    public List<RefData> update(String index, String tradeDate) throws Exception;
    public RefData update(RefData refData, String tradeDate) throws Exception;
	public RefData getRefData(String symbol);
	public boolean remove(RefData refData);
	public String getRefDataFile();
	public List<RefData> getRefDataList();
	public String getMarket();
	public void setRefDataFile(String refDataFile);
	public void injectRefDataList(List<RefData> refDataList);
	public void clearRefData();
}
