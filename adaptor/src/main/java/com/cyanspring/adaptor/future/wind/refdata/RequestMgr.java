package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.adaptor.future.wind.data.CodeTableResult;
import com.cyanspring.adaptor.future.wind.data.ExchangeRefData;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.staticdata.CodeTableData;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RequestMgr implements IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(RequestMgr.class);

    private WindRefDataAdapter windRefDataAdapter;
    RequestThread thread = null;

    RequestMgr(WindRefDataAdapter windRefDataAdapter) {
        this.windRefDataAdapter = windRefDataAdapter;
    }

    public void init() {
        if (thread == null) {
            thread = new RequestThread(this, "RequestMgr");
            thread.start();
        }
    }

    public void uninit() {
        if (thread != null) {
            thread.close();
            thread = null;
        }
    }

    void addReqData(Object objReq) {
        if (thread != null) {
            thread.addRequest(objReq);
        }
    }

    void process(int type, Object objMsg) {
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
            case WindDef.MSG_REFDATA_CHECKUPDATE: {
                log.info("RefDataUpdate Check begin");
                HashMap<String, ExchangeRefData> refDataUpdateTempMap
                        = new HashMap<String, ExchangeRefData>(windRefDataAdapter.getExRefDataUpdateHashMap());
                HashMap<String, ExchangeRefData> refDateTempMap
                        = new HashMap<String, ExchangeRefData>(windRefDataAdapter.getExRefDataHashMap());
                if(refDataUpdateTempMap.size() == 0){
                    log.info("exRefDataUpdateHashMap is empty");
                    return;
                }
                for(String exchange : refDataUpdateTempMap.keySet()) {
                    log.debug("process exchange refDateUpdate," + exchange);
                    ExchangeRefData exchangeRefData = refDateTempMap.get(exchange);
                    ExchangeRefData exchangeRefDataUpdate = refDataUpdateTempMap.get(exchange);
                    if (exchangeRefData == null || exchangeRefData.getRefDataHashMap().size() == 0) {
                        log.debug("RefDataUpdate:Origin refData empty-" + exchange);
                        continue;
                    }
                    if (exchangeRefDataUpdate == null || exchangeRefDataUpdate.getRefDataHashMap().size() == 0) {
                        log.debug("RefDataUpdate:Update refData empty-" + exchange);
                        continue;
                    }
                    //Process RefDataUpdate Event begin
                    Set<String> keysInRefDataMap = new HashSet<String>(exchangeRefData.getRefDataHashMap().keySet());
                    Set<String> keysInRefDataUpdateMap = new HashSet<String>(exchangeRefDataUpdate.getRefDataHashMap().keySet());
                    //Add
                    Set<String> refDataAdd = new HashSet<>(keysInRefDataUpdateMap);
                    refDataAdd.removeAll(keysInRefDataMap);
                    if (refDataAdd.size() > 0) {
                        List<RefData> refDataAddList = new ArrayList<>();
                        for (String key : refDataAdd) {
                            refDataAddList.add(exchangeRefDataUpdate.getRefDataHashMap().get(key));
                        }
                        windRefDataAdapter.sendRefDataUpdate(refDataAddList, RefDataUpdateEvent.Action.ADD);
                    }
                    //Mod CNName
                    Set<String> refDataBoth = new HashSet<>(keysInRefDataUpdateMap);
                    refDataBoth.retainAll(keysInRefDataMap);
                    if (refDataBoth.size() > 0) {
                        List<RefData> refDataModList = new ArrayList<>();
                        for (String key : refDataBoth) {
                            String originCNName = exchangeRefDataUpdate.getRefDataHashMap().get(key).getCNDisplayName();
                            String updateCNName = exchangeRefDataUpdate.getRefDataHashMap().get(key).getCNDisplayName();
                            if (!originCNName.equals(updateCNName))
                                refDataModList.add(exchangeRefDataUpdate.getRefDataHashMap().get(key));
                        }
                        if (refDataModList.size() > 0) {
                            windRefDataAdapter.sendRefDataUpdate(refDataModList, RefDataUpdateEvent.Action.MOD);
                        }
                    }
                    //Del
                    Set<String> refDataDel = new HashSet<>(keysInRefDataMap);
                    refDataDel.removeAll(refDataBoth);
                    if (refDataDel.size() > 0) {
                        List<RefData> refDataDelList = new ArrayList<>();
                        for (String key : refDataDel) {
                            refDataDelList.add(exchangeRefData.getRefDataHashMap().get(key));
                        }
                        windRefDataAdapter.sendRefDataUpdate(refDataDelList, RefDataUpdateEvent.Action.DEL);
                    }
                    //Set Origin RefDataMap
                    windRefDataAdapter.getExRefDataHashMap().get(exchange).setRefDataUpdate(false);
                    windRefDataAdapter.getExRefDataHashMap().get(exchange)
                            .setRefDataHashMap(new HashMap<String, RefData>(exchangeRefDataUpdate.getRefDataHashMap()));
                    //Process RefDataUpdate Event end
                }
                //clean refData update
                windRefDataAdapter.getExRefDataUpdateHashMap().clear();
                log.debug("RefDataUpdate Check end");
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
}
