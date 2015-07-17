package com.cyanspring.apievent.obj;


/**
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class Account {
    private double dailyPnL;
    private double PnL;
    private double urPnL;
    private double allTimePnL;
    private String currency;
    private double cash;
    private double margin;
    private double value;

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

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
