package com.cyanspring.adaptor.future.wind.data;


import java.util.concurrent.ConcurrentHashMap;

public class WindDataParser extends AbstractWindDataParser {

    /**
     * Parset InputMessage Array To Wind IndexData Object
     * @param inputMessageArray
     * @param indexDataBySymbolMap
     * @return
     */
    public IndexData convertToIndexData(String[] inputMessageArray, ConcurrentHashMap<String, IndexData> indexDataBySymbolMap) {
        IndexData indexData = null;
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
                        if (indexDataBySymbolMap.containsKey(value)) {
                            indexData = (IndexData)indexDataBySymbolMap.get(value);
                        } else {
                            // add future data
                            indexData = new IndexData();
                            indexData.setWindCode(value);
                            indexData.setCode(value.split("\\.")[0]);
                            indexDataBySymbolMap.put(value, indexData);
                        }
                    }
                    switch (key) {
                        case "ActionDay":
                            indexData.setActionDay(Integer.parseInt(value));
                            break;
                        case "HighIndex":
                            indexData.setHighIndex(Long.parseLong(value));
                            break;
                        case "LastIndex":
                            indexData.setLastIndex(Long.parseLong(value));
                            break;
                        case "LowIndex":
                            indexData.setLowIndex(Long.parseLong(value));
                            break;
                        case "OpenIndex":
                            indexData.setOpenIndex(Long.parseLong(value));
                            break;
                        case "PrevIndex":
                            indexData.setPrevIndex(Long.parseLong(value));
                            break;
                        case "TotalVolume":
                            indexData.setTotalVolume(Long.parseLong(value));
                            break;
                        case "Time":
                            indexData.setTime(Integer.parseInt(value));
                            break;
                        case "TradingDay":
                            indexData.setTradingDay(Integer.parseInt(value));
                            break;
                        case "Turnover":
                            indexData.setTurnover(Long.parseLong(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return indexData;
    }

    /**
     * Parset InputMessage Array To Wind FutureData Object
     * @param inputMessageArray
     * @param futureDataBySymbolMap
     * @return
     */
    public FutureData convertToFutureData(String[] inputMessageArray, ConcurrentHashMap<String, FutureData> futureDataBySymbolMap) {
        FutureData futureData = null;
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
                            futureData = (FutureData)futureDataBySymbolMap.get(value);
                        } else {
                            // add future data
                            futureData = new FutureData();
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
    public StockData convertToStockData(String[] inputMessageArray, ConcurrentHashMap<String, StockData> stockDataBySymbolMap) {
        StockData stockData = null;
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
                            stockData = (StockData)stockDataBySymbolMap.get(value);
                        } else {
                            stockData = new StockData();
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

}
