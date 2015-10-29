package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.FutureData;
import com.cyanspring.adaptor.future.wind.data.IndexData;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.adaptor.future.wind.data.TransationData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QuoteMgr {

    private static final Logger log = LoggerFactory
            .getLogger(QuoteMgr.class);

    private boolean isModifyTickTime = true;

    private WindGateWayAdapter windGateWayAdapter;
    private BlockingQueue<Object>queue = new LinkedBlockingQueue<>();
    private Thread controlReqThread = null;
    private List<Object> qList = new ArrayList<>();

    QuoteMgr(WindGateWayAdapter windGateWayAdapter){
        this.windGateWayAdapter = windGateWayAdapter;
    }

    public void init() {
        if (controlReqThread == null){
            //ControlReqThread control queue task, if queue size > 0 , poll and exec process method.
            controlReqThread = new Thread(new Runnable() {
                @Override
                public void run() {
                	Object[] arr;
                	int type,cnt;
                	int max = 50;
                    while(true){
                    	qList.clear();
                    	qList.add(queue.poll());
                    	cnt = queue.drainTo(qList) + 1;   	
                    	for (Object obj : qList) {
                    		try {
                    			arr = (Object[]) obj;                    			
                    		} catch (Exception e) {
                    			log.error(e.getMessage(),e);
                    			continue;
                    		}
                    		if (arr == null || arr.length != 2) {
                                continue;
                            }
                            type = (int) arr[0];
                            process(type, arr[1]);
                    	}
                    	if(cnt > max) {
                    		max = cnt;
                    		log.info("windAdaptor queue reach new max: " + max);
                    	} else if ( cnt < (max/2)) {
                    		max = cnt;
                    		log.info("windAdaptor queue down to: " + max);
                    	}
//                            if (queue.size() > 0) {
//                                Object[] arr;
//                                try {
//                                    arr = (Object[]) queue.poll();
//                                }catch (Exception e){
//                                    log.error(e.getMessage(),e);
//                                    arr = null;
//                                }
//                                if (arr == null || arr.length != 2) {
//                                    continue;
//                                }
//                                int type = (int) arr[0];
//                                process(type, arr[1]);
//                            }else{
//                                try {
//                                    TimeUnit.MILLISECONDS.sleep(1);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                    }
                }
            });
            controlReqThread.setName("QuoteMgr-" + windGateWayAdapter.getId());
            controlReqThread.start();
        }
    }

    public void uninit() {
        if (controlReqThread != null){
            controlReqThread.interrupt();
            controlReqThread = null;
        }
    }

    public void AddRequest(Object reqObj) {
        if(controlReqThread != null) {
            queue.offer(reqObj);
        }
    }

    public boolean checkSymbol(String symbol){
        boolean futureExist = FutureItem.futureItemBySymbolMap.containsKey(symbol);
        boolean stockExist = StockItem.stockItemBySymbolMap.containsKey(symbol);
        boolean indexExist = IndexItem.indexItemBySymbolMap.containsKey(symbol);
        return futureExist || stockExist || indexExist;
    }

    void process(int type, Object objMsg) {
        if(objMsg == null) return;
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

    public boolean isModifyTickTime() {
        return isModifyTickTime;
    }

    public void setModifyTickTime(boolean isModifyTickTime) {
        this.isModifyTickTime = isModifyTickTime;
    }
}
