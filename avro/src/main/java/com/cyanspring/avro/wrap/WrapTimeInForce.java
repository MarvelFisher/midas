package com.cyanspring.avro.wrap;

import com.cyanspring.avro.types.TimeInForce;

public enum WrapTimeInForce {

	Day(0, TimeInForce.Day, com.cyanspring.common.type.TimeInForce.DAY),

	GoodTillCancel(1, TimeInForce.GoodTillCancel,
			com.cyanspring.common.type.TimeInForce.GOOD_TILL_CANCEL),

	AtTheOpening(2, TimeInForce.AtTheOpening,
			com.cyanspring.common.type.TimeInForce.AT_THE_OPENING),

	ImmediateOrCancel(3, TimeInForce.ImmediateOrCancel,
			com.cyanspring.common.type.TimeInForce.IMMEDIATE_OR_CANCEL),

	FillOrKill(4, TimeInForce.FillOrKill,
			com.cyanspring.common.type.TimeInForce.FILL_OR_KILL),

	GoodTillCrossing(5, TimeInForce.GoodTillCrossing,
			com.cyanspring.common.type.TimeInForce.GOOD_TILL_CROSSING),

	GoodTillDate(6, TimeInForce.GoodTillDate,
			com.cyanspring.common.type.TimeInForce.GOOD_TILL_DATE),

	AtTheClose(7, TimeInForce.AtTheClose,
			com.cyanspring.common.type.TimeInForce.AT_THE_CLOSE),

	;

	private int code;
	private TimeInForce timeInForce;
	private com.cyanspring.common.type.TimeInForce commonTimeInForce;

	private WrapTimeInForce(int code, TimeInForce timeInForce,
			com.cyanspring.common.type.TimeInForce commonTimeInForce) {
		this.code = code;
		this.timeInForce = timeInForce;
		this.commonTimeInForce = commonTimeInForce;
	}

	public int getCode() {
		return code;
	}

	public TimeInForce getTimeInForce() {
		return timeInForce;
	}

	public com.cyanspring.common.type.TimeInForce getCommonTimeInForce() {
		return commonTimeInForce;
	}

}
