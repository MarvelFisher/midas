package com.cyanspring.avro.wrap;

import java.util.HashMap;

import org.apache.avro.Schema;

import com.cyanspring.avro.types.ObjectType;

public enum WrapObjectType {

	OrderUpdate(0, ObjectType.OrderUpdate,
			com.cyanspring.avro.trading.OrderUpdate.SCHEMA$),

	NewOrderRequest(1, ObjectType.NewOrderRequest,
			com.cyanspring.avro.trading.NewOrderRequest.SCHEMA$),

	NewOrderReply(2, ObjectType.NewOrderReply,
			com.cyanspring.avro.trading.NewOrderReply.SCHEMA$),

	AmendOrderRequest(3, ObjectType.AmendOrderRequest,
			com.cyanspring.avro.trading.AmendOrderRequest.SCHEMA$),

	AmendOrderReply(4, ObjectType.AmendOrderReply,
			com.cyanspring.avro.trading.AmendOrderReply.SCHEMA$),

	CancelOrderRequest(5, ObjectType.CancelOrderRequest,
			com.cyanspring.avro.trading.CancelOrderRequest.SCHEMA$),

	CancelOrderReply(6, ObjectType.CancelOrderReply,
			com.cyanspring.avro.trading.CancelOrderReply.SCHEMA$),

	;

	private int code;
	private ObjectType objectType;
	private Schema schema;

	WrapObjectType(int code, ObjectType objectType, Schema schema) {
		this.code = code;
		this.objectType = objectType;
		this.schema = schema;
	}

	public int getCode() {
		return code;
	}

	public ObjectType getObjectType() {
		return objectType;
	}

	public Schema getSchema() {
		return schema;
	}

	private static HashMap<Integer, WrapObjectType> map4Code;
	static {
		WrapObjectType[] values = values();
		map4Code = new HashMap<Integer, WrapObjectType>(values.length);
		for (WrapObjectType wrapObjectType : values) {
			map4Code.put(wrapObjectType.code, wrapObjectType);
		}
	}

	public static WrapObjectType valueOf(int code) {
		if (map4Code.containsKey(code)) {
			return map4Code.get(code);
		}
		return null;
	}

}
