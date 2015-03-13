package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;

public class FXRefSymbolInfo extends IRefSymbolInfo
{
	public FXRefSymbolInfo(String serverMarket)
	{
		super(serverMarket);
	}
	@Override
	public List<SymbolInfo> getBySymbolStrings(List<String> symbolList) 
	{
		int index;
		ArrayList<SymbolInfo> infoList = new ArrayList<SymbolInfo>();
		for (int ii = 0; ii < symbolList.size(); ii++) infoList.add(null);
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolList.contains(symbolinfo.getCode()))
			{
				index = symbolList.indexOf(symbolinfo.getCode());
				infoList.remove(index);
				infoList.add(index, symbolinfo);
			}
		}
		while (infoList.contains(null)) infoList.remove(null);
		return infoList;
	}
	@Override
	public List<SymbolInfo> getBySymbolInfos(List<SymbolInfo> inputInfoList) 
	{
		ArrayList<SymbolInfo> infoList = new ArrayList<SymbolInfo>();
		for (SymbolInfo symbolinfo : inputInfoList)
		{
			if (Collections.binarySearch(refSymbolInfo, symbolinfo) >= 0)
			{
				infoList.add(symbolinfo);
			}
		}
		return infoList;
	}
}
