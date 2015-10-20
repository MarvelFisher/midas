package com.cyanspring.common.alert;

public class PastPercentagePriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
	private String dateTime ;
	private String content ;
	private double percentage ;	
	
	public PastPercentagePriceAlert(String userId, String symbol, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content, String commodity){
		super(userId, symbol, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content, commodity);
	}
	
	public PastPercentagePriceAlert()
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
