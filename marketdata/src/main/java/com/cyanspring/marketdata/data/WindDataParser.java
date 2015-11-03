package com.cyanspring.marketdata.data;


import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.marketdata.type.FDTFields;
import com.cyanspring.marketdata.util.SpellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WindDataParser {

    private static final Logger log = LoggerFactory
            .getLogger(WindDataParser.class);

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
     * Convert String Array To long Array
     *
     * @param str_arr
     * @return long array
     */
    public static long[] parseStringTolong(String[] str_arr) {
        long[] longs = new long[str_arr.length];
        for (int i = 0; i < str_arr.length; i++) {
            longs[i] = Long.parseLong(str_arr[i]);
        }
        return longs;
    }

    /**
     * Convert Number ArrayList to long[]
     * @param arrayList
     * @return
     */
    public static long[] parseArrayListTolongArray(ArrayList<Number> arrayList){
        long[] longs = new long[arrayList.size()];
        for(int i =0; i<arrayList.size(); i++){
            longs[i] = arrayList.get(i).longValue();
        }
        return longs;
    }
}
