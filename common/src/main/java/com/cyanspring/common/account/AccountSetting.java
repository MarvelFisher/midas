package com.cyanspring.common.account;

import com.cyanspring.common.Default;
import com.cyanspring.common.data.DataObject;

public class AccountSetting extends DataObject {

	protected AccountSetting() {
		
	}
	public AccountSetting(String accountId) {
		setId(accountId);
	}	
	
	//!!! very important: you must add entry here whenever you add a setting
	public static AccountSetting createEmptySettings(String accountId) {
		AccountSetting settings = new AccountSetting(accountId);
		settings.setDefaultQty(Default.getOrderQuantity());
		settings.setStopLossValue(Default.getPositionStopLoss());
		settings.setCompanySLValue(0.0);
		settings.setMargin(0.0);
		settings.setRoute("");
		settings.setCommission(0.0);
		settings.setLeverageRate(0.0);
		settings.setDailyStopLoss(0.0);
		settings.setTrailingStop(0.0);

		return settings;
	}
	
	public String getId() {
		return get(String.class, AccountSettingType.ID.value());
	}
	public void setId(String id) {
		put(AccountSettingType.ID.value(), id);
	}
	public Double getDefaultQty() {
		Double result = get(Double.class, AccountSettingType.DEFAULT_QTY.value());
		return null == result? Default.getOrderQuantity() : result;
	}
	public void setDefaultQty(Double defaultQty) {
		put(AccountSettingType.DEFAULT_QTY.value(), defaultQty);
	}
	public Double getStopLossValue() {
		Double result = get(Double.class, AccountSettingType.STOP_LOSS_VALUE.value());
		return null == result? Default.getPositionStopLoss() : result;
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
	public double getMargin(){
		return get(double.class, AccountSettingType.MARGIN.value());
	}
	public void setMargin(double margin){
		put(AccountSettingType.MARGIN.value(), margin);
	}
	public String getRoute() {
		return get(String.class, AccountSettingType.ROUTE.value());
	}
	public void setRoute(String route) {
		put(AccountSettingType.ROUTE.value(), route);
	}
	public double getLeverageRate(){
		return get(double.class, AccountSettingType.LEVERAGE_RATE.value());
	}
	public void setLeverageRate(double lRate){
		put(AccountSettingType.LEVERAGE_RATE.value(), lRate);
	}
	public double getCommission(){
		return get(double.class, AccountSettingType.COMMISSION.value());
	}
	public void setCommission(double commission){
		put(AccountSettingType.COMMISSION.value(), commission);
	}	
	public double getDailyStopLoss(){
		return get(double.class, AccountSettingType.DAILY_STOPLOSS.value());
	}
	public void setDailyStopLoss(double dailyStopLoss){
		put(AccountSettingType.DAILY_STOPLOSS.value(), dailyStopLoss);
	}
	
	public double getTrailingStop() {
		return get(double.class, AccountSettingType.TRAILING_STOP.value());
	}
	
	public void setTrailingStop(double trailingStop){
		put(AccountSettingType.TRAILING_STOP.value(), trailingStop);
	}
	
	public AccountSetting clone() {
		return (AccountSetting)super.clone();
	} 
	@Override
	public String toString() {
		return this.getFields().toString();
	}
}
