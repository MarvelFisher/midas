package com.cyanspring.adaptor.future.wind.data;


import com.cyanspring.Network.Transport.FDTFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WindDataParser extends AbstractWindDataParser {

    private static final Logger log = LoggerFactory
            .getLogger(WindDataParser.class);

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
                            indexData = indexDataBySymbolMap.get(value);
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
     * Parser inputHashMap to Wind IndexData Object
     * @param inputHashMap
     * @param indexDataBySymbolMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public IndexData convertToIndexData(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, IndexData> indexDataBySymbolMap) throws UnsupportedEncodingException {
        IndexData indexData = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String symbol = new String((byte[]) inputHashMap.get(FDTFields.WindSymbolCode), "UTF-8");
            if (indexDataBySymbolMap.containsKey(symbol)) {
                indexData = indexDataBySymbolMap.get(symbol);
            } else {
                indexData = new IndexData();
                indexData.setWindCode(symbol);
                indexData.setCode(symbol.split("\\.")[0]);
                indexDataBySymbolMap.put(symbol, indexData);
            }
            if (null != inputHashMap.get(FDTFields.ActionDay))
                indexData.setActionDay(((Number) inputHashMap.get(FDTFields.ActionDay)).intValue());
            if (null != inputHashMap.get(FDTFields.High))
                indexData.setHighIndex(((Number) inputHashMap.get(FDTFields.High)).longValue());
            if (null != inputHashMap.get(FDTFields.Last))
                indexData.setLastIndex(((Number) inputHashMap.get(FDTFields.Last)).longValue());
            if (null != inputHashMap.get(FDTFields.Low)) indexData.setLowIndex(((Number) inputHashMap.get(FDTFields.Low)).longValue());
            if (null != inputHashMap.get(FDTFields.Open))
                indexData.setOpenIndex(((Number) inputHashMap.get(FDTFields.Open)).longValue());
            if (null != inputHashMap.get(FDTFields.PreClose))
                indexData.setPrevIndex(((Number) inputHashMap.get(FDTFields.PreClose)).longValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                indexData.setTotalVolume(((Number) inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Time)) indexData.setTime(((Number) inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.TradingDay))
                indexData.setTradingDay(((Number) inputHashMap.get(FDTFields.TradingDay)).intValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                indexData.setTurnover(((Number) inputHashMap.get(FDTFields.Turnover)).longValue());
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
                            futureData = futureDataBySymbolMap.get(value);
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
     * Parser input HashMap to Wind FutureData Object
     * @param inputHashMap
     * @param futureDataBySymbolMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public FutureData convertToFutureData(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, FutureData> futureDataBySymbolMap) throws UnsupportedEncodingException {
        FutureData futureData = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String symbol = new String((byte[]) inputHashMap.get(FDTFields.WindSymbolCode), "UTF-8");
            if (futureDataBySymbolMap.containsKey(symbol)) {
                futureData = futureDataBySymbolMap.get(symbol);
            } else {
                futureData = new FutureData();
                futureData.setWindCode(symbol);
                futureData.setCode(symbol.split("\\.")[0]);
                futureDataBySymbolMap.put(symbol, futureData);
            }
            if (null != inputHashMap.get(FDTFields.ActionDay))
                futureData.setActionDay(((Number) inputHashMap.get(FDTFields.ActionDay)).intValue());
            if (null != inputHashMap.get(FDTFields.AskPriceArray))
                futureData.setAskPrice(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.AskPriceArray)));
            if (null != inputHashMap.get(FDTFields.AskVolumeArray))
                futureData.setAskVol(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.AskVolumeArray)));
            if (null != inputHashMap.get(FDTFields.BidPriceArray))
                futureData.setBidPrice(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.BidPriceArray)));
            if (null != inputHashMap.get(FDTFields.BidVolumeArray))
                futureData.setBidVol(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.BidVolumeArray)));
            if (null != inputHashMap.get(FDTFields.Close))
                futureData.setClose(((Number)inputHashMap.get(FDTFields.Close)).longValue());
            if (null != inputHashMap.get(FDTFields.High))
                futureData.setHigh(((Number)inputHashMap.get(FDTFields.High)).longValue());
            if (null != inputHashMap.get(FDTFields.HighLimit))
                futureData.setHighLimited(((Number)inputHashMap.get(FDTFields.HighLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Low))
                futureData.setLow(((Number)inputHashMap.get(FDTFields.Low)).longValue());
            if (null != inputHashMap.get(FDTFields.LowLimit))
                futureData.setLowLimited(((Number)inputHashMap.get(FDTFields.LowLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Last))
                futureData.setMatch(((Number)inputHashMap.get(FDTFields.Last)).longValue());
            if (null != inputHashMap.get(FDTFields.Open))
                futureData.setOpen(((Number)inputHashMap.get(FDTFields.Open)).longValue());
            if (null != inputHashMap.get(FDTFields.OpenInterest))
                futureData.setOpenInterest(((Number)inputHashMap.get(FDTFields.OpenInterest)).longValue());
            if (null != inputHashMap.get(FDTFields.PreSettlePrice))
                futureData.setPreClose(((Number)inputHashMap.get(FDTFields.PreSettlePrice)).longValue());
            if (null != inputHashMap.get(FDTFields.SettlePrice))
                futureData.setSettlePrice(((Number)inputHashMap.get(FDTFields.SettlePrice)).longValue());
            if (null != inputHashMap.get(FDTFields.Status))
                futureData.setStatus(((Number)inputHashMap.get(FDTFields.Status)).intValue());
            if (null != inputHashMap.get(FDTFields.Time))
                futureData.setTime(((Number)inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.TradingDay))
                futureData.setTradingDay(((Number)inputHashMap.get(FDTFields.TradingDay)).intValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                futureData.setVolume(((Number)inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                futureData.setTurnover(((Number)inputHashMap.get(FDTFields.Turnover)).longValue());
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
                            stockData = stockDataBySymbolMap.get(value);
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
                        case "TotalAskVol":
                            stockData.setTotalAskVol(Long.parseLong(value));
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
     * Parser input HashMap to Wind StockData Object
     * @param inputHashMap
     * @param stockDataBySymbolMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public StockData convertToStockData(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, StockData> stockDataBySymbolMap) throws UnsupportedEncodingException {
        StockData stockData = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String symbol = new String((byte[]) inputHashMap.get(FDTFields.WindSymbolCode), "UTF-8");
            if (stockDataBySymbolMap.containsKey(symbol)) {
                stockData = stockDataBySymbolMap.get(symbol);
            } else {
                stockData = new StockData();
                stockData.setWindCode(symbol);
                stockData.setCode(symbol.split("\\.")[0]);
                stockDataBySymbolMap.put(symbol, stockData);
            }
            if (null != inputHashMap.get(FDTFields.ActionDay))
                stockData.setActionDay(((Number) inputHashMap.get(FDTFields.ActionDay)).intValue());
            if (null != inputHashMap.get(FDTFields.AskPriceArray))
                stockData.setAskPrice(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.AskPriceArray)));
            if (null != inputHashMap.get(FDTFields.AskVolumeArray))
                stockData.setAskVol(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.AskVolumeArray)));
            if (null != inputHashMap.get(FDTFields.BidPriceArray))
                stockData.setBidPrice(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.BidPriceArray)));
            if (null != inputHashMap.get(FDTFields.BidVolumeArray))
                stockData.setBidVol(parseArrayListTolongArray((ArrayList<Number>) inputHashMap.get(FDTFields.BidVolumeArray)));
            if (null != inputHashMap.get(FDTFields.Close))
                stockData.setClose(((Number) inputHashMap.get(FDTFields.Close)).longValue());
            if (null != inputHashMap.get(FDTFields.High))
                stockData.setHigh(((Number) inputHashMap.get(FDTFields.High)).longValue());
            if (null != inputHashMap.get(FDTFields.HighLimit))
                stockData.setHighLimited(((Number) inputHashMap.get(FDTFields.HighLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Low))
                stockData.setLow(((Number) inputHashMap.get(FDTFields.Low)).longValue());
            if (null != inputHashMap.get(FDTFields.LowLimit))
                stockData.setLowLimited(((Number) inputHashMap.get(FDTFields.LowLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Last))
                stockData.setMatch(((Number) inputHashMap.get(FDTFields.Last)).longValue());
            if (null != inputHashMap.get(FDTFields.Open))
                stockData.setOpen(((Number) inputHashMap.get(FDTFields.Open)).longValue());
            if (null != inputHashMap.get(FDTFields.PreClose))
                stockData.setPreClose(((Number) inputHashMap.get(FDTFields.PreClose)).longValue());
            if (null != inputHashMap.get(FDTFields.Status))
                stockData.setStatus(((Number) inputHashMap.get(FDTFields.Status)).intValue());
            if (null != inputHashMap.get(FDTFields.Time))
                stockData.setTime(((Number) inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.TradingDay))
                stockData.setTradingDay(((Number) inputHashMap.get(FDTFields.TradingDay)).intValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                stockData.setVolume(((Number) inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                stockData.setTurnover(((Number) inputHashMap.get(FDTFields.Turnover)).longValue());
            if (null != inputHashMap.get(FDTFields.NumberOfTrades))
                stockData.setNumTrades(((Number) inputHashMap.get(FDTFields.NumberOfTrades)).longValue());
            if (null != inputHashMap.get(FDTFields.TotalBidVolume))
                stockData.setTotalBidVol(((Number) inputHashMap.get(FDTFields.TotalBidVolume)).longValue());
            if (null != inputHashMap.get(FDTFields.TotalAskVolume))
                stockData.setTotalAskVol(((Number) inputHashMap.get(FDTFields.TotalAskVolume)).longValue());
            if (null != inputHashMap.get(FDTFields.WgtAvgAskPrice))
                stockData.setWeightedAvgAskPrice(((Number) inputHashMap.get(FDTFields.WgtAvgAskPrice)).longValue());
            if (null != inputHashMap.get(FDTFields.WgtAvgBidPrice))
                stockData.setWeightedAvgBidPrice(((Number) inputHashMap.get(FDTFields.WgtAvgBidPrice)).longValue());
            if (null != inputHashMap.get(FDTFields.WgtAvgAskPrice))
                stockData.setWeightedAvgAskPrice(((Number) inputHashMap.get(FDTFields.WgtAvgAskPrice)).longValue());
            if (null != inputHashMap.get(FDTFields.YieldToMaturity))
                stockData.setYieldToMaturity(((Number) inputHashMap.get(FDTFields.YieldToMaturity)).intValue());
            if (null != inputHashMap.get(FDTFields.Prefix))
                stockData.setPrefix(new String((byte[]) inputHashMap.get(FDTFields.Prefix), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.Syl1))
                stockData.setYieldToMaturity(((Number) inputHashMap.get(FDTFields.Syl1)).intValue());
            if (null != inputHashMap.get(FDTFields.Syl2))
                stockData.setYieldToMaturity(((Number) inputHashMap.get(FDTFields.Syl2)).intValue());
            if (null != inputHashMap.get(FDTFields.SD2))
                stockData.setYieldToMaturity(((Number) inputHashMap.get(FDTFields.SD2)).intValue());
        }
        return stockData;
    }
}