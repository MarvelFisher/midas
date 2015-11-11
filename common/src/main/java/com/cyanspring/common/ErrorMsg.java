package com.cyanspring.common;

public class ErrorMsg {
	private int code;
	private String language;
	private String message;
	
	public ErrorMsg(int code, String language, String message) {
		super();
		this.code = code;
		this.language = language;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}
	public String getLanguage() {
		return language;
	}
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "" + code + ":" + language + ":" + message;
	}
}
