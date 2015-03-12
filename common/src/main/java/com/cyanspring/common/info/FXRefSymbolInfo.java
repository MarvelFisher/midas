package com.cyanspring.common.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.staticdata.RefData;

public class FXRefSymbolInfo implements IRefSymbolInfo
{
	private List<SymbolInfo> refSymbolInfo = new ArrayList<SymbolInfo>();
	private String serverMarket;
	public FXRefSymbolInfo(String serverMarket)
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
			symbolinfo.setCNTradingUnit(refdata.getCNTradingUnit());
			symbolinfo.setENTradingUnit(refdata.getENTradingUnit());
			symbolinfo.setTWTradingUnit(refdata.getTWTradingUnit());
			symbolinfo.setSettlementDate(refdata.getSettlementDate());
			symbolinfo.setCommissionFee(refdata.getCommissionFee());
			symbolinfo.setNumeratorDP(refdata.getNumberatorDp());
			symbolinfo.setDecimalPoint(refdata.getDeciamlPoint());
			symbolinfo.setMinimalCF(refdata.getMinimalCommissionFee());
			symbolinfo.setPricePerUnit(refdata.getPricePerUnit());
			symbolinfo.setMaximalLot(refdata.getMaximalLot());
			symbolinfo.setStrategy(refdata.getStrategy());
			symbolinfo.setMarginRate(refdata.getMarginRate());
			symbolinfo.setCategory(refdata.getCategory());
			symbolinfo.setDenominator(refdata.getDenominator());
			symbolinfo.setTradable(refdata.getTradable());
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

	@Override
	public int at(SymbolInfo symbolinfo) 
	{
		int index = Collections.binarySearch(refSymbolInfo, symbolinfo);
		return index;
	}

	@Override
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
}
