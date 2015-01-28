package com.cyanspring.common.account;

import com.cyanspring.common.Default;
import com.cyanspring.common.data.DataObject;

public class AccountSetting extends DataObject {

	protected AccountSetting() {
		
	}
	public AccountSetting(String accountId) {
		setId(accountId);
		setDefaultQty(Default.getOrderQuantity());
		setStopLossValue(Default.getPositionStopLoss());
		setCompanySLValue(0.0);
		setMargin(0.0);
		setRoute("");
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
	public Double getCompanySLValue(){
		return get(Double.class, AccountSettingType.COMPANY_SL_VALUE.value());
	}	  
	public void setCompanySLValue(Double companySLValue){
		put(AccountSettingType.COMPANY_SL_VALUE.value(), companySLValue);
	}
	public Double getMargin(){
		return get(Double.class, AccountSettingType.MARGIN.value());
	}
	public void setMargin(Double margin){
		put(AccountSettingType.MARGIN.value(), margin);
	}
	public String getRoute() {
		return get(String.class, AccountSettingType.ROUTE.value());
	}
	public void setRoute(String route) {
		put(AccountSettingType.ROUTE.value(), route);
	}
	public AccountSetting clone() {
		return (AccountSetting)super.clone();
	} 
	@Override
	public String toString() {
		return this.getFields().toString();
	}
}
