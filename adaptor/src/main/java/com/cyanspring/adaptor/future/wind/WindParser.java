package com.cyanspring.adaptor.future.wind;


import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_QUOTATIONDATE_CHANGE;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;

import java.util.concurrent.ConcurrentHashMap;

public class WindParser {

    private static final int INDEXSESSION_PREOPEN = 0;
    private static final int INDEXSESSION_OPEN = 1;
    private static final int INDEXSESSION_CLOSE = 2;

    public static int getItemSessionStatus(MarketSessionData marketSessionData){
        int sessionStatus = -1;
        if(MarketSessionType.PREOPEN == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_PREOPEN;
        if(MarketSessionType.OPEN == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_OPEN;
        if(MarketSessionType.CLOSE == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_CLOSE;
        return sessionStatus;
    }

    /**
     * Parser InputMessage Array To Wind Quotation Date Change Object
     * @param inputMessageArray
     * @return
     */
    public static TDF_QUOTATIONDATE_CHANGE convertToQuotaDate(String[] inputMessageArray) {
        TDF_QUOTATIONDATE_CHANGE quotationdateChange = new TDF_QUOTATIONDATE_CHANGE();
        String key = null;
        String value = null;
        for (String anInputMessageArray : inputMessageArray) {
            key = anInputMessageArray.split("=")[0];
            value = anInputMessageArray.split("=")[1];
            switch (key) {
                case "Market":
                    break;
                case "OldDate":
                    quotationdateChange.setOldDate(Integer.parseInt(value));
                    break;
                case "NewDate":
                    quotationdateChange.setNewDate(Integer.parseInt(value));
                    break;
                default:
                    break;
            }
        }
        return quotationdateChange;
    }


    /**
     * Parset InputMessage Array To Wind FutureData Object
     * @param inputMessageArray
     * @param futureDataBySymbolMap
     * @return
     */
    public static TDF_FUTURE_DATA convertToFutureData(String[] inputMessageArray, ConcurrentHashMap<String, TDF_FUTURE_DATA> futureDataBySymbolMap) {
        TDF_FUTURE_DATA futureData = null;
        String key = null;
        String value = null;
        String[] kv_arr = null;

        for (String anInputMessageArray : inputMessageArray) {
            if (anInputMessageArray != null && !"".equals(anInputMessageArray)) {
                kv_arr = anInputMessageArray.split("=");
                if (kv_arr.length > 1) {
                    key = kv_arr[0];
                    value = kv_arr[1];
                    if (key.equals("Symbol")) {
                        if (futureDataBySymbolMap.containsKey(value)) {
                            futureData = futureDataBySymbolMap.get(value);
                        } else {
                            // add future data
                            futureData = new TDF_FUTURE_DATA();
                            futureData.setWindCode(value);
                            futureData.setCode(value.split("\\.")[0]);
                            futureDataBySymbolMap.put(value, futureData);
                        }
                    }
                    switch (key) {
                        case "ActionDay":
                            futureData.setActionDay(Integer.parseInt(value));
                            break;
                        case "AskPrice":
                            futureData.setAskPrice(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "AskVol":
                            futureData.setAskVol(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "BidPrice":
                            futureData.setBidPrice(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "BidVol":
                            futureData.setBidVol(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "Close":
                            futureData.setClose(Long.parseLong(value));
                            break;
                        case "High":
                            futureData.setHigh(Long.parseLong(value));
                            break;
                        case "Ceil":
                            futureData.setHighLimited(Long.parseLong(value));
                            break;
                        case "Low":
                            futureData.setLow(Long.parseLong(value));
                            break;
                        case "Floor":
                            futureData.setLowLimited(Long.parseLong(value));
                            break;
                        case "Last":
                            futureData.setMatch(Long.parseLong(value));
                            break;
                        case "Open":
                            futureData.setOpen(Long.parseLong(value));
                            break;
                        case "OI":
                            futureData.setOpenInterest(Long.parseLong(value));
                            break;
                        case "PreSettlePrice":
                            futureData.setPreClose(Long.parseLong(value));
                            break;
                        case "SettlePrice":
                            futureData.setSettlePrice(Long.parseLong(value));
                            break;
                        case "Status":
                            futureData.setStatus(Integer.parseInt(value));
                            break;
                        case "Time":
                            futureData.setTime(Integer.parseInt(value));
                            break;
                        case "TradingDay":
                            futureData.setTradingDay(Integer.parseInt(value));
                            break;
                        case "Turnover":
                            futureData.setTurnover(Long.parseLong(value));
                            break;
                        case "Volume":
                            futureData.setVolume(Long.parseLong(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return futureData;
    }

    /**
     * Parser inputMessage to wind StockData Object
     * @param inputMessageArray
     * @param stockDataBySymbolMap
     * @return
     */
    public static TDF_MARKET_DATA convertToStockData(String[] inputMessageArray, ConcurrentHashMap<String, TDF_MARKET_DATA> stockDataBySymbolMap) {
        TDF_MARKET_DATA stockData = null;
        String key = null;
        String value = null;
        String[] kv_arr = null;

        for (String anInputMessageArray : inputMessageArray) {
            if (anInputMessageArray != null && !"".equals(anInputMessageArray)) {
                kv_arr = anInputMessageArray.split("=");
                if (kv_arr.length > 1) {
                    key = kv_arr[0];
                    value = kv_arr[1];
                    if (key.equals("Symbol")) {
                        if (stockDataBySymbolMap.containsKey(value)) {
                            stockData = stockDataBySymbolMap.get(value);
                        } else {
                            stockData = new TDF_MARKET_DATA();
                            stockData.setWindCode(value);
                            stockData.setCode(value.split("\\.")[0]);
                            stockDataBySymbolMap.put(value, stockData);
                        }
                    }
                    switch (key) {
                        case "ActionDay":
                            stockData.setActionDay(Integer.parseInt(value));
                            break;
                        case "AskPrice":
                            stockData.setAskPrice(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "AskVol":
                            stockData.setAskVol(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "BidPrice":
                            stockData.setBidPrice(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "BidVol":
                            stockData.setBidVol(parseStringTolong(value.substring(1,
                                    value.length() - 1).split("\\s")));
                            break;
                        case "High":
                            stockData.setHigh(Long.parseLong(value));
                            break;
                        case "Ceil":
                            stockData.setHighLimited(Long.parseLong(value));
                            break;
                        case "Low":
                            stockData.setLow(Long.parseLong(value));
                            break;
                        case "Floor":
                            stockData.setLowLimited(Long.parseLong(value));
                            break;
                        case "Last":
                            stockData.setMatch(Long.parseLong(value));
                            break;
                        case "Open":
                            stockData.setOpen(Long.parseLong(value));
                            break;
                        case "IOPV":
                            stockData.setIOPV(Integer.parseInt(value));
                            break;
                        case "PreClose":
                            stockData.setPreClose(Long.parseLong(value));
                            break;
                        case "Status":
                            stockData.setStatus(Integer.parseInt(value));
                            break;
                        case "Time":
                            stockData.setTime(Integer.parseInt(value));
                            break;
                        case "TradingDay":
                            stockData.setTradingDay(Integer.parseInt(value));
                            break;
                        case "Turnover":
                            stockData.setTurnover(Long.parseLong(value));
                            break;
                        case "Volume":
                            stockData.setVolume(Long.parseLong(value));
                            break;
                        case "NumTrades":
                            stockData.setNumTrades(Long.parseLong(value));
                            break;
                        case "TotalBidVol":
                            stockData.setTotalBidVol(Long.parseLong(value));
                            break;
                        case "WgtAvgAskPrice":
                            stockData.setWeightedAvgAskPrice(Long.parseLong(value));
                            break;
                        case "WgtAvgBidPrice":
                            stockData.setWeightedAvgBidPrice(Long.parseLong(value));
                            break;
                        case "YieldToMaturity":
                            stockData.setYieldToMaturity(Integer.parseInt(value));
                            break;
                        case "Prefix":
                            stockData.setPrefix(value);
                            break;
                        case "Syl1":
                            stockData.setSyl1(Integer.parseInt(value));
                            break;
                        case "Syl2":
                            stockData.setSyl2(Integer.parseInt(value));
                            break;
                        case "SD2":
                            stockData.setSD2(Integer.parseInt(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return stockData;
    }

    /**
     * Convert String Array To long Array
     *
     * @param str_arr
     * @return long array
     */
    public static long[] parseStringTolong(String[] str_arr) {
        long[] long_arr = new long[str_arr.length];
        for (int i = 0; i < str_arr.length; i++) {
            long_arr[i] = Long.parseLong(str_arr[i]);
        }
        return long_arr;
    }

}
