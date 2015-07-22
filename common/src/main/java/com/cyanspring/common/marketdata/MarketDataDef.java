package com.cyanspring.common.marketdata;

public class MarketDataDef {
    //Quote Log level
    public static final int QUOTE_GENERAL = 1;
    public static final int QUOTE_PRICE_ERROR = 2;
    public static final int QUOTE_TIME_ERROR = 3;

    //Quote Source
    public static final int QUOTE_CLEAN_SESSION = -1;
    public static final int QUOTE_SOURCE_RESEND = 0;
    public static final int QUOTE_SOURCE_IB = 1;
    public static final int QUOTE_SOUECE_ID = 2;
    public static final int QUOTE_SOURCE_WIND_GENERAL = 101;
    public static final int QUOTE_SOURCE_WIND_INDEX = 102;
}
