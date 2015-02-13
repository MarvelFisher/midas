package com.cyanspring.common.marketdata;

public enum SymbolInfoType {
	IFL0("", "", "", "", "", "")
	;

	public static SymbolInfoType fromString(String s)
	{
		for(SymbolInfoType type : values())
		{
			if(type.name().substring(1).equalsIgnoreCase(s))
				return type;
		}
		return null;
	}
	private SymbolInfoType(String cnName, String twName, String enName, String jpName, String esName, String krName)
	{
		this.cnName = cnName;
		this.twName = twName;
		this.enName = enName;
		this.jpName = jpName;
		this.esName = esName;
		this.krName = krName;
	}
	public String getCnName() {
		return cnName;
	}
	public String getTwName() {
		return twName;
	}
	public String getEnName() {
		return enName;
	}
	public String getJpName() {
		return jpName;
	}
	public String getEsName() {
		return esName;
	}
	public String getKrName() {
		return krName;
	}
	private String cnName;
	private String twName;
	private String enName;
	private String jpName;
	private String esName;
	private String krName;
}
