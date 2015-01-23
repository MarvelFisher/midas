package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class PriceAlert implements Comparable<PriceAlert>{
	private String id;
	private String userId;
	private String symbol;
	private double price;
	private String dateTime ;
	private String content ;
	
	public PriceAlert(String userId, String symbol, double price, String strdateTime){
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.setUserId(userId); //david
		this.setSymbol(symbol);  //USDJPY
		this.setPrice(price);
		this.setDateTime(strdateTime); // yyyy-mm-dd hh:mm:ss
		this.content = "" ;
	}
	
	public void modifyPriceAlert(PriceAlert pricealert)
	{
		if (!this.id.equals(pricealert.getId()))
		{
			return ;
		}
		if (!this.userId.equals(pricealert.getUserId()))
		{
			return ;
		}
		this.setSymbol(pricealert.getSymbol());  //USDJPY
		this.setPrice(pricealert.getPrice());
		this.setDateTime(pricealert.getDateTime()); // yyyy-mm-dd hh:mm:ss
		this.content = pricealert.getContent();
	}
	@Override
	public int compareTo(PriceAlert other) {
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
