package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class OverAllPositionReplyEvent extends RemoteAsyncEvent {

	private boolean isOk;
	private String message;
	private List<OpenPosition> openPositionList;
	private List<ClosedPosition> closedPositionList;

	public OverAllPositionReplyEvent(String key, String receiver,boolean isOk
			,String message,List<OpenPosition> openPositionList,List<ClosedPosition> closedPositionList ) {
		super(key, receiver);
		this.openPositionList = openPositionList;
		this.closedPositionList = closedPositionList;
	}

	public boolean isOk() {
		return isOk;
	}

	public String getMessage() {
		return message;
	}

	public List<OpenPosition> getOpenPositionList() {
		return openPositionList;
	}

	public List<ClosedPosition> getClosedPositionList() {
		return closedPositionList;
	}
}
