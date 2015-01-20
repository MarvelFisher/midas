package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SymbolEvent extends RemoteAsyncEvent {

	public SymbolEvent(String key, String receiver, String market, 
			String code, String windCode, String cnName, String enName) {
		super(key, receiver);
		this.market = market;
		this.code = code;
		this.windCode = windCode;
		this.cnName = cnName;
		this.enName = enName;
	}
	
	private String market;
	private String code;
	private String windCode;
	private String cnName;
	private String enName;
	
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
