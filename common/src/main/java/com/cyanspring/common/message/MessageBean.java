package com.cyanspring.common.message;

public class MessageBean {
	private int code;
	private String msg;
	private String localMsg;
	public MessageBean(int code,String msg,String localMsg) {
		this.code =code;
		this.msg = msg;
		this.localMsg = localMsg;
	}
	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
	public String getLocalMsg() {
		return localMsg;
	}


}
