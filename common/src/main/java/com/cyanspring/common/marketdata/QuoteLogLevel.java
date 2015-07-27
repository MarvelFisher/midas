package com.cyanspring.common.marketdata;

public enum QuoteLogLevel {
    GENERAL(1),
    PRICE_ERROR(2),
    TIME_ERROR(3)
    ;
    private int value;
    QuoteLogLevel(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
