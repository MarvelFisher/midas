package com.cyanspring.common.event.account;

import java.util.Map;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class DeleteGroupManagementReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private boolean isOk ; 
	private String message;
	private Map <GroupManagement,String> result;
	public DeleteGroupManagementReplyEvent(String key, String receiver,boolean isOk,String message,Map <GroupManagement,String> result) {
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.result = result;
	}
	
	public boolean isOk() {
		return isOk;
	}

	public String getMessage() {
		return message;
	}

	public Map<GroupManagement, String> getResult() {
		return result;
	}

}
