package com.cyanspring.avro.wrap;

import java.util.HashMap;

import com.cyanspring.avro.types.OrdStatus;

public enum WrapOrdStatus {

	New(0, OrdStatus.New, com.cyanspring.common.type.OrdStatus.NEW),

	PartiallyFilled(1, OrdStatus.PartiallyFilled,
			com.cyanspring.common.type.OrdStatus.PARTIALLY_FILLED),

	Filled(2, OrdStatus.Filled, com.cyanspring.common.type.OrdStatus.FILLED),

	DoneForDay(3, OrdStatus.DoneForDay,
			com.cyanspring.common.type.OrdStatus.DONE_FOR_DAY),

	Canceled(4, OrdStatus.Canceled,
			com.cyanspring.common.type.OrdStatus.CANCELED),

	Replaced(5, OrdStatus.Replaced,
			com.cyanspring.common.type.OrdStatus.REPLACED),

	PendingCancel(6, OrdStatus.PendingCancel,
			com.cyanspring.common.type.OrdStatus.PENDING_CANCEL),

	Stopped(7, OrdStatus.Stopped, com.cyanspring.common.type.OrdStatus.STOPPED),

	Rejected(8, OrdStatus.Rejected,
			com.cyanspring.common.type.OrdStatus.REJECTED),

	Suspended(9, OrdStatus.Suspended,
			com.cyanspring.common.type.OrdStatus.SUSPENDED),

	PendingNew(10, OrdStatus.PendingNew,
			com.cyanspring.common.type.OrdStatus.PENDING_NEW),

	Calculated(11, OrdStatus.Calculated,
			com.cyanspring.common.type.OrdStatus.CALCULATED),

	Expired(12, OrdStatus.Expired, com.cyanspring.common.type.OrdStatus.EXPIRED),

	AcceptedForBidding(13, OrdStatus.AcceptedForBidding,
			com.cyanspring.common.type.OrdStatus.ACCEPTED_FOR_BIDDING),

	PendingReplace(14, OrdStatus.PendingReplace,
			com.cyanspring.common.type.OrdStatus.PENDING_REPLACE),

	;

	private int code;
	private OrdStatus ordStatus;
	private com.cyanspring.common.type.OrdStatus commonOrdStatus;

	private WrapOrdStatus(int code, OrdStatus ordStatus,
			com.cyanspring.common.type.OrdStatus commonOrdStatus) {
		this.code = code;
		this.ordStatus = ordStatus;
		this.commonOrdStatus = commonOrdStatus;
	}

	public int getCode() {
		return code;
	}

	public OrdStatus getOrdStatus() {
		return ordStatus;
	}

	public com.cyanspring.common.type.OrdStatus getCommonOrdStatus() {
		return commonOrdStatus;
	}
	
	private static HashMap<Integer, WrapOrdStatus> map4Code;

	private static HashMap<com.cyanspring.common.type.OrdStatus, WrapOrdStatus> map4CommonOrdStatus;

	static {
		WrapOrdStatus[] values = values();
		map4Code = new HashMap<Integer, WrapOrdStatus>(values.length);
		map4CommonOrdStatus = new HashMap<com.cyanspring.common.type.OrdStatus, WrapOrdStatus>(
				values.length);
		for (WrapOrdStatus wrapOrdStatus : values) {
			map4Code.put(wrapOrdStatus.code, wrapOrdStatus);
			map4CommonOrdStatus.put(wrapOrdStatus.commonOrdStatus,
					wrapOrdStatus);
		}
	}

	public static WrapOrdStatus valueOf(int code) {
		if (map4Code.containsKey(code)) {
			return map4Code.get(code);
		}
		return null;
	}

	public static WrapOrdStatus valueOf(com.cyanspring.common.type.OrdStatus commonOrdStatus) {
		if (map4CommonOrdStatus.containsKey(commonOrdStatus)) {
			return map4CommonOrdStatus.get(commonOrdStatus);
		}
		return null;
	}

}
