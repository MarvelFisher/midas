package com.cyanspring.common.cstw.kdb.bean;

import java.util.List;

public class SignalBean {
	private String symbol;
	private List<SignalScaleBean> scaleList;
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public List<SignalScaleBean> getScaleList() {
		return scaleList;
	}
	public void setScaleList(List<SignalScaleBean> scaleList) {
		this.scaleList = scaleList;
	}
}
