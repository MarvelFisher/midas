package com.cyanspring.common.event.order;

import java.util.List;

import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllExecutionSnapshotReplyEvent extends RemoteAsyncEvent{

	private boolean isOk ;
	private String message ;
	private List<Execution> executionList;
	
	public AllExecutionSnapshotReplyEvent(String key, String receiver,boolean isOk,String message,List<Execution> executionList) {		
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.executionList = executionList;
	}

	public boolean isOk() {
		return isOk;
	}

	public String getMessage() {
		return message;
	}

	public List<Execution> getExecutionList() {
		return executionList;
	}
}
