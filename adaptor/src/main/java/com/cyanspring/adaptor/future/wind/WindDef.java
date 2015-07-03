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

    //Wind Stock Status field
    public static final int STOCK_STATUS_NORMAL = 0;
    public static final int STOCK_STATUS_STOP_TRA_IN_OPEN = (int) 'R';
    public static final int STOCK_STATUS_SLEEP = (int) 'P';
    public static final int STOCK_STATUS_STOP_SYMBOL = (int) 'B';
    public static final int STOCK_STATUS_MARKET_CLOSE = (int) 'C';
    public static final int STOCK_STATUS_STOP_SYMBOL_2 = (int) 'D';
    public static final int STOCK_STATUS_NEW_SYMBOL = (int) 'Y';
    public static final int STOCK_STATUS_PENDING = (int) 'W';
    public static final int STOCK_STATUS_PENDING_2 = (int) 'X';
    public static final int STOCK_STATUS_NOT_SERVICE = (int) 'S';
    public static final int STOCK_STATUS_VAR_STOP = (int) 'Q';
    public static final int STOCK_STATUS_STOP_TRAN = (int) 'V';
    public static final int STOCK_STATUS_WAIT_DELETE = (int) 'Z';
    public static final long STOCK_WARNING_MILLISECONDS = 4000;
    public static final String STOCK_EX_RIGHT = "XR";
    public static final String STOCK_EX_DIVIDENT = "XD";
    public static final String STOCK_EX_RIGHT_DIVIDENT = "DR";

    //Wind Transation BSFlag field
    public static final int TRANS_BSFLAG_BUY = (int) 'B';
    public static final int TRANS_BSFLAG_SELL = (int) 'S';
    public static final int TRANS_BSFLAG_NONE = (int) ' ';

    public static final int AM10 = 100000000;
    public static final long timerInterval = 5000;
    public static final long SmallSessionTimeInterval = 30 * 60 * 1000;
    public static final int ReceiveQuoteTimeInterval = 30 * 60 * 1000;
    public static final String TITLE_FUTURE = "FUTURE";
    public static final String TITLE_STOCK = "STOCK";
    public static final String TITLE_INDEX = "INDEX";
    public static final String TITLE_TRANSATION = "TRANS";
    public static final String WARN_LAST_LESS_THAN_ZERO = "QUOTE WARNING : Last less than Zero";
    public static final String WARN_TRADEDATE_NOT_MATCH = "QUOTE WARNING : TradeDate NOT match";
    public static final String WARN_TIME_FORMAT_ERROR = "QUOTE WARNING : Time format fault";
    public static final String WARN_TURNOVER_LESS_THAN_ZERO = "QUOTE WARNING : Turnover less than Zero";
    public static final String WARN_CLOSE_OVER_TIME = "QUOTE WARNING : Close Over "
            + ReceiveQuoteTimeInterval / 60 / 1000 + " Time";
    public static final String ERROR_NO_REFDATA = "QUOTE ERROR : No RefData";
}
