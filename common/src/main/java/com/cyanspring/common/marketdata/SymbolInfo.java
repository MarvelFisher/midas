package com.cyanspring.common.marketdata;

public class SymbolInfo implements Cloneable{
	private String market;
	private String code;
	private String windCode;
	private String cnName;
	private String twName;
	private String enName;
	
	public SymbolInfo(String market, 
			String code, String windCode, String cnName, String enName, String twName) 
	{
		this.market = market;
		this.code = code;
		this.windCode = windCode;
		this.cnName = cnName;
		this.enName = enName;
		this.twName = twName;
	}
	public SymbolInfo(SymbolInfo symbolinfo)
	{
		this.market = symbolinfo.getMarket();
		this.code = symbolinfo.getCode();
		this.windCode = symbolinfo.getWindCode();
		this.cnName = symbolinfo.getCnName();
		this.enName = symbolinfo.getEnName();
	}

	public String getMarket() {
		return market;
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
	public void setTwName(String twName) {
		this.twName = twName;
	}
}
