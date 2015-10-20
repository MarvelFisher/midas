package com.cyanspring.common.alert;

public class CurPriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
    private String Commodity;
	private double risePrice;
	private double dropPrice;
	private String dateTime ;
	private String content ;
	private double risePercentage;
	private double fallPercentage;
	
	public CurPriceAlert(String userId, String symbol, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content, String commodity){
		super(userId, symbol, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content, commodity);
	}

    public CurPriceAlert(String userId, String symbol, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content){
        super(userId, symbol, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content);
    }

	public CurPriceAlert()
	{
		super() ;
	}
}
