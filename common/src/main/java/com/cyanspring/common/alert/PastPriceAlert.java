package com.cyanspring.common.alert;

public class PastPriceAlert extends BasePriceAlert{
	private String id;
	private String userId;
	private String symbol;
	private double price;
	private String dateTime ;
	private String content ;
	
	public PastPriceAlert(String userId, String symbol, double price,
			String strdateTime,String content) {
		super(userId, symbol, price, strdateTime, content);
	}
}
