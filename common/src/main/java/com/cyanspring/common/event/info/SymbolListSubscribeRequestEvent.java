package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SymbolListSubscribeRequestEvent extends RemoteAsyncEvent {
	private SymbolListSubscribeType type ;
	private String userID ;
	private String market ;
	private String group ;
	private List<String> symbolList ;

	public SymbolListSubscribeRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

	public SymbolListSubscribeType getType() {
		return type;
	}

	public void setType(SymbolListSubscribeType type) {
		this.type = type;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public List<String> getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(List<String> symbolList) {
		this.symbolList = symbolList;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
