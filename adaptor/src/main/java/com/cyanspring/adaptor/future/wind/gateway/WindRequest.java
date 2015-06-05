package com.cyanspring.adaptor.future.wind.gateway;

public class WindRequest {
	public final static int Subscribe = 1;
	public final static int Unsubscribe = 2;
	public final static int RequestCodeTable = 3;
	
	
	int reqId;
	String strInfo;
	
	public WindRequest(int req,String str)
	{
		reqId = req;		
		strInfo = str;
	}
}
