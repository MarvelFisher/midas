package com.cyanspring.common.staticdata;

import java.util.List;

public interface IRefDataListener {
	void init() throws Exception;
	void uninit();
    void onRefData(List<RefData> refDataList) throws Exception;
}
