package com.cyanspring.common.marketdata;

import java.io.Serializable;
import java.util.Map;

import com.cyanspring.common.info.RefSubName;

public class SymbolInfo implements Cloneable, Serializable, Comparable<SymbolInfo>{
	private static Map<String, RefSubName> subNameMap = null;
	private String market = null;
	private String exchange = null;
	private String code = null;
	private String windCode = null;
	private String hint = null;
	private String cnName = null;
	private String cnSubName = null;
	private String twName = null;
	private String twSubName = null;
	private String enName = null;
	private String enSubName = null;
	private String krName = null;
	private String krSubName = null;
	private String jpName = null;
	private String jpSubName = null;
	private String esName = null;
	private String esSubName = null;
	private int lotSize = -1;
	private String tickTable = "";
	private double PriceLimit = 0.0;
	private String CNTradingUnit = null;
	private String ENTradingUnit = null;
	private String TWTradingUnit = null;
	private String SettlementDate = null;
	private double CommissionFee = 0.0;
	private double NumeratorDP = 0.0;
	private int DecimalPoint = 0;
	private double MinimalCF = 0.0;
	private double PricePerUnit = 0.0;
	private int MaximumLot = 0;
	private int MaximumHold = 0;
	private String Strategy = null;
	private double MarginRate = 0.0;
	private String Category = null;
	private double Denominator = 0.0;
	private String Tradable = null;
	
	public SymbolInfo(String market, String code) 
	{
		this.market = market;
		this.code = code;
	}
	public SymbolInfo(SymbolInfo symbolinfo)
	{
		this.market = symbolinfo.getMarket();
		this.code = symbolinfo.getCode();
		this.setWindCode(symbolinfo.getWindCode());
		this.setHint(symbolinfo.getHint());
		this.cnName = symbolinfo.getCnName();
		this.cnSubName = symbolinfo.getCnSubName();
		this.enName = symbolinfo.getEnName();
		this.enSubName = symbolinfo.getEnSubName();
		this.twName = symbolinfo.getTwName();
		this.twSubName = symbolinfo.getTwSubName();
		this.jpName = symbolinfo.getJpName();
		this.jpSubName = symbolinfo.getJpSubName();
		this.krName = symbolinfo.getKrName();
		this.krSubName = symbolinfo.getKrSubName();
		this.esName = symbolinfo.getEsName();
		this.esSubName = symbolinfo.getEsSubName();
	}

	public String getMarket() {
		return market;
	}
	public String getExchange() {
		return exchange;
	}
	public String getCode() {
		return code;
	}
	public String getWindCode() {
		return windCode;
	}
	public String getCnName() {
		return cnName;
	}
	public String getEnName() {
		return enName;
	}
	public String getTwName() {
		return twName;
	}
	public String getHint() {
		return hint;
	}
	public String getCnSubName() {
		return cnSubName;
	}
	public String getTwSubName() {
		return twSubName;
	}
	public String getEnSubName() {
		return enSubName;
	}
	public String getKrName() {
		return krName;
	}
	public String getKrSubName() {
		return krSubName;
	}
	public String getJpName() {
		return jpName;
	}
	public String getJpSubName() {
		return jpSubName;
	}
	public String getEsName() {
		return esName;
	}
	public String getEsSubName() {
		return esSubName;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public void setCnName(String cnName) {
		this.cnName = cnName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.cnSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.cnSubName = subNameMap.get(getHint()).getCNSubName();
			}
			else
			{
				this.cnSubName = "";
			}
		}
	}
	public void setTwName(String twName) {
		this.twName = twName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.twSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.twSubName = subNameMap.get(getHint()).getTWSubName();
			}
			else
			{
				this.twSubName = "";
			}
		}
	}
	public void setEnName(String enName) {
		this.enName = enName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.enSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.enSubName = subNameMap.get(getHint()).getENSubName();
			}
			else
			{
				this.enSubName = "";
			}
		}
	}
	public void setJpName(String jpName) {
		this.jpName = jpName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.jpSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.jpSubName = subNameMap.get(getHint()).getENSubName();
			}
			else
			{
				this.jpSubName = "";
			}
		}
	}
	public void setEsName(String esName) {
		this.esName = esName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.esSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.esSubName = subNameMap.get(getHint()).getENSubName();
			}
			else
			{
				this.esSubName = "";
			}
		}
	}
	public void setKrName(String krName) {
		this.krName = krName;
		if (getHint() == null || getHint().isEmpty() || subNameMap == null)
		{
			this.krSubName = "";
		}
		else
		{
			if (subNameMap.get(getHint()) != null)
			{
				this.krSubName = subNameMap.get(getHint()).getENSubName();
			}
			else
			{
				this.krSubName = "";
			}
		}
	}
	public void setWindCode(String windCode) {
		this.windCode = windCode;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public int getLotSize() {
		return lotSize;
	}
	public void setLotSize(int lotSize) {
		this.lotSize = lotSize;
	}
	public String getTickTable() {
		return tickTable;
	}
	public void setTickTable(String tickTable) {
		this.tickTable = tickTable;
	}
	@Override
	public int compareTo(SymbolInfo o) {
		int i = 0;
		if (o.market == null) return 1;
		else if (this.market == null) return -1;
		else i = this.market.compareTo(o.market);
		if (i != 0) return i;
		if (o.code == null) return 1;
		else if (this.code == null) return -1;
		else i = this.code.compareTo(o.code);
		return i;
	}
	public double getPriceLimit() {
		return PriceLimit;
	}
	public void setPriceLimit(double priceLimit) {
		PriceLimit = priceLimit;
	}
	public String getCNTradingUnit() {
		return CNTradingUnit;
	}
	public void setCNTradingUnit(String cNTradingUnit) {
		CNTradingUnit = cNTradingUnit;
	}
	public String getENTradingUnit() {
		return ENTradingUnit;
	}
	public void setENTradingUnit(String eNTradingUnit) {
		ENTradingUnit = eNTradingUnit;
	}
	public String getTWTradingUnit() {
		return TWTradingUnit;
	}
	public void setTWTradingUnit(String tWTradingUnit) {
		TWTradingUnit = tWTradingUnit;
	}
	public String getSettlementDate() {
		return SettlementDate;
	}
	public void setSettlementDate(String settlementDate) {
		SettlementDate = settlementDate;
	}
	public double getCommissionFee() {
		return CommissionFee;
	}
	public void setCommissionFee(double commissionFee) {
		CommissionFee = commissionFee;
	}
	public double getNumeratorDP() {
		return NumeratorDP;
	}
	public void setNumeratorDP(double numeratorDP) {
		NumeratorDP = numeratorDP;
	}
	public int getDecimalPoint() {
		return DecimalPoint;
	}
	public void setDecimalPoint(int decimalPoint) {
		DecimalPoint = decimalPoint;
	}
	public double getMinimalCF() {
		return MinimalCF;
	}
	public void setMinimalCF(double minimalCF) {
		MinimalCF = minimalCF;
	}
	public double getPricePerUnit() {
		return PricePerUnit;
	}
	public void setPricePerUnit(double pricePerUnit) {
		PricePerUnit = pricePerUnit;
	}
	public int getMaximumLot() {
		return MaximumLot;
	}
	public void setMaximumLot(int maximalLot) {
		MaximumLot = maximalLot;
	}
	public String getStrategy() {
		return Strategy;
	}
	public void setStrategy(String strategy) {
		Strategy = strategy;
	}
	public double getMarginRate() {
		return MarginRate;
	}
	public void setMarginRate(double marginRate) {
		MarginRate = marginRate;
	}
	public String getCategory() {
		return Category;
	}
	public void setCategory(String category) {
		Category = category;
	}
	public double getDenominator() {
		return Denominator;
	}
	public void setDenominator(double denominator) {
		Denominator = denominator;
	}
	public String getTradable() {
		return Tradable;
	}
	public void setTradable(String tradable) {
		Tradable = tradable;
	}
	public static Map<String, RefSubName> getSubNameMap() {
		return subNameMap;
	}
	public static void setSubNameMap(Map<String, RefSubName> subNameMap) {
		SymbolInfo.subNameMap = subNameMap;
	}
	public int getMaximumHold() {
		return MaximumHold;
	}
	public void setMaximumHold(int maximumHold) {
		MaximumHold = maximumHold;
	}
}
