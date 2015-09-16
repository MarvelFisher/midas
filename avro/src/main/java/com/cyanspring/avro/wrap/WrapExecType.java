package com.cyanspring.avro.wrap;

import com.cyanspring.avro.types.ExecType;

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

}
