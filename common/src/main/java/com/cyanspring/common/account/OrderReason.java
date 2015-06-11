package com.cyanspring.common.account;

public enum OrderReason {
	ManualClose(1),
	StopOrder(2),
	PositionStopLoss(3),
	MarginCall(4),
	TrailingStop(5),
	DayEnd(6),
	CompanyStopLoss(7),
	CompanyPositionStopLoss(8),
	CompanyDailyStopLoss(9),
	AccountStopLoss(10),
	DailyStopLoss(11)
	;
	
	private final int value;
	private OrderReason(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}
}
