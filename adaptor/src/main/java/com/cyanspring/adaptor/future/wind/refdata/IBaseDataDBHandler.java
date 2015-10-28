package com.cyanspring.adaptor.future.wind.refdata;

import com.cyanspring.common.staticdata.BaseDBData;

import java.util.HashMap;

/**
 * Created by Shuwei.Kuo on 2015/10/28.
 */
public interface IBaseDataDBHandler {
    public void saveDBDataToQuoteExtendFile(HashMap<String,BaseDBData> BaseDBDataHashMap);
    public HashMap<String, BaseDBData> getBaseDBData();
    public String getExecuteTime();
}
