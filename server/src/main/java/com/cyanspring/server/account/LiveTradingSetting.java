package com.cyanspring.server.account;

public class LiveTradingSetting {
	private boolean needCheckPosition;
	private boolean needCheckFreeze;
	private boolean needCheckTerminate;
	private String  closeAllPositionTime;
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
	public String getCloseAllPositionTime() {
		return closeAllPositionTime;
	}
	public void setCloseAllPositionTime(String closeAllPositionTime) {
		this.closeAllPositionTime = closeAllPositionTime;
	}


	
}
