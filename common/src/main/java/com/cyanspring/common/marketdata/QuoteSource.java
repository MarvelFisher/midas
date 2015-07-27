package com.cyanspring.common.marketdata;

public enum QuoteSource {
    CLEAN_SESSION(-1),
    RESEND(0),
    IB(1),
    ID(2),
    WIND_GENERAL(101),
    WIND_INDEX(102)
    ;
    private int value;
    QuoteSource(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
