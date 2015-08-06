package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class GroupListRequestEvent extends RemoteAsyncEvent 
{
	private String userID ;
	private String market ;
	private String txId ;
	private int queryType = -1;
	public GroupListRequestEvent(String key, String receiver) 
	{
		super(key, receiver);
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
	public int getQueryType() {
		return queryType;
	}
	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}
	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}
}
