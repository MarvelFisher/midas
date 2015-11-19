package com.cyanspring.common.business;

import java.util.TimeZone;

import com.cyanspring.common.account.LiveTradingType;

public class GlobalSetting {
	private  String user = "default";
	private  String account = "default";
	private  String market = "FX";
	private  String currency = "USD";
	private  String accountPrefix = "A";
	private  double accountCash = 100000.0;
	private  double marginTimes = 40.0;
	private  double marginCut = 100000;
	private  double commission = 0.0;
	private  double commissionMin = 0.0;
	private  TimeZone timeZone;
	private  double orderQuantity = 100000;
	private  double positionStopLoss = 1000.0;
	private  double marginCall = 0.95;
	private  int settlementDays = 0;	
	private  double stopLossPercent = 0.2;
	private  double freezePercent = 0.2;
	private  double terminatePecent = 0.2;
	private  double companyStopLossValue = 0;
	private  double freezeValue = 0;
	private  double terminateValue = 0;
	private  boolean liveTrading = false;
	private  boolean userLiveTrading = false;
	private  LiveTradingType liveTradingType = LiveTradingType.DEFAULT;
	private  double creditPartial = 0.55;
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	/**
	 * @return the market
	 */
	public String getMarket() {
		return market;
	}
	/**
	 * @param market the market to set
	 */
	public void setMarket(String market) {
		this.market = market;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	/**
	 * @return the accountPrefix
	 */
	public String getAccountPrefix() {
		return accountPrefix;
	}
	/**
	 * @param accountPrefix the accountPrefix to set
	 */
	public void setAccountPrefix(String accountPrefix) {
		this.accountPrefix = accountPrefix;
	}
	/**
	 * @return the accountCash
	 */
	public double getAccountCash() {
		return accountCash;
	}
	/**
	 * @param accountCash the accountCash to set
	 */
	public void setAccountCash(double accountCash) {
		this.accountCash = accountCash;
	}
	/**
	 * @return the marginTimes
	 */
	public double getMarginTimes() {
		return marginTimes;
	}
	/**
	 * @param marginTimes the marginTimes to set
	 */
	public void setMarginTimes(double marginTimes) {
		this.marginTimes = marginTimes;
	}
	/**
	 * @return the marginCut
	 */
	public double getMarginCut() {
		return marginCut;
	}
	/**
	 * @param marginCut the marginCut to set
	 */
	public void setMarginCut(double marginCut) {
		this.marginCut = marginCut;
	}
	/**
	 * @return the commission
	 */
	public double getCommission() {
		return commission;
	}
	/**
	 * @param commission the commission to set
	 */
	public void setCommission(double commission) {
		this.commission = commission;
	}
	/**
	 * @return the commissionMin
	 */
	public double getCommissionMin() {
		return commissionMin;
	}
	/**
	 * @param commissionMin the commissionMin to set
	 */
	public void setCommissionMin(double commissionMin) {
		this.commissionMin = commissionMin;
	}
	/**
	 * @return the timeZone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}
	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	/**
	 * @return the orderQuantity
	 */
	public double getOrderQuantity() {
		return orderQuantity;
	}
	/**
	 * @param orderQuantity the orderQuantity to set
	 */
	public void setOrderQuantity(double orderQuantity) {
		this.orderQuantity = orderQuantity;
	}
	/**
	 * @return the positionStopLoss
	 */
	public double getPositionStopLoss() {
		return positionStopLoss;
	}
	/**
	 * @param positionStopLoss the positionStopLoss to set
	 */
	public void setPositionStopLoss(double positionStopLoss) {
		this.positionStopLoss = positionStopLoss;
	}
	/**
	 * @return the marginCall
	 */
	public double getMarginCall() {
		return marginCall;
	}
	/**
	 * @param marginCall the marginCall to set
	 */
	public void setMarginCall(double marginCall) {
		this.marginCall = marginCall;
	}
	/**
	 * @return the settlementDays
	 */
	public int getSettlementDays() {
		return settlementDays;
	}
	/**
	 * @param settlementDays the settlementDays to set
	 */
	public void setSettlementDays(int settlementDays) {
		this.settlementDays = settlementDays;
	}
	/**
	 * @return the stopLossPercent
	 */
	public double getStopLossPercent() {
		return stopLossPercent;
	}
	/**
	 * @param stopLossPercent the stopLossPercent to set
	 */
	public void setStopLossPercent(double stopLossPercent) {
		this.stopLossPercent = stopLossPercent;
	}
	/**
	 * @return the freezePercent
	 */
	public double getFreezePercent() {
		return freezePercent;
	}
	/**
	 * @param freezePercent the freezePercent to set
	 */
	public void setFreezePercent(double freezePercent) {
		this.freezePercent = freezePercent;
	}
	/**
	 * @return the terminatePecent
	 */
	public double getTerminatePecent() {
		return terminatePecent;
	}
	/**
	 * @param terminatePecent the terminatePecent to set
	 */
	public void setTerminatePecent(double terminatePecent) {
		this.terminatePecent = terminatePecent;
	}
	/**
	 * @return the companyStopLossValue
	 */
	public double getCompanyStopLossValue() {
		return companyStopLossValue;
	}
	/**
	 * @param companyStopLossValue the companyStopLossValue to set
	 */
	public void setCompanyStopLossValue(double companyStopLossValue) {
		this.companyStopLossValue = companyStopLossValue;
	}
	/**
	 * @return the freezeValue
	 */
	public double getFreezeValue() {
		return freezeValue;
	}
	/**
	 * @param freezeValue the freezeValue to set
	 */
	public void setFreezeValue(double freezeValue) {
		this.freezeValue = freezeValue;
	}
	/**
	 * @return the terminateValue
	 */
	public double getTerminateValue() {
		return terminateValue;
	}
	/**
	 * @param terminateValue the terminateValue to set
	 */
	public void setTerminateValue(double terminateValue) {
		this.terminateValue = terminateValue;
	}
	/**
	 * @return the liveTrading
	 */
	public boolean isLiveTrading() {
		return liveTrading;
	}
	/**
	 * @param liveTrading the liveTrading to set
	 */
	public void setLiveTrading(boolean liveTrading) {
		this.liveTrading = liveTrading;
	}
	/**
	 * @return the userLiveTrading
	 */
	public boolean isUserLiveTrading() {
		return userLiveTrading;
	}
	/**
	 * @param userLiveTrading the userLiveTrading to set
	 */
	public void setUserLiveTrading(boolean userLiveTrading) {
		this.userLiveTrading = userLiveTrading;
	}
	/**
	 * @return the liveTradingType
	 */
	public LiveTradingType getLiveTradingType() {
		return liveTradingType;
	}
	/**
	 * @param liveTradingType the liveTradingType to set
	 */
	public void setLiveTradingType(LiveTradingType liveTradingType) {
		this.liveTradingType = liveTradingType;
	}
	/**
	 * @return the creditPartial
	 */
	public double getCreditPartial() {
		return creditPartial;
	}
	/**
	 * @param creditPartial the creditPartial to set
	 */
	public void setCreditPartial(double creditPartial) {
		this.creditPartial = creditPartial;
	}

}
