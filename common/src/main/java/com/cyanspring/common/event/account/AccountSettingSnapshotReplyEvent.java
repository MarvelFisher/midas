package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.message.ExtraEventMessage;
import com.cyanspring.common.message.ExtraEventMessageBuilder;

public class AccountSettingSnapshotReplyEvent extends RemoteAsyncEvent {
	private AccountSetting accountSetting;
	private boolean ok;
	private String message;
	private String txId;
	private ExtraEventMessageBuilder messageBuilder;
	
	public AccountSettingSnapshotReplyEvent(String key, String receiver,
			AccountSetting accountSetting, boolean ok, String message, String txId,ExtraEventMessageBuilder messageBuilder) {
		super(key, receiver);
		this.accountSetting = accountSetting;
		this.ok = ok;
		this.message = message;
		this.txId = txId;
		this.messageBuilder = messageBuilder;
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}

	public boolean isOk() {
		return ok;
	}

	public String getMessage() {
		return message;
	}

	public String getTxId() {
		return txId;
	}

	public String getStopLiveTradingStartTime(){
		
		if( null == messageBuilder)
			return null;
		
		return messageBuilder.getMessage(ExtraEventMessage.USER_STOP_LIVE_TRADING_START_TIME);
	}
	
	public String getStopLiveTradingEndTime(){
		
		if( null == messageBuilder)
			return null;
		
		return messageBuilder.getMessage(ExtraEventMessage.USER_STOP_LIVE_TRADING_END_TIME);
	}
}
