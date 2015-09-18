package com.cyanspring.avro.wrap;

import java.util.HashMap;

import org.apache.avro.Schema;

import com.cyanspring.avro.generate.base.types.ObjectType;

public enum WrapObjectType {

	StateUpdate(0, ObjectType.StateUpdate,
			com.cyanspring.avro.generate.base.StateUpdate.SCHEMA$),

	OrderUpdate(1, ObjectType.OrderUpdate,
			com.cyanspring.avro.generate.trade.bean.OrderUpdate.SCHEMA$),

	NewOrderRequest(2, ObjectType.NewOrderRequest,
			com.cyanspring.avro.generate.trade.bean.NewOrderRequest.SCHEMA$),

	NewOrderReply(3, ObjectType.NewOrderReply,
			com.cyanspring.avro.generate.trade.bean.NewOrderReply.SCHEMA$),

	AmendOrderRequest(4, ObjectType.AmendOrderRequest,
			com.cyanspring.avro.generate.trade.bean.AmendOrderRequest.SCHEMA$),

	AmendOrderReply(5, ObjectType.AmendOrderReply,
			com.cyanspring.avro.generate.trade.bean.AmendOrderReply.SCHEMA$),

	CancelOrderRequest(6, ObjectType.CancelOrderRequest,
			com.cyanspring.avro.generate.trade.bean.CancelOrderRequest.SCHEMA$),

	CancelOrderReply(7, ObjectType.CancelOrderReply,
			com.cyanspring.avro.generate.trade.bean.CancelOrderReply.SCHEMA$),

	Quote(8, ObjectType.Quote,
			com.cyanspring.avro.generate.market.bean.OrderUpdate.SCHEMA$),

	SubscribeQuote(9, ObjectType.SubscribeQuote,
			com.cyanspring.avro.generate.market.bean.SubscribeQuote.SCHEMA$),

	UnsubscribeQuote(10, ObjectType.UnsubscribeQuote,
			com.cyanspring.avro.generate.market.bean.UnsubscribeQuote.SCHEMA$), 
			
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
