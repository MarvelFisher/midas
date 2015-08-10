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
	    ArrayList<String> symbolListClr = new ArrayList<String>();
	    for (String s: symbolList)
	    {
	        if(Collections.frequency(symbolListClr, s) < 1) symbolListClr.add(s);
	    }
		for (int ii = 0; ii < symbolListClr.size(); ii++) 
		{
			infoList.add(null);
		}
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolListClr.contains(symbolinfo.getCode()))
			{
				index = symbolListClr.indexOf(symbolinfo.getCode());
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
		int index;
		ArrayList<SymbolInfo> infoList = new ArrayList<SymbolInfo>();
		for (SymbolInfo symbolinfo : inputInfoList)
		{
			index = Collections.binarySearch(refSymbolInfo, symbolinfo); 
			if (index >= 0)
			{
				infoList.add(refSymbolInfo.get(index));
			}
		}
		return infoList;
	}
	@Override
	public SymbolInfo getbySymbol(String symbol) 
	{
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbol.equals(symbolinfo.getCode()))
			{
				return symbolinfo;
			}
		}
		return null;
	}
}
