package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.SymbolInfo;

public class SymbolListSubscribeEvent extends RemoteAsyncEvent {
	private boolean isSuccess ;
	private String errorMsg ;
	private SymbolListSubscribeType type ;
	private String userID ;
	private String market ;
	private String group ;
	private String txId ;
	private List<SymbolInfo> symbolList ;

	public SymbolListSubscribeEvent(String key, String receiver) {
		super(key, receiver);
	}
	public SymbolListSubscribeEvent(String key, String receiver,
			String userID, String market, String group, String txId, List<SymbolInfo> symbolList) {
		super(key, receiver);
		this.userID = userID;
		this.market = market;
		this.group = group;
		this.txId = txId;
		this.symbolList = symbolList;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public SymbolListSubscribeType getType() {
		return type;
	}

	public void setType(SymbolListSubscribeType type) {
		this.type = type;
	}

	public List<SymbolInfo> getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(List<SymbolInfo> symbolList) {
		this.symbolList = symbolList;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
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

}
