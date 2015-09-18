package com.cyanspring.avro.wrap;

import java.util.HashMap;

import com.cyanspring.avro.generate.trade.types.ExecType;

public enum WrapExecType {

	New(0, ExecType.New, com.cyanspring.common.type.ExecType.NEW),

	PartiallyFilled(1, ExecType.PartiallyFilled,
			com.cyanspring.common.type.ExecType.PARTIALLY_FILLED),

	Filled(2, ExecType.Filled, com.cyanspring.common.type.ExecType.FILLED),

	DoneForDay(3, ExecType.DoneForDay,
			com.cyanspring.common.type.ExecType.DONE_FOR_DAY),

	Canceled(4, ExecType.Canceled, com.cyanspring.common.type.ExecType.CANCELED),

	Replace(5, ExecType.Replace, com.cyanspring.common.type.ExecType.REPLACE),

	PendingCancel(6, ExecType.PendingCancel,
			com.cyanspring.common.type.ExecType.PENDING_CANCEL),

	Stopped(7, ExecType.Stopped, com.cyanspring.common.type.ExecType.STOPPED),

	Rejected(8, ExecType.Rejected, com.cyanspring.common.type.ExecType.REJECTED),

	Suspended(9, ExecType.Suspended,
			com.cyanspring.common.type.ExecType.SUSPENDED),

	PendingNew(10, ExecType.PendingNew,
			com.cyanspring.common.type.ExecType.PENDING_NEW),

	Calculated(11, ExecType.Calculated,
			com.cyanspring.common.type.ExecType.CALCULATED),

	Expired(12, ExecType.Expired, com.cyanspring.common.type.ExecType.EXPIRED),

	Restated(13, ExecType.Restated,
			com.cyanspring.common.type.ExecType.RESTATED),

	PendingReplace(14, ExecType.PendingReplace,
			com.cyanspring.common.type.ExecType.PENDING_REPLACE),

	;

	private int code;
	private ExecType execType;
	private com.cyanspring.common.type.ExecType commonExecType;

	private WrapExecType(int code, ExecType execType,
			com.cyanspring.common.type.ExecType commonExecType) {
		this.code = code;
		this.execType = execType;
		this.commonExecType = commonExecType;
	}

	public int getCode() {
		return code;
	}

	public ExecType getExecType() {
		return execType;
	}

	public com.cyanspring.common.type.ExecType getCommonExecType() {
		return commonExecType;
	}

	private static HashMap<Integer, WrapExecType> map4Code;

	private static HashMap<com.cyanspring.common.type.ExecType, WrapExecType> map4CommonExecType;

	static {
		WrapExecType[] values = values();
		map4Code = new HashMap<Integer, WrapExecType>(values.length);
		map4CommonExecType = new HashMap<com.cyanspring.common.type.ExecType, WrapExecType>(
				values.length);
		for (WrapExecType wrapExecType : values) {
			map4Code.put(wrapExecType.code, wrapExecType);
			map4CommonExecType.put(wrapExecType.commonExecType, wrapExecType);
		}
	}

	public static WrapExecType valueOf(int code) {
		if (map4Code.containsKey(code)) {
			return map4Code.get(code);
		}
		return null;
	}

	public static WrapExecType valueOf(
			com.cyanspring.common.type.ExecType commonExecType) {
		if (map4CommonExecType.containsKey(commonExecType)) {
			return map4CommonExecType.get(commonExecType);
		}
		return null;
	}

}
