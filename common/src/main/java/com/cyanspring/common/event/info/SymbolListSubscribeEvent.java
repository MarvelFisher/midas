package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SymbolListSubscribeEvent extends RemoteAsyncEvent {
	private boolean isSuccess ;
	private String errorMsg ;

	public SymbolListSubscribeEvent(String key, String receiver) {
		super(key, receiver);
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

}
