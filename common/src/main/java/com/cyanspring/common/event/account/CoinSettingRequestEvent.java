package com.cyanspring.common.event.account;

import java.util.Date;

import com.cyanspring.common.business.CoinType;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CoinSettingRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String txId;
	private String accountId;
	private Date endDate;
	private CoinType coinType;
	public CoinSettingRequestEvent(String key, String receiver,String txId,String accountId,Date endDate,CoinType coinType) {
		super(key, receiver);
		this.txId = txId;
		this.accountId = accountId;
		this.endDate = endDate;
		this.coinType = coinType;
	}
	public String getTxId() {
		return txId;
	}
	public Date getEndDate() {
		return endDate;
	}
	public CoinType getCoinType() {
		return coinType;
	}
	public String getAccountId() {
		return accountId;
	}
}
