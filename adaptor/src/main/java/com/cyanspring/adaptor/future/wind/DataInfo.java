package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.TDF_MSG;

public class DataInfo {
	private TDF_MSG msg;
	private String localCurrentTime;
	DataInfo(TDF_MSG msg, String localCurrentTime) {
		this.msg = msg;
		this.localCurrentTime = localCurrentTime;		
	}
	public TDF_MSG getMsg(){
		return msg;
	}
	public String getLocalCurrentTime(){
		return localCurrentTime;
	}
	 
}
 