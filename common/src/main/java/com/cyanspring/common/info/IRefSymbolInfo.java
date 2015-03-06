package com.cyanspring.common.info;

import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.staticdata.RefData;

public interface IRefSymbolInfo {
	public void reset();
	public void setByRefData(List<RefData> refdataList);
	public List<SymbolInfo> getBySymbolStrings(List<String> symbolList);
	public List<SymbolInfo> getBySymbolInfos(List<SymbolInfo> inputInfoList);
	public List<SymbolInfo> getAllSymbolInfo(String market);
}
