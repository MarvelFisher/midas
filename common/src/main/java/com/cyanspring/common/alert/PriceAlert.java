package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class PriceAlert implements Comparable<PriceAlert>{
	private String id;
	private String userId;
	private String symbol;
	private double price;
	private String dateTime ;
	private String content ;
	
	public PriceAlert(String userId, String symbol, double price, String strdateTime) {
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.userId = userId; //david
		this.symbol = symbol;  //USDJPY
		this.price = price;
		this.dateTime = strdateTime; // yyyy-mm-dd hh:mm:ss
		this.content = "" ;
	}
	
	@Override
	public int compareTo(PriceAlert other) {
		int iReturn = this.userId.compareTo(other.userId);
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

	public String getSymbol() {
		return symbol;
	}

	public double getPrice() {
		return price;
	}
	
	public String getTime()
	{
		return dateTime ;
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
