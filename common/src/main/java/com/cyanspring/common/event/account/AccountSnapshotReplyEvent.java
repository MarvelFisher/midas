package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountSnapshotReplyEvent extends RemoteAsyncEvent {
	private Account account;
	private AccountSetting accountSetting;
	private List<OpenPosition> openPositions;
	private List<ClosedPosition> closedPositions;
	private List<Execution> executions;

	public AccountSnapshotReplyEvent(String key, String receiver,
			Account account, AccountSetting accountSetting,
			List<OpenPosition> openPositions,
			List<ClosedPosition> closedPositions, List<Execution> executions) {
		super(key, receiver);
		this.account = account;
		this.accountSetting = accountSetting;
		this.openPositions = openPositions;
		this.closedPositions = closedPositions;
		this.executions = executions;
	}

	public Account getAccount() {
		return account;
	}
	public AccountSetting getAccountSetting() {
		return accountSetting;
	}
	public List<OpenPosition> getOpenPositions() {
		return openPositions;
	}
	public List<ClosedPosition> getClosedPositions() {
		return closedPositions;
	}
	public List<Execution> getExecutions() {
		return executions;
	}
}
