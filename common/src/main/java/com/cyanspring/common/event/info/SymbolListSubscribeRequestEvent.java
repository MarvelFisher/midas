package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SymbolListSubscribeRequestEvent extends RemoteAsyncEvent {
	private SymbolListSubscribeType type ;
	private String userID ;
	private String market ;
	private String group ;
	private String txId ;
	private int queryType = -1;
	private List<String> symbolList ;

	public SymbolListSubscribeRequestEvent(String key, String receiver) {
		super(key, receiver);
	}
	public SymbolListSubscribeRequestEvent(String key, String receiver,
			String userID, String market, String group, String txId, List<String> symbolList) {
		super(key, receiver);
		this.userID = userID;
		this.market = market;
		this.group = group;
		this.txId = txId;
		this.symbolList = symbolList;
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

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}
	public int getQueryType() {
		return queryType;
	}
	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}

}
