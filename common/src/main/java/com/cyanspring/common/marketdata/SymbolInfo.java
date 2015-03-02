package com.cyanspring.common.marketdata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SymbolInfo implements Cloneable, Serializable, Comparable<SymbolInfo>{
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
		this.cnSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getCnName();
	}
	public void setTwName(String twName) {
		this.twName = twName;
		this.twSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getTwName();
	}
	public void setEnName(String enName) {
		this.enName = enName;
		this.enSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getEnName();
	}
	public void setJpName(String jpName) {
		this.jpName = jpName;
		this.jpSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getJpName();
	}
	public void setEsName(String esName) {
		this.esName = esName;
		this.esSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getEsName();
	}
	public void setKrName(String krName) {
		this.krName = krName;
		this.krSubName = (getHint() == null || getHint().isEmpty()) ? this.code : getHint();//SymbolInfoType.fromString(getHint()).getKrName();
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
}
