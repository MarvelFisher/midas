package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.staticdata.RefData;

public abstract class IRefSymbolInfo {
	protected List<SymbolInfo> refSymbolInfo = new ArrayList<SymbolInfo>();
	protected String serverMarket;
	
	public IRefSymbolInfo(String serverMarket)
	{
		this.serverMarket = serverMarket;
	}
	public void reset()
	{
		refSymbolInfo.clear();
	}
	public int setByRefData(List<RefData> refdataList)
	{
		SymbolInfo symbolinfo = null;
		String strTmp = null;
		int index, nCount = 0;
		for (RefData refdata : refdataList)
		{
			if (refdata.getExchange() == null || refdata.getSymbol() == null) continue;
			
			symbolinfo = new SymbolInfo(serverMarket, refdata.getSymbol());
			index = Collections.binarySearch(refSymbolInfo, symbolinfo);
			if (index < 0)
			{
				refSymbolInfo.add(~index, symbolinfo);
				nCount++;
			}
			else
				symbolinfo = refSymbolInfo.get(index);
			symbolinfo.updateByRefData(refdata);
		}
		return nCount;
	}
	public void delByRefData(List<RefData> refdataList)
	{
		SymbolInfo symbolinfo = null;
		int index = 0;
		for (RefData refdata : refdataList)
		{
			if (refdata.getExchange() == null || refdata.getSymbol() == null) continue;
			
			symbolinfo = new SymbolInfo(serverMarket, refdata.getSymbol());
			index = Collections.binarySearch(refSymbolInfo, symbolinfo);
			if (index >= 0)
			{
				refSymbolInfo.remove(index);
			}
		}
	}
	public List<SymbolInfo> getAllSymbolInfo(String market) 
	{
		ArrayList<SymbolInfo> infoList = new ArrayList<SymbolInfo>();
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolinfo.getMarket().equals(market))
			{
				infoList.add(symbolinfo);
			}
		}
		return infoList;
	}
	public List<String> getSymbolList(String market)
	{
		ArrayList<String> retList = new ArrayList<String>();
		for (SymbolInfo symbolinfo : refSymbolInfo)
		{
			if (symbolinfo.getExchange().equals(market))
			{
				if (symbolinfo.hasRefSymbol() == false)
					retList.add(symbolinfo.getCode());
				else
					retList.add(symbolinfo.getHint() + "." + symbolinfo.getExchange());
			}
		}
		return retList;
	}
	public int at(SymbolInfo symbolinfo) 
	{
		int index = Collections.binarySearch(refSymbolInfo, symbolinfo);
		return index;
	}
	public SymbolInfo get(int index) {
		if (index >= 0)
		{
			return refSymbolInfo.get(index);
		}
		else
		{
			return null;
		}
	}
	public abstract SymbolInfo getbySymbol(String symbol);
	public abstract List<SymbolInfo> getBySymbolStrings(List<String> symbolList);
	public abstract List<SymbolInfo> getBySymbolInfos(List<SymbolInfo> inputInfoList);
}
