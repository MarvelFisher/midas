package com.cyanspring.common.marketdata;

public class SymbolInfo implements Cloneable{
	private String market;
	private String code;
	private String windCode;
	private String hint;
	private String cnName;
	private String cnSubName;
	private String twName;
	private String twSubName;
	private String enName;
	private String enSubName;
	private String krName;
	private String krSubName;
	private String jpName;
	private String jpSubName;
	private String esName;
	private String esSubName;
	
	public SymbolInfo(String market, 
			String code, String windCode, String hint, String cnName, String enName, String twName, String jpName, String krName, String esName) 
	{
		this.market = market;
		this.code = code;
		this.windCode = windCode;
		this.hint = hint;
		this.cnName = cnName;
		this.enName = enName;
		this.twName = twName;
		this.jpName = jpName;
		this.krName = krName;
		this.esName = esName;
		if (hint == null || hint.isEmpty())
		{
			this.cnSubName = code;
			this.twSubName = code;
			this.enSubName = code;
			this.jpSubName = code;
			this.esSubName = code;
			this.krSubName = code;
		}
		else
		{
			this.cnSubName = SymbolInfoType.fromString(hint).getCnName();
			this.twSubName = SymbolInfoType.fromString(hint).getTwName();
			this.enSubName = SymbolInfoType.fromString(hint).getEnName();
			this.jpSubName = SymbolInfoType.fromString(hint).getJpName();
			this.esSubName = SymbolInfoType.fromString(hint).getEsName();
			this.krSubName = SymbolInfoType.fromString(hint).getKrName();
		}
	}
	public SymbolInfo(SymbolInfo symbolinfo)
	{
		this.market = symbolinfo.getMarket();
		this.code = symbolinfo.getCode();
		this.windCode = symbolinfo.getWindCode();
		this.hint = symbolinfo.getHint();
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
}
