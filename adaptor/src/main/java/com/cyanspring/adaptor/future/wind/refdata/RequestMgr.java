package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.adaptor.future.wind.data.CodeTableResult;
import com.cyanspring.adaptor.future.wind.data.ExchangeRefData;
import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.common.staticdata.RefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RequestMgr{

    private static final Logger log = LoggerFactory
            .getLogger(RequestMgr.class);

    private WindRefDataAdapter windRefDataAdapter;
    private ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    private Thread controlReqThread = null;

    RequestMgr(WindRefDataAdapter windRefDataAdapter) {
        this.windRefDataAdapter = windRefDataAdapter;
    }

    public void init() {
        if (controlReqThread == null){
            //ControlReqThread control queue task, if queue size > 0 , poll and exec process method.
            controlReqThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if (queue.size() > 0) {
                            Object[] arr;
                            try {
                                arr = (Object[]) queue.poll();
                            }catch (Exception e){
                                log.error(e.getMessage(),e);
                                arr = null;
                            }
                            if (arr == null || arr.length != 2) {
                                continue;
                            }
                            int type = (int) arr[0];
                            process(type, arr[1]);
                        }else{
                            try {
                                TimeUnit.MILLISECONDS.sleep(1);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            });
            controlReqThread.setName("RequsetMgr-" + windRefDataAdapter.getRefDataAdapterName());
            controlReqThread.start();
        }
    }

    public void uninit() {
        if (controlReqThread != null){
            controlReqThread.interrupt();
            controlReqThread = null;
        }
    }

    void addReqData(Object objReq) {
        if(controlReqThread != null){
            queue.offer(objReq);
        }
    }

    void process(int type, Object objMsg) {
        if(objMsg == null) return;
        switch (type) {
            case WindDef.MSG_SYS_CODETABLE: {
                CodeTableData codeTableData = (CodeTableData) objMsg;
                RefData refData = null;
                switch (codeTableData.getSecurityType()) {
                    case WindType.IC_INDEX:
                        refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataICHashMap());
                        break;
                    case WindType.FT_INDEX:
                        refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataFTHashMap());
                        break;
                    case WindType.SC_SHARES_A:
                    case WindType.SC_SHARES_G:
                    case WindType.SC_SHARES_S:
                        refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataSCHashMap());
                        break;
                    case WindType.FC_INDEX:
                    case WindType.FC_COMMODITY:
                    case WindType.FC_INDEX_CX:
                    case WindType.FC_COMMODITY_CX:
                        refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataFCHashMap());
                        break;
                    default:
                        break;
                }
                if (refData != null) {
                    if(!windRefDataAdapter.isSubscribed()) {
                        windRefDataAdapter.getRefDataHashMap().put(codeTableData.getWindCode(), refData);
                    }else{
                        String exchage = codeTableData.getSecurityExchange();
                        if(windRefDataAdapter.getExRefDataHashMap().containsKey(exchage)){
                            ExchangeRefData exchangeRefData = windRefDataAdapter.getExRefDataHashMap().get(exchage);
                            if(exchangeRefData.isRefDataUpdate()){
                                ExchangeRefData exchangeRefDataUpdate;
                                if(windRefDataAdapter.getExRefDataUpdateHashMap().containsKey(exchage)){
                                    exchangeRefDataUpdate = windRefDataAdapter.getExRefDataUpdateHashMap().get(exchage);
                                }else{
                                    exchangeRefDataUpdate = new ExchangeRefData(exchage);
                                    windRefDataAdapter.getExRefDataUpdateHashMap().put(exchage,exchangeRefDataUpdate);
                                }
                                exchangeRefDataUpdate.getRefDataHashMap().put(refData.getSymbol(),refData);
                            }
                        }
                    }
                }else{
                    return;
                }
            }
            break;
            case WindDef.MSG_SYS_CODETABLE_RESULT:{
                CodeTableResult codeTableResult = (CodeTableResult) objMsg;
                ExchangeRefData exchangeRefData = windRefDataAdapter.getExRefDataHashMap().get(codeTableResult.getSecurityExchange());
                exchangeRefData.setHashCode(codeTableResult.getHashCode());
                exchangeRefData.setStatus(true);
                if(!windRefDataAdapter.isSubscribed()){
                    for(ExchangeRefData exchangeRefDataTemp: windRefDataAdapter.getExRefDataHashMap().values()){
                        if(!exchangeRefDataTemp.isStatus()) return;
                    }
                    //ALL exchange status true then sendRequestCodeTable
                    windRefDataAdapter.sendRquestCodeTable(windRefDataAdapter.getMarketsList());
                }else{
                    exchangeRefData.setRefDataUpdate(true);
                    List<String> marketList = new ArrayList<String>();
                    marketList.add(codeTableResult.getSecurityExchange());
                    windRefDataAdapter.sendRquestCodeTable(marketList);
                }
            }
            break;
            case WindDef.MSG_SYS_REQUEST_SNAPSHOT: {
                log.info("Request SnapShot");
                Set<String> codeTableKeySet = new HashSet<>(windRefDataAdapter.getCodeTableDataBySymbolMap().keySet());
                StringBuffer sb = new StringBuffer();
                if(codeTableKeySet != null && codeTableKeySet.size() > 0){
                    for(String symbol : codeTableKeySet){
                        if(sb.toString().equals("")){
                            sb.append(symbol);
                        }else{
                            sb.append(";").append(symbol);
                        }
                    }
                    windRefDataAdapter.sendSubscribe(sb.toString());
                }
            }
            break;
            case WindDef.MSG_SYS_SNAPSHOTENDS: {
                log.info("SnapShot Ends");
                windRefDataAdapter.sendClearSubscribe();
            }
            break;
            default:
                break;
        }
    }
}
