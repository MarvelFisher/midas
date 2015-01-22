package com.cyanspring.common.marketdata;

public class SymbolInfo {
	private String market;
	private String code;
	private String windCode;
	private String cnName;
	private String enName;
	
	public SymbolInfo(String market, 
			String code, String windCode, String cnName, String enName) 
	{
		this.market = market;
		this.code = code;
		this.windCode = windCode;
		this.cnName = cnName;
		this.enName = enName;
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
}
