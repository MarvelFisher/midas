package com.cyanspring.apievent.obj;

import java.util.Date;

/**
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class Position {
    private String id;
    private String account;
    private String user;
    private String symbol;
    private double qty;
    private double PnL;
    private Date created;
    private double acPnL;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getPnL() {
        return PnL;
    }

    public void setPnL(double pnL) {
        PnL = pnL;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public double getAcPnL() {
        return acPnL;
    }

    public void setAcPnL(double acPnL) {
        this.acPnL = acPnL;
    }

    protected String formatString() {
		return this.id + ", " + this.account + ", " + this.symbol  + ", " + 
				this.qty + ", " + this.PnL;
	}
	
	@Override
	public synchronized String toString() {
		return "[" + formatString() + "]";
	}
}
