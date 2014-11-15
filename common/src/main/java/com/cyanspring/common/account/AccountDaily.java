package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.Date;

public class AccountDaily extends BaseAccount implements Serializable {
	private Date onDate;
	
	protected AccountDaily() {
		super();
	}

	public AccountDaily(String id, String userId, Date onDate) {
		super(id, userId);
		this.onDate = onDate;
	}

	public Date getOnDate() {
		return onDate;
	}

	protected void setOnDate(Date onDate) {
		this.onDate = onDate;
	}
	
}
