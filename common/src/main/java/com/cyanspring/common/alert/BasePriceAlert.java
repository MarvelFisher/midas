package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class BasePriceAlert implements Comparable<BasePriceAlert>{
	private String id;
	private String userId;
	private String symbol;
	private double price;
	private String dateTime ;
	private String content ;
	
	public BasePriceAlert(String userId, String symbol, double price, String strdateTime, String content){
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.setUserId(userId); //david
		this.setSymbol(symbol);  //USDJPY
		this.setPrice(price);
		this.setDateTime(strdateTime); // yyyy-mm-dd hh:mm:ss
		this.setContent(content);
	}
	
	public BasePriceAlert()
	{
		super();
	}
	
	protected String fieldtoString()
	{
		return new String("userId='" + userId + "',symbol='" + symbol + "',price=" + String.valueOf(price) +
				",datetime='" + dateTime + "'");
	}
	
	@Override
	public String toString()
	{
		return "[" + fieldtoString() + "]" ;
	}
	public void modifyPriceAlert(BasePriceAlert basePriceAlert)
	{
		if (!this.id.equals(basePriceAlert.getId()))
		{
			return ;
		}
		if (!this.userId.equals(basePriceAlert.getUserId()))
		{
			return ;
		}
		this.setSymbol(basePriceAlert.getSymbol());  //USDJPY
		this.setPrice(basePriceAlert.getPrice());
		this.setDateTime(basePriceAlert.getDateTime()); // yyyy-mm-dd hh:mm:ss
		this.setContent(basePriceAlert.getContent());
	}
	@Override
	public int compareTo(BasePriceAlert other) {
		int iReturn = this.getUserId().compareTo(other.getUserId());
		if (iReturn == 0)
		{
			iReturn = this.id.compareTo(other.id);
		}
		return iReturn;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String strId){
		this.id = strId ;
	}

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}	
	
	public String getContent()
	{
		return content ;
	}
	
	public void setContent(String content)
	{
		this.content = content ;
	}	
}
