package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.Date;

public class AccountDaily extends BaseAccount implements Serializable {
	private Date onDate;
	private Date onTime;
	private String tradeDate;
	
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
	
	public void setOnTime(Date onTime) {
		this.onTime = onTime;
	}
	
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}
	
	public Date getOnTime() {
		return onTime;
	}
	
	public String getTradeDate() {
		return tradeDate;
	}
	
}
