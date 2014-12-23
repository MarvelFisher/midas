package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PriceHighLowEvent extends RemoteAsyncEvent {
	
	private PriceHighLowType type;
	private double high;
	private double low;
	public PriceHighLowEvent(String key, String receiver,
			PriceHighLowType type, double high, double low) {
		super(key, receiver);
		this.type = type;
		this.high = high;
		this.low = low;
	}
	public PriceHighLowType getType() {
		return type;
	}
	public double getHigh() {
		return high;
	}
	public double getLow() {
		return low;
	}
	
}
