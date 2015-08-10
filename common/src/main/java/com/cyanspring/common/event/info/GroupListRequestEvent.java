package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.info.GroupInfo;

@SuppressWarnings("serial")
public class GroupListRequestEvent extends RemoteAsyncEvent 
{
	private String userID ;
	private String market ;
	private String txId ;
	private int queryType = -1;
	private List<GroupInfo> groupList = null;
	private GroupListType type;
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
	public List<GroupInfo> getGroupList() {
		return groupList;
	}
	public void setGroupList(List<GroupInfo> groupList) {
		this.groupList = groupList;
	}
	public GroupListType getType() {
		return type;
	}
	public void setType(GroupListType type) {
		this.type = type;
	}
}
