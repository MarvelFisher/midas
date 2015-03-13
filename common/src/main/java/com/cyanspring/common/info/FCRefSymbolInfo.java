package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;

public class FCRefSymbolInfo extends IRefSymbolInfo
{
	public FCRefSymbolInfo(String serverMarket)
	{
		super(serverMarket);
	}
	@Override
	public List<SymbolInfo> getBySymbolStrings(List<String> symbolList) 
	{
		int index;
		ArrayList<SymbolInfo> infoList = new ArrayList<SymbolInfo>();
		for (int ii = 0; ii < symbolList.size(); ii++)
		{
			infoList.add(null);
		}
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolList.contains(symbolinfo.getCode()) || symbolList.contains(symbolinfo.getHint()))
			{
				index = symbolList.indexOf(symbolinfo.getCode());
				if (index < 0)
				{
					index = symbolList.indexOf(symbolinfo.getHint());
				}
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
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			for (SymbolInfo inputinfo : inputInfoList)
			{
				if (symbolinfo.getCode().equals(inputinfo.getCode()) 
						|| symbolinfo.getHint().equals(inputinfo.getHint()))
				{
					infoList.add(symbolinfo);
					break;
				}
			}
		}
		return infoList;
	}
}
