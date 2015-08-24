package com.cyanspring.apievent.obj;


/**
 * Basic account information
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class Account {
    private double dailyPnL;
    private double PnL;
    private double urPnL;
    private double allTimePnL;
    private String currency;
    private double cash;
    private double cashAvailable;
    private double value;
	private double cashDeduct;

    public double getDailyPnL() {
        return dailyPnL;
    }

    public void setDailyPnL(double dailyPnL) {
        this.dailyPnL = dailyPnL;
    }

    public double getPnL() {
        return PnL;
    }

    public void setPnL(double pnL) {
        PnL = pnL;
    }

    public double getUrPnL() {
        return urPnL;
    }

    public void setUrPnL(double urPnL) {
        this.urPnL = urPnL;
    }

    public double getAllTimePnL() {
        return allTimePnL;
    }

    public void setAllTimePnL(double allTimePnL) {
        this.allTimePnL = allTimePnL;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getCashAvailable() {
        return cashAvailable;
    }

    public void setCashAvailable(double cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

	public double getCashDeduct() {
		return cashDeduct;
	}

	public void setCashDeduct(double cashDeduct) {
		this.cashDeduct = cashDeduct;
	}
    
}
