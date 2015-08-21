package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            case WindDef.MSG_SYS_CODETABLE_RESULT: {
                CodeTableData codeTableData = (CodeTableData) objMsg;
//                log.debug("Get Request-" + codeTableData.getWindCode() + "," + codeTableData.getCnName());
                RefData refData = null;
                if (codeTableData.getSecurityType() < 10) {
                    refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataICHashMap());
                } else {
                    refData = RefDataParser.convertCodeTableToRefData(codeTableData, windRefDataAdapter.getRefDataSCHashMap());
                }
                if (refData != null) {
                    if(!windRefDataAdapter.isSubscribed()) {
                        windRefDataAdapter.getRefDataHashMap().put(codeTableData.getWindCode(), refData);
                    }else{
                        windRefDataAdapter.getRefDataUpdateHashMap().put(codeTableData.getWindCode(), refData);
                    }
                }
            }
            break;
            case WindDef.MSG_WINDGW_CONNECTED: {
                windRefDataAdapter.sendRquestCodeTable(false);
            }
            break;
            case WindDef.MSG_REFDATA_CHECKUPDATE: {
                log.info("RefDataUpdate Check begin");

                if(windRefDataAdapter.getRefDataHashMap() == null || windRefDataAdapter.getRefDataHashMap().size() == 0){
                    log.info("refDataHashMap is empty");
                    return;
                }
                if(windRefDataAdapter.getRefDataUpdateHashMap() == null || windRefDataAdapter.getRefDataUpdateHashMap().size() == 0){
                    log.info("refDataUpdateHashMap is empty");
                    return;
                }
                Set<String> keysInRefDataMap = new HashSet<String>(windRefDataAdapter.getRefDataHashMap().keySet());
                Set<String> keysInRefDataUpdateMap = new HashSet<String>(windRefDataAdapter.getRefDataUpdateHashMap().keySet());
                //Add
                Set<String> refDataAdd = new HashSet<>(keysInRefDataUpdateMap);
                refDataAdd.removeAll(keysInRefDataMap);
                if(refDataAdd.size() > 0){
                    List<RefData> refDataAddList = new ArrayList<>();
                    for(String key: refDataAdd){
                        refDataAddList.add(windRefDataAdapter.getRefDataUpdateHashMap().get(key));
                    }
                    windRefDataAdapter.sendRefDataUpdate(refDataAddList, RefDataUpdateEvent.Action.ADD);
                }
                //Mod CNName
                Set<String> refDataBoth = new HashSet<>(keysInRefDataUpdateMap);
                refDataBoth.retainAll(keysInRefDataMap);
                if(refDataBoth.size() > 0){
                    List<RefData> refDataModList = new ArrayList<>();
                    for(String key: refDataBoth){
                        String originCNName = windRefDataAdapter.getRefDataHashMap().get(key).getCNDisplayName();
                        String updateCNName = windRefDataAdapter.getRefDataUpdateHashMap().get(key).getCNDisplayName();
                        if(!originCNName.equals(updateCNName)) refDataModList.add(windRefDataAdapter.getRefDataUpdateHashMap().get(key));
                    }
                    if(refDataModList.size() > 0){
                        windRefDataAdapter.sendRefDataUpdate(refDataModList, RefDataUpdateEvent.Action.MOD);
                    }
                }
                //Del
                Set<String> refDataDel = new HashSet<>(keysInRefDataMap);
                refDataDel.removeAll(refDataBoth);
                if(refDataDel.size() > 0){
                    List<RefData> refDataDelList = new ArrayList<>();
                    for(String key: refDataDel){
                        refDataDelList.add(windRefDataAdapter.getRefDataHashMap().get(key));
                    }
                    windRefDataAdapter.sendRefDataUpdate(refDataDelList, RefDataUpdateEvent.Action.DEL);
                }

                //override refDataMap
                windRefDataAdapter.getRefDataHashMap().clear();
                windRefDataAdapter.getRefDataHashMap().putAll(windRefDataAdapter.getRefDataUpdateHashMap());

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
