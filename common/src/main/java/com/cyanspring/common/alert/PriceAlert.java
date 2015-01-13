package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class PriceAlert implements Comparable<PriceAlert>{
	private String id;
	private String userId;
	private String senderId;
	private String symbol;
	private double price;
	private String dateTime ;
	
	public PriceAlert(String SenderId,String userId, String symbol, double price, String strdateTime) {
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.senderId = SenderId ; // david
		this.userId = userId; //david
		//this.userAccountId = accountId; //david-FX
		this.symbol = symbol;  //USDJPY
		this.price = price;
		this.dateTime = strdateTime; // yyyy-mm-dd hh:mm:ss
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
	
	public String getSenderId(){
		return senderId ;
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
	
}
