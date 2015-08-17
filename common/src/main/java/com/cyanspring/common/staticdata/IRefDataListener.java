package com.cyanspring.common.staticdata;

import java.util.List;

public interface IRefDataListener {
    void onRefData(List<RefData> refDataList) throws Exception;
    void onRefDataUpdate(List<RefData> refDataList);
}
