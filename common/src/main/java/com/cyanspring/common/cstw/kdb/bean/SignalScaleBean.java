package com.cyanspring.common.cstw.kdb.bean;

import com.cyanspring.common.cstw.kdb.SignalType;

public class SignalScaleBean {
	private Double from;
	private Double to;
	private SignalType signal;
	public Double getFrom() {
		return from;
	}
	public void setFrom(Double from) {
		this.from = from;
	}
	public Double getTo() {
		return to;
	}
	public void setTo(Double to) {
		this.to = to;
	}
	public SignalType getSignal() {
		return signal;
	}
	public void setSignal(SignalType signal) {
		this.signal = signal;
	}
	
}
