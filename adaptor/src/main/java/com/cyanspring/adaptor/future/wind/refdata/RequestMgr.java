package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.adaptor.future.wind.data.CodeTableData;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RequestMgr implements IReqThreadCallback {

    private static final Logger log = LoggerFactory
            .getLogger(RequestMgr.class);

//    public static RequestMgr instance = new RequestMgr();
//
//    public static RequestMgr instance() {
//        return instance;
//    }
    private WindRefDataAdapter windRefDataAdapter;
    RequestThread thread = null;

    RequestMgr(WindRefDataAdapter windRefDataAdapter){
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
                    windRefDataAdapter.getRefDataHashMap().put(codeTableData.getWindCode(), refData);
                }
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
