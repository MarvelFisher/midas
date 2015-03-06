package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.staticdata.RefData;

public class FCRefSymbolInfo  implements IRefSymbolInfo
{
	private List<SymbolInfo> refSymbolInfo = new ArrayList<SymbolInfo>();
	private String serverMarket;
	public FCRefSymbolInfo(String serverMarket)
	{
		this.serverMarket = serverMarket;
	}

	@Override
	public void reset() 
	{
		refSymbolInfo.clear();
	}
	@Override
	public void setByRefData(List<RefData> refdataList) 
	{
		SymbolInfo symbolinfo = null;
		String strTmp = null;
		int index;
		for (RefData refdata : refdataList)
		{
			if (refdata.getExchange() == null || refdata.getSymbol() == null) continue;
			
			symbolinfo = new SymbolInfo(serverMarket, refdata.getSymbol());
			symbolinfo.setExchange(refdata.getExchange());
			symbolinfo.setWindCode(null);
			symbolinfo.setHint(refdata.getRefSymbol());
			strTmp = (refdata.getENDisplayName() == null) ? refdata.getSymbol() : refdata.getENDisplayName();
			symbolinfo.setEnName(strTmp);
			symbolinfo.setCnName((refdata.getCNDisplayName() == null) ? strTmp : refdata.getCNDisplayName());
			symbolinfo.setTwName((refdata.getTWDisplayName() == null) ? strTmp : refdata.getTWDisplayName());
			symbolinfo.setJpName(strTmp);
			symbolinfo.setKrName(strTmp);
			symbolinfo.setEsName(strTmp);
			symbolinfo.setLotSize(refdata.getLotSize());
			symbolinfo.setTickTable(refdata.getTickTable());
			index = Collections.binarySearch(refSymbolInfo, symbolinfo);
			if (index < 0)
			{
				refSymbolInfo.add(~index, symbolinfo);
			}
		}
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
	@Override
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

}
