package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.FutureData;
import com.cyanspring.adaptor.future.wind.data.IndexData;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.adaptor.future.wind.data.TransationData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteMgr implements IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(QuoteMgr.class);

    private boolean isModifyTickTime = true;

    private WindGateWayAdapter windGateWayAdapter;
    RequestThread thread = null;

    QuoteMgr(WindGateWayAdapter windGateWayAdapter){
        this.windGateWayAdapter = windGateWayAdapter;
    }

    public void init() {
        if (thread == null) {
            thread = new RequestThread(this, "QuoteMgr");
            thread.start();
        }
    }

    public void uninit() {
        if (thread != null) {
            thread.close();
            thread = null;
        }
    }

    public void AddRequest(Object reqObj) {
        thread.addRequest(reqObj);
    }

    public boolean checkSymbol(String symbol){
        boolean futureExist = FutureItem.futureItemBySymbolMap.containsKey(symbol);
        boolean stockExist = StockItem.stockItemBySymbolMap.containsKey(symbol);
        boolean indexExist = IndexItem.indexItemBySymbolMap.containsKey(symbol);
        return futureExist || stockExist || indexExist;
    }

    public boolean checkFutureSymbol(String strSymbol) {
        return FutureItem.futureItemBySymbolMap.containsKey(strSymbol); //Future
    }

    public void addFutureSymbol(String symbol){
        if (!checkFutureSymbol(symbol)) {
            FutureItem item = new FutureItem(symbol);
            FutureItem.futureItemBySymbolMap.put(symbol, item);
        }
    }

    public boolean checkStockSymbol(String strSymbol) {
        return StockItem.stockItemBySymbolMap.containsKey(strSymbol); //Stock
    }

    public void addStockSymbol(String symbol) {
        if (!checkStockSymbol(symbol)) {
            StockItem item = new StockItem(symbol);
            StockItem.stockItemBySymbolMap.put(symbol, item);
        }
    }

    public boolean checkIndexSymbol(String strSymbol) {
        return IndexItem.indexItemBySymbolMap.containsKey(strSymbol); //Stock
    }

    public void addIndexSymbol(String symbol) {
        if (!checkIndexSymbol(symbol)) {
            IndexItem item = new IndexItem(symbol);
            IndexItem.indexItemBySymbolMap.put(symbol, item);
        }
    }


    void process(int type, Object objMsg) {
        switch (type) {
            case WindDef.MSG_DATA_INDEX: {
                IndexData indexData = (IndexData) objMsg;
                IndexItem.processIndexData(indexData, windGateWayAdapter, this);
            }
            break;
            case WindDef.MSG_DATA_FUTURE: {
                FutureData futureData = (FutureData) objMsg;
                FutureItem.processFutureData(futureData, windGateWayAdapter, this);
            }
            break;
            case WindDef.MSG_DATA_MARKET: {
                StockData stockData = (StockData) objMsg;
                StockItem.processMarketData(stockData, windGateWayAdapter, this);
            }
            break;
            case WindDef.MSG_DATA_TRANSACTION:{
                TransationData transationData = (TransationData) objMsg;
                TransationItem.processTransationData(transationData, windGateWayAdapter, this);
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onStartEvent(RequestThread sender) {

    }

    @Override
    public void onRequestEvent(RequestThread sender, Object reqObj) {
        Object[] arr = (Object[]) reqObj;
        if (arr == null || arr.length != 2) {
            return;
        }
        int type = (int) arr[0];
        process(type, arr[1]);
    }

    @Override
    public void onStopEvent(RequestThread sender) {

    }

    public boolean isModifyTickTime() {
        return isModifyTickTime;
    }

    public void setModifyTickTime(boolean isModifyTickTime) {
        this.isModifyTickTime = isModifyTickTime;
    }
}
