package com.cyanspring.common.alert;

public class CurPercentagePriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
    private String Commodity;
    private String dateTime ;
	private String content ;
	private double percentage ;	
	
	public CurPercentagePriceAlert(String userId, String symbol, String group, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content, String commodity){
		super(userId, symbol, group, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content, commodity);
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
