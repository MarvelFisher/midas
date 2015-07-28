package com.cyanspring.common.business;

import java.io.Serializable;
import java.util.Date;

/**
 * Hibernate - Database table
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class CashAudit implements Serializable {
	private String cashID;
    private String accountID;
    private AuditType type;
    private Date time;
    private double cashDeposited;
    private double addCash;
    
    public CashAudit(){
    	
    }

    public CashAudit(String cashID, String accountID, AuditType type, Date time, double cashDeposited, double addCash) {
        this.cashID = cashID;
    	this.accountID = accountID;
        this.type = type;
        this.time = time;
        this.cashDeposited = cashDeposited;
        this.addCash = addCash;
    }
    
    public String getCashID() {
		return cashID;
	}

	public void setCashID(String cashID) {
		this.cashID = cashID;
	}

	public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public AuditType getType() {
        return type;
    }

    public void setType(AuditType type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getCashDeposited() {
        return cashDeposited;
    }

    public void setCashDeposited(double cashDeposited) {
        this.cashDeposited = cashDeposited;
    }

    public double getAddCash() {
        return addCash;
    }

    public void setAddCash(double addCash) {
        this.addCash = addCash;
    }
}
