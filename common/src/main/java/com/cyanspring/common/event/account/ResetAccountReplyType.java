package com.cyanspring.common.event.account;

public enum ResetAccountReplyType {
	LTSCORE(1),
	LTSINFO_USERMANAGER(2),
	LTSINFO_ALERTMANAGER(3)
	;
	
	private int replyType;
	ResetAccountReplyType(int Type)
	{
		this.setReplyType(Type) ;
	}
	
	public int getReplyType() {
		return replyType;
	}

	public void setReplyType(int replyType) {
		this.replyType = replyType;
	}	
}
