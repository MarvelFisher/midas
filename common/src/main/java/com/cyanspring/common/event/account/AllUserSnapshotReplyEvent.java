package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllUserSnapshotReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private List<User> users = new ArrayList<User>();

	public AllUserSnapshotReplyEvent(String key, String receiver,List<User> users) {
		super(key, receiver);
		this.users = users;
	}

	public List<User> getUsers() {
		return users;
	}

}
