package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class BasePriceAlert implements Comparable<BasePriceAlert>{
	private String id;
	private String userId;
	private String symbol;
    private String Commodity;
	private double risePrice;
	private double dropPrice;
	private String dateTime;
	private String content;
	private double risePercentage;
	private double dropPercentage;
	private boolean sendFlag;
	
	public BasePriceAlert(String userId, String symbol, double risePrice, double dropPrice, 
			double risePercentage, double dropPercentage, String strdateTime, String content, String commodity){
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.setUserId(userId); //david
		this.setSymbol(symbol);  //USDJPY
        this.setCommodity(commodity);
		this.setRisePrice(risePrice);
		this.setDropPrice(dropPrice);
		this.setRisePercentage(risePercentage);
		this.setDropPercentage(dropPercentage);
		this.setDateTime(strdateTime); // yyyy-mm-dd hh:mm:ss
		this.setContent(content);
		this.setSendFlag(false);
	}

    public BasePriceAlert(String userId, String symbol, double risePrice, double dropPrice, 
			double risePercentage, double fallPercentage, String strdateTime, String content){
        super();
        this.id = "A" + IdGenerator.getInstance().getNextID();
        this.setUserId(userId); //david
        this.setSymbol(symbol);  //USDJPY
		this.setRisePrice(risePrice);
		this.setDropPrice(dropPrice);
		this.setRisePercentage(risePercentage);
		this.setDropPercentage(fallPercentage);
        this.setDateTime(strdateTime); // yyyy-mm-dd hh:mm:ss
        this.setContent(content);
        this.setSendFlag(false);
    }
	
	public BasePriceAlert()
	{
		super();
	}
	
	protected String fieldtoString()
	{
		return new String("userId='" + userId + "',symbol='" + symbol + "',rise=" + String.valueOf(risePrice) +
				",fall=" + String.valueOf(dropPrice) + ",datetime='" + dateTime + "'");
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
		this.setRisePrice(basePriceAlert.getRisePrice());
		this.setDropPrice(basePriceAlert.getDropPrice());
		this.setRisePercentage(basePriceAlert.getRisePercentage());
		this.setDropPercentage(basePriceAlert.getDropPercentage());
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
	
	@Override
	public boolean equals(Object obj){		
		return this.getId().equals(((BasePriceAlert)obj).getId());
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

	public boolean isSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(boolean sendFlag) {
		this.sendFlag = sendFlag;
	}

    public String getCommodity() {
        return Commodity;
    }

    public void setCommodity(String commodity) {
        Commodity = commodity;
    }

	public double getRisePrice()
	{
		return risePrice;
	}

	public void setRisePrice(double risePrice)
	{
		this.risePrice = risePrice;
	}

	public double getDropPrice()
	{
		return dropPrice;
	}

	public void setDropPrice(double dropPrice)
	{
		this.dropPrice = dropPrice;
	}

	public double getRisePercentage()
	{
		return risePercentage;
	}

	public void setRisePercentage(double risePercentage)
	{
		this.risePercentage = risePercentage;
	}

	public double getDropPercentage()
	{
		return dropPercentage;
	}

	public void setDropPercentage(double dropPercentage)
	{
		this.dropPercentage = dropPercentage;
	}

}
