package com.cyanspring.common.account;

import com.cyanspring.common.data.DataObject;

public class AccountSetting extends DataObject {

	protected AccountSetting() {
		
	}
	public AccountSetting(String accountId) {
		setId(accountId);
	}	
	public String getId() {
		return get(String.class, AccountSettingType.ID.value());
	}
	public void setId(String id) {
		put(AccountSettingType.ID.value(), id);
	}
	public Double getDefaultQty() {
		return get(Double.class, AccountSettingType.DEFAULT_QTY.value());
	}
	public void setDefaultQty(Double defaultQty) {
		put(AccountSettingType.DEFAULT_QTY.value(), defaultQty);
	}
	public Double getStopLossValue() {
		return get(Double.class, AccountSettingType.STOP_LOSS_VALUE.value());
	}
	public void setStopLossValue(Double stopLossValue) {
		put(AccountSettingType.STOP_LOSS_VALUE.value(), stopLossValue);
	}
	
	public AccountSetting clone() {
		return (AccountSetting)super.clone();
	}
	
	@Override
	public String toString() {
		return this.getFields().toString();
	}
}
