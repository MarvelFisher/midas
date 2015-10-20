package com.cyanspring.common.alert;

public class PastPriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
    private String Commdity;
	private double risePrice;
	private double dropPrice;
	private String dateTime ;
	private String content ;
	private double risePercentage;
	private double fallPercentage;

	public PastPriceAlert(String userId, String symbol, String group, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content, String commodity){
		super(userId, symbol, group, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content, commodity);
	}
    public PastPriceAlert(String userId, String symbol, String group, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content){
        super(userId, symbol, group, risePrice, dropPrice, 
				risePercentage, fallPercentage, strdateTime, content);
    }
	public PastPriceAlert()
	{
		super();
	}
}
