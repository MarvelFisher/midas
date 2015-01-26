package com.cyanspring.common.alert;

public class CurPriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
	private double price;
	private String dateTime ;
	private String content ;
	
	public CurPriceAlert(String userId, String symbol, double price,
			String strdateTime,String content) {
		super(userId, symbol, price, strdateTime, content);
	}
	
	public CurPriceAlert()
	{
		super() ;
	}
}
