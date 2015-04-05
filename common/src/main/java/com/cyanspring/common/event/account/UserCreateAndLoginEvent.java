package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

//Third-Party Authentication
public class UserCreateAndLoginEvent extends RemoteAsyncEvent {
	private User user;
	private String txId;
	private String country;
	private String language;
	private String original_id;

	public UserCreateAndLoginEvent(String key, String receiver, User user, String country, String language, String org_id, String txId) {
		super(key, receiver);
		this.user = user;
		this.country = country;
		this.language = language;
		this.txId = txId;
		this.original_id = org_id;
		setPriority(EventPriority.HIGH);
	}
	
	public String getOriginalID()
	{
		return original_id;
	}

	public User getUser() {
		return user;
	}

	public String getTxId() {
		return txId;
	}
	
	public String getCountry() {
		return country;
	}
	
	public String getLanguage() {
		return language;
	}

}
