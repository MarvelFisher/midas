package com.cyanspring.common.event.account;

import java.util.Date;

import com.cyanspring.common.account.PremiumFollowInfo;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PremiumFollowRequestEvent extends RemoteAsyncEvent {
	private PremiumFollowInfo info;
	private String txId;
	private Date time;
	
	public PremiumFollowRequestEvent(String key, String receiver,
			PremiumFollowInfo info, String txId) {
		super(key, receiver);
		this.info = info;
		this.txId = txId;
	}

	public PremiumFollowInfo getInfo() {
		return info;
	}
	public String getTxId() {
		return txId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
}
