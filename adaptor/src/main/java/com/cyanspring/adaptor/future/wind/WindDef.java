package com.cyanspring.adaptor.future.wind;

/**
 * Wind use info
 */
public final class WindDef {
    public static final int MSG_INVALID = -100;
    public static final int MSG_SYS_DISCONNECT_NETWORK = -99;
    public static final int MSG_SYS_CONNECT_RESULT = -98;
    public static final int MSG_SYS_LOGIN_RESULT = -97;
    public static final int MSG_SYS_CODETABLE_RESULT = -96;
    public static final int MSG_SYS_QUOTATIONDATE_CHANGE = -95;
    public static final int MSG_SYS_MARKET_CLOSE = -94;
    public static final int MSG_SYS_HEART_BEAT = -93;
    public static final int MSG_DATA_INDEX = -92;
    public static final int MSG_DATA_MARKET = -91;
    public static final int MSG_DATA_FUTURE = -90;
    public static final int MSG_DATA_TRANSACTION = -89;
    public static final int MSG_DATA_ORDERQUEUE = -88;
    public static final int MSG_DATA_ORDER = -87;

    public static final int AM10 = 100000000;
    public static final long timerInterval = 5000;
    public static final long SmallSessionTimeInterval = 30 * 60 * 1000;
    public static final int ReceiveQuoteTimeInterval = 30 * 60 * 1000;
    public static final String TITLE_FUTURE = "FUTURE";
    public static final String TITLE_STOCK = "STOCK";
    public static final String TITLE_INDEX = "INDEX";
    public static final String WARN_LAST_LESS_THAN_ZERO = "QUOTE WARNING : Last less than Zero";
    public static final String WARN_TRADEDATE_NOT_MATCH = "QUOTE WARNING : TradeDate NOT match";
    public static final String WARN_TIME_FORMAT_ERROR = "QUOTE WARNING : Time format fault";
    public static final String WARN_TURNOVER_LESS_THAN_ZERO = "QUOTE WARNING : Turnover less than Zero";
    public static final String WARN_CLOSE_OVER_TIME = "QUOTE WARNING : Close Over "
            + ReceiveQuoteTimeInterval / 60 / 1000 + " Time";
}
