package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.Collections;
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
			if (symbolListClr.contains(symbolinfo.getCode()) 
					|| symbolListClr.contains(symbolinfo.getHint()) )
			{
				index = symbolListClr.indexOf(symbolinfo.getCode());
				if (index < 0)
				{
					index = symbolListClr.indexOf(symbolinfo.getHint());
				}
				infoList.remove(index);
				infoList.add(index, symbolinfo);
			}
			else if (symbolListClr.contains(symbolinfo.getHint() + "." + symbolinfo.getExchange()))
			{
				index = symbolListClr.indexOf(symbolinfo.getCode());
				if (index < 0)
				{
					index = symbolListClr.indexOf(symbolinfo.getHint() + "." + symbolinfo.getExchange());
				}
				infoList.remove(index);
				infoList.add(index, symbolinfo);
			}
			else if (symbolinfo.getWindCode() != null 
					&& symbolinfo.getWindCode().equals("HOT")
					&& symbolListClr.contains(symbolinfo.getCategory()))
			{
				index = symbolListClr.indexOf(symbolinfo.getCategory());
				if (index < 0)
				{
					index = symbolListClr.indexOf(symbolinfo.getHint() + "." + symbolinfo.getExchange());
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
		for (SymbolInfo inputinfo : inputInfoList)
		{
			for (SymbolInfo symbolinfo : refSymbolInfo)
			{
				if (symbolinfo.getWindCode().equals("HOT") && 
						symbolinfo.getCategory().equals(inputinfo.getCategory()))
				{
					infoList.add(symbolinfo);
					break;
				}
				else if (symbolinfo.getHintOrCode().equals(inputinfo.getHintOrCode()))
				{
					infoList.add(symbolinfo);
					break;
				}
			}
		}
		return infoList;
	}
	@Override
	public SymbolInfo getbySymbol(String symbol) 
	{
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbol.equals(symbolinfo.getCode()) 
					|| symbol.equals(symbolinfo.getHint() + "." + symbolinfo.getExchange()))
			{
				return symbolinfo;
			}
		}
		return null;
	}
}
