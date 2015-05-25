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
			symbolinfo.setLimitMaximumLot(refdata.getLimitMaximumLot());
			symbolinfo.setMarketMaximumLot(refdata.getMarketMaximumLot());
//			symbolinfo.setMaximumLot(refdata.getMaximumLot());
			symbolinfo.setMaximumHold(refdata.getMaximumHold());
			symbolinfo.setStrategy(refdata.getStrategy());
			symbolinfo.setMarginRate(refdata.getMarginRate());
			symbolinfo.setCategory(refdata.getCategory());
			symbolinfo.setDenominator(refdata.getDenominator());
			symbolinfo.setTradable(refdata.getTradable());
			symbolinfo.setSpellName(refdata.getSpellName());
			index = Collections.binarySearch(refSymbolInfo, symbolinfo);
			if (index < 0)
			{
				refSymbolInfo.add(~index, symbolinfo);
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
	public abstract List<SymbolInfo> getBySymbolStrings(List<String> symbolList);
	public abstract List<SymbolInfo> getBySymbolInfos(List<SymbolInfo> inputInfoList);
}
