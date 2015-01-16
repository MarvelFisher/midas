package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.PriceHighLow;

public class PriceHighLowEvent extends RemoteAsyncEvent {
	private List<PriceHighLow> listHighLow ;
	private boolean isSuccess ;
	private String errorMsg ;

	public PriceHighLowEvent(String key, String receiver) {
		super(key, receiver);
	}
	public PriceHighLowEvent(String key, String receiver,
			List<PriceHighLow> listHighLow) {
		super(key, receiver);
		this.listHighLow = listHighLow ;
	}
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public List<PriceHighLow> getListHighLow() {
		return listHighLow;
	}
	public void setListHighLow(List<PriceHighLow> listHighLow) {
		this.listHighLow = listHighLow;
	}
}
