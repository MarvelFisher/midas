package com.cyanspring.marketdata.data;

public class CodeTableResult implements Cloneable{
    private int actionDay;
    private long hashCode;
    private String securityExchange;

    public int getActionDay() {
        return actionDay;
    }

    public void setActionDay(int actionDay) {
        this.actionDay = actionDay;
    }

    public long getHashCode() {
        return hashCode;
    }

    public void setHashCode(long hashCode) {
        this.hashCode = hashCode;
    }

    public String getSecurityExchange() {
        return securityExchange;
    }

    public void setSecurityExchange(String securityExchange) {
        this.securityExchange = securityExchange;
    }

    public CodeTableResult clone(){
        try {
            CodeTableResult codeTableResult = (CodeTableResult)super.clone();
            return codeTableResult;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
