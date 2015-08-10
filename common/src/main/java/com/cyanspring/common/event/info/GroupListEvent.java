package com.cyanspring.common.event.info;

import java.util.List;
import java.util.Map.Entry;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.info.GroupInfo;

@SuppressWarnings("serial")
public class GroupListEvent extends RemoteAsyncEvent 
{
	private String userID ;
	private String market ;
	private String txId ;
	private int queryType = -1;
	private List<GroupInfo> groupList;
	private boolean ok ;
	private String message = null;
	public GroupListEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public List<GroupInfo> getGroupList() {
		return groupList;
	}
	public void setGroupList(List<GroupInfo> groupList) {
		this.groupList = groupList;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
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
