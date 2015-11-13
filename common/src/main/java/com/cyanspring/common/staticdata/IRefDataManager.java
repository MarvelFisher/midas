package com.cyanspring.common.staticdata;

import java.util.List;

public interface IRefDataManager {
	public void init() throws Exception;
    public List<RefData> updateAll(String tradeDate) throws Exception;
    public List<RefData> update(String index, String tradeDate) throws Exception;
	public boolean remove(RefData refData);
	public void clearRefData();
	public RefData getRefData(String symbol);
	public String getRefDataFile();
	public List<RefData> getRefDataList();
	public String getMarket();
	public void setRefDataFile(String refDataFile);
	public void setQuoteFile(String quoteFile);
	public void saveRefDataToFile();
}
