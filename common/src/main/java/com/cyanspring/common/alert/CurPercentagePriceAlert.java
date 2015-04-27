package com.cyanspring.common.alert;

public class CurPercentagePriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
	private String dateTime ;
	private String content ;
	private double percentage ;	
	
	public CurPercentagePriceAlert(String userId, double percentage, String symbol, String strdateTime, String content) {
		super(userId, percentage, symbol, strdateTime, content);
	}
	
	public CurPercentagePriceAlert()
	{
		super() ;
	}
	
	@Override
	protected String fieldtoString()
	{
		return new String("userId='" + userId + "',symbol='" + symbol + "',percentage=" + String.valueOf(percentage) +
				",datetime='" + dateTime + "'");
	}
}
