package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreateGroupManagementEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;

	private List<GroupManagement> groupManagementList;

	public CreateGroupManagementEvent(String key, String receiver,List<GroupManagement> groupManagementList) {
		super(key, receiver);
		this.groupManagementList = groupManagementList;
	}

	public List<GroupManagement> getGroupManagementList() {
		return groupManagementList;
	}
}
