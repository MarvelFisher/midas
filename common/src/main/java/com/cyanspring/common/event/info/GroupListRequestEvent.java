package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class GroupListRequestEvent extends RemoteAsyncEvent 
{
	private String userID ;
	private String market ;
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
}
