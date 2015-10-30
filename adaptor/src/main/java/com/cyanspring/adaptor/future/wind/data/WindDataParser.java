package com.cyanspring.adaptor.future.wind.data;


import com.cyanspring.network.transport.FDTFields;
import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.common.staticdata.CodeTableData;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
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
     *
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
     *
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
            if (null != inputHashMap.get(FDTFields.Low))
                indexData.setLowIndex(((Number) inputHashMap.get(FDTFields.Low)).longValue());
            if (null != inputHashMap.get(FDTFields.Open))
                indexData.setOpenIndex(((Number) inputHashMap.get(FDTFields.Open)).longValue());
            if (null != inputHashMap.get(FDTFields.PreClose))
                indexData.setPrevIndex(((Number) inputHashMap.get(FDTFields.PreClose)).longValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                indexData.setTotalVolume(((Number) inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Time))
                indexData.setTime(((Number) inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.TradingDay))
                indexData.setTradingDay(((Number) inputHashMap.get(FDTFields.TradingDay)).intValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                indexData.setTurnover(((Number) inputHashMap.get(FDTFields.Turnover)).longValue());
        }
        return indexData;
    }

    /**
     * Parset InputMessage Array To Wind FutureData Object
     *
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
                            futureData.setAskPrice(new long[]{0});
                            futureData.setAskVol(new long[]{0});
                            futureData.setBidPrice(new long[]{0});
                            futureData.setBidVol(new long[]{0});
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
     *
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
                futureData.setAskPrice(new long[]{0});
                futureData.setAskVol(new long[]{0});
                futureData.setBidPrice(new long[]{0});
                futureData.setBidVol(new long[]{0});
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
                futureData.setClose(((Number) inputHashMap.get(FDTFields.Close)).longValue());
            if (null != inputHashMap.get(FDTFields.High))
                futureData.setHigh(((Number) inputHashMap.get(FDTFields.High)).longValue());
            if (null != inputHashMap.get(FDTFields.HighLimit))
                futureData.setHighLimited(((Number) inputHashMap.get(FDTFields.HighLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Low))
                futureData.setLow(((Number) inputHashMap.get(FDTFields.Low)).longValue());
            if (null != inputHashMap.get(FDTFields.LowLimit))
                futureData.setLowLimited(((Number) inputHashMap.get(FDTFields.LowLimit)).longValue());
            if (null != inputHashMap.get(FDTFields.Last))
                futureData.setMatch(((Number) inputHashMap.get(FDTFields.Last)).longValue());
            if (null != inputHashMap.get(FDTFields.Open))
                futureData.setOpen(((Number) inputHashMap.get(FDTFields.Open)).longValue());
            if (null != inputHashMap.get(FDTFields.OpenInterest))
                futureData.setOpenInterest(((Number) inputHashMap.get(FDTFields.OpenInterest)).longValue());
            if (null != inputHashMap.get(FDTFields.PreSettlePrice))
                futureData.setPreSettlePrice(((Number) inputHashMap.get(FDTFields.PreSettlePrice)).longValue());
            if (null != inputHashMap.get(FDTFields.PreClose))
                futureData.setPreClose(((Number) inputHashMap.get(FDTFields.PreClose)).longValue());
            if (null != inputHashMap.get(FDTFields.SettlePrice))
                futureData.setSettlePrice(((Number) inputHashMap.get(FDTFields.SettlePrice)).longValue());
            if (null != inputHashMap.get(FDTFields.Status))
                futureData.setStatus(((Number) inputHashMap.get(FDTFields.Status)).intValue());
            if (null != inputHashMap.get(FDTFields.Time))
                futureData.setTime(((Number) inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.TradingDay))
                futureData.setTradingDay(((Number) inputHashMap.get(FDTFields.TradingDay)).intValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                futureData.setVolume(((Number) inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                futureData.setTurnover(((Number) inputHashMap.get(FDTFields.Turnover)).longValue());
        }
        return futureData;
    }

    /**
     * Parser inputMessage to wind StockData Object
     *
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
                            stockData.setAskPrice(new long[]{0});
                            stockData.setAskVol(new long[]{0});
                            stockData.setBidPrice(new long[]{0});
                            stockData.setBidVol(new long[]{0});
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
                        case "BuyVolume":
                            stockData.setBuyVol(Long.parseLong(value));
                            break;
                        case "BuyTurnover":
                            stockData.setBuyTurnover(Long.parseLong(value));
                            break;
                        case "SellVolume":
                            stockData.setSellVol(Long.parseLong(value));
                            break;
                        case "SellTurnover":
                            stockData.setSellTurnover(Long.parseLong(value));
                            break;
                        case "UnclassifiedVolume":
                            stockData.setUnclassifiedVol(Long.parseLong(value));
                            break;
                        case "UnclassifiedTurnover":
                            stockData.setUnclassifiedTurnover(Long.parseLong(value));
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
     *
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
                stockData.setAskPrice(new long[]{0});
                stockData.setAskVol(new long[]{0});
                stockData.setBidPrice(new long[]{0});
                stockData.setBidVol(new long[]{0});
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
            if (null != inputHashMap.get(FDTFields.BuyVolume))
                stockData.setBuyVol(((Number) inputHashMap.get(FDTFields.BuyVolume)).longValue());
            if (null != inputHashMap.get(FDTFields.BuyTurnover))
                stockData.setBuyTurnover(((Number) inputHashMap.get(FDTFields.BuyTurnover)).longValue());
            if (null != inputHashMap.get(FDTFields.SellVolume))
                stockData.setSellVol(((Number) inputHashMap.get(FDTFields.SellVolume)).longValue());
            if (null != inputHashMap.get(FDTFields.SellTurnover))
                stockData.setSellTurnover(((Number) inputHashMap.get(FDTFields.SellTurnover)).longValue());
            if (null != inputHashMap.get(FDTFields.UnclassifiedVolume))
                stockData.setUnclassifiedVol(((Number) inputHashMap.get(FDTFields.UnclassifiedVolume)).longValue());
            if (null != inputHashMap.get(FDTFields.UnclassifiedTurnover))
                stockData.setUnclassifiedTurnover(((Number) inputHashMap.get(FDTFields.UnclassifiedTurnover)).longValue());
        }
        return stockData;
    }

    /**
     * Parser inputMessage to wind Transation Object
     *
     * @param inputMessageArray
     * @param transationDataBySymbolMap
     * @return
     */
    public TransationData convertToTransationData(String[] inputMessageArray, ConcurrentHashMap<String, TransationData> transationDataBySymbolMap) {
        TransationData transationData = null;
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
                        if (transationDataBySymbolMap.containsKey(value)) {
                            transationData = transationDataBySymbolMap.get(value);
                        } else {
                            transationData = new TransationData();
                            transationData.setWindCode(value);
                            transationDataBySymbolMap.put(value, transationData);
                        }
                    }
                    switch (key) {
                        case "ActionDay":
                            transationData.setActionDay(Integer.parseInt(value));
                            break;
                        case "Last":
                            transationData.setMatch(Long.parseLong(value));
                            break;
                        case "IndexNumber":
                            transationData.setIndexNumber(Integer.parseInt(value));
                            break;
                        case "Time":
                            transationData.setTime(Integer.parseInt(value));
                            break;
                        case "Turnover":
                            transationData.setTurnover(Long.parseLong(value));
                            break;
                        case "Volume":
                            transationData.setVolume(Long.parseLong(value));
                            break;
                        case "BuySellFlag":
                            transationData.setBuySellFlag(Integer.parseInt(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return transationData;
    }

    /**
     * Parser input HashMap to Wind Transation Object
     *
     * @param inputHashMap
     * @param transationDataBySymbolMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public TransationData convertToTransationData(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, TransationData> transationDataBySymbolMap) throws UnsupportedEncodingException {
        TransationData transationData = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String symbol = new String((byte[]) inputHashMap.get(FDTFields.WindSymbolCode), "UTF-8");
            if (transationDataBySymbolMap.containsKey(symbol)) {
                transationData = transationDataBySymbolMap.get(symbol);
            } else {
                transationData = new TransationData();
                transationData.setWindCode(symbol);
                transationDataBySymbolMap.put(symbol, transationData);
            }
            if (null != inputHashMap.get(FDTFields.ActionDay))
                transationData.setActionDay(((Number) inputHashMap.get(FDTFields.ActionDay)).intValue());
            if (null != inputHashMap.get(FDTFields.IndexNumber))
                transationData.setIndexNumber(((Number) inputHashMap.get(FDTFields.IndexNumber)).intValue());
            if (null != inputHashMap.get(FDTFields.Last))
                transationData.setMatch(((Number) inputHashMap.get(FDTFields.Last)).longValue());
            if (null != inputHashMap.get(FDTFields.Time))
                transationData.setTime(((Number) inputHashMap.get(FDTFields.Time)).intValue());
            if (null != inputHashMap.get(FDTFields.Volume))
                transationData.setVolume(((Number) inputHashMap.get(FDTFields.Volume)).longValue());
            if (null != inputHashMap.get(FDTFields.Turnover))
                transationData.setTurnover(((Number) inputHashMap.get(FDTFields.Turnover)).longValue());
            if (null != inputHashMap.get(FDTFields.BuySellFlag))
                transationData.setBuySellFlag(((Number) inputHashMap.get(FDTFields.BuySellFlag)).intValue());
        }
        return transationData;
    }

    /**
     * Parser inputMessage to wind CodeTable Object
     *
     * @param inputMessageArray
     * @param codeTableDataBySymbolMap
     * @return
     */
    public CodeTableData convertToCodeTableData(String[] inputMessageArray, ConcurrentHashMap<String, CodeTableData> codeTableDataBySymbolMap) {
        CodeTableData codeTableData = null;
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
                        if (codeTableDataBySymbolMap.containsKey(value)) {
                            codeTableData = codeTableDataBySymbolMap.get(value);
                        } else {
                            codeTableData = new CodeTableData();
                            codeTableData.setWindCode(value);
                        }
                    }
                    switch (key) {
                        case "CNName":
                            codeTableData.setCnName(value);
                            codeTableData.setSpellName(getSpellName(value,true));
                            break;
                        case "ShortName":
                            codeTableData.setShortName(value);
                            break;
                        case "EnglishName":
                            codeTableData.setEnglishName(value);
                            break;
                        case "SecurityExchange":
                            codeTableData.setSecurityExchange(value);
                            break;
                        case "SecurityType":
                            codeTableData.setSecurityType(Integer.parseInt(value));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return codeTableData;
    }

    /**
     * Parser input HashMap to Wind CodeTable Object
     *
     * @param inputHashMap
     * @param codeTableDataBySymbolMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public CodeTableData convertToCodeTableData(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, CodeTableData> codeTableDataBySymbolMap) throws UnsupportedEncodingException {
        CodeTableData codeTableData = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String symbol = new String((byte[]) inputHashMap.get(FDTFields.WindSymbolCode), "UTF-8");
            if (codeTableDataBySymbolMap.containsKey(symbol)) {
                codeTableData = codeTableDataBySymbolMap.get(symbol);
            } else {
                codeTableData = new CodeTableData();
                codeTableData.setWindCode(symbol);
            }
            if (null != inputHashMap.get(FDTFields.CNName)) {
                codeTableData.setCnName(new String((byte[]) inputHashMap.get(FDTFields.CNName), "UTF-8"));
                codeTableData.setSpellName(getSpellName(codeTableData.getCnName(), true));
            }
            if (null != inputHashMap.get(FDTFields.ShortName))
                codeTableData.setShortName(new String((byte[]) inputHashMap.get(FDTFields.ShortName), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.EnglishName))
                codeTableData.setEnglishName(new String((byte[]) inputHashMap.get(FDTFields.EnglishName), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.SecurityExchange))
                codeTableData.setSecurityExchange(new String((byte[]) inputHashMap.get(FDTFields.SecurityExchange), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.SecurityType))
                codeTableData.setSecurityType(((Number) inputHashMap.get(FDTFields.SecurityType)).intValue());
            if (null != inputHashMap.get(FDTFields.Product))
                codeTableData.setProduct(new String((byte[]) inputHashMap.get(FDTFields.Product), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.ProductName))
                codeTableData.setProductName(new String((byte[]) inputHashMap.get(FDTFields.ProductName), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.SymbolName))
                codeTableData.setSymbolName(new String((byte[]) inputHashMap.get(FDTFields.SymbolName), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.Group))
                codeTableData.setGroup(new String((byte[]) inputHashMap.get(FDTFields.Group), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.Currency))
                codeTableData.setCurrency(new String((byte[]) inputHashMap.get(FDTFields.Currency), "UTF-8"));
            if (null != inputHashMap.get(FDTFields.ShowID)) {
                String showID = new String((byte[]) inputHashMap.get(FDTFields.ShowID), "UTF-8");
                if(!"".equals(showID))codeTableData.setShowID(showID);
            }
        }
        return codeTableData;
    }

    /**
     * Parser input HashMap to Wind CodeTableResult Object
     *
     * @param inputHashMap
     * @param codeTableResultByExchangeMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public CodeTableResult convertToCodeTableResult(HashMap<Integer, Object> inputHashMap, ConcurrentHashMap<String, CodeTableResult> codeTableResultByExchangeMap) throws UnsupportedEncodingException {
        CodeTableResult codeTableResult = null;
        if (inputHashMap != null && inputHashMap.size() > 0) {
            String exchange = new String((byte[]) inputHashMap.get(FDTFields.SecurityExchange), "UTF-8");
            if (codeTableResultByExchangeMap.containsKey(exchange)) {
                codeTableResult = codeTableResultByExchangeMap.get(exchange);
            } else {
                codeTableResult = new CodeTableResult();
                codeTableResult.setSecurityExchange(exchange);
            }
            if (null != inputHashMap.get(FDTFields.ActionDay))
                codeTableResult.setActionDay(((Number) inputHashMap.get(FDTFields.ActionDay)).intValue());
            if (null != inputHashMap.get(FDTFields.HashCode)) {
                long hashCode = ((Number) inputHashMap.get(FDTFields.HashCode)).longValue();
                if(codeTableResult.getHashCode() == hashCode){
                    return null;
                }else {
                    codeTableResult.setHashCode(((Number) inputHashMap.get(FDTFields.HashCode)).longValue());
                }
            }
        }
        return codeTableResult;
    }

    /**
     * convert Chinese Full Name to Spell Name
     *
     * @param fullName
     * @param isSimple
     * @return
     */
    public static String getSpellName(String fullName, boolean isSimple) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        String spellName = "";
        try {
            for (int i = 0; i < fullName.length(); i++) {
                char word = fullName.charAt(i);
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word, format);
                if (pinyinArray == null) { //pinyin4j不能处理非中文
                    spellName = spellName + String.valueOf(word).trim();
                    if (!isSimple) spellName = spellName + " ";
                    continue;
                }
                if (isSimple) {
                    spellName = spellName + pinyinArray[0].substring(0, 1);
                } else {
                    spellName = spellName + pinyinArray[0] + " ";
                }
            }
            if(spellName.contains(WindDef.STOCK_EX_DIVIDENT)) spellName = spellName.replace(WindDef.STOCK_EX_DIVIDENT, "");
            if(spellName.contains(WindDef.STOCK_EX_RIGHT)) spellName = spellName.replace(WindDef.STOCK_EX_RIGHT, "");
            if(spellName.contains(WindDef.STOCK_EX_RIGHT_DIVIDENT)) spellName = spellName.replace(WindDef.STOCK_EX_RIGHT_DIVIDENT,"");
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return changeFullToHalf(spellName);
    }

    /**
     * change String FullWidth to HalfWidth
     *
     * @param originStr
     * @return destStr
     * @see <a href="http://www.utf8-chartable.de/unicode-utf8-table.pl?start=65280&number=128&unicodeinhtml=dec">UTF-8 encoding table</a>
     */
    public static String changeFullToHalf(String originStr) {
        for (char c : originStr.toCharArray()) {
            originStr = originStr.replaceAll("　", " ");
            if ((int) c >= 65281 && (int) c <= 65374) {
                originStr = originStr.replace(c, (char) (((int) c) - 65248));
            }
        }
        return originStr;
    }
}
