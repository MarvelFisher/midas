package com.cyanspring.server.livetrading;

public class LiveTradingSetting {
	private boolean needCheckPosition;
	private boolean needCheckFreeze;
	private boolean needCheckTerminate;
	private String 	userStopLiveTradingStartTime;
	private String 	userStopLiveTradingEndTime;

	
	
	public String getUserStopLiveTradingStartTime() {
		return userStopLiveTradingStartTime;
	}
	public void setUserStopLiveTradingStartTime(String userStopLiveTradingStartTime) {
		this.userStopLiveTradingStartTime = userStopLiveTradingStartTime;
	}
	public String getUserStopLiveTradingEndTime() {
		return userStopLiveTradingEndTime;
	}
	public void setUserStopLiveTradingEndTime(String userStopLiveTradingEndTime) {
		this.userStopLiveTradingEndTime = userStopLiveTradingEndTime;
	}
	public boolean isNeedCheckPosition() {
		return needCheckPosition;
	}
	public void setNeedCheckPosition(boolean needCheckPosition) {
		this.needCheckPosition = needCheckPosition;
	}
	public boolean isNeedCheckFreeze() {
		return needCheckFreeze;
	}
	public void setNeedCheckFreeze(boolean needCheckFreeze) {
		this.needCheckFreeze = needCheckFreeze;
	}
	public boolean isNeedCheckTerminate() {
		return needCheckTerminate;
	}
	public void setNeedCheckTerminate(boolean needCheckTerminate) {
		this.needCheckTerminate = needCheckTerminate;
	}
}
