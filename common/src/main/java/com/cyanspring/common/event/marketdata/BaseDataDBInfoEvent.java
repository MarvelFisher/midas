package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.BaseDBData;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shuwei on 2015/9/8.
 */
public class BaseDataDBInfoEvent extends RemoteAsyncEvent {
    private HashMap<String, BaseDBData> baseDBDataHashMap;
    private Date timeStamp = Clock.getInstance().now();

    public BaseDataDBInfoEvent(String key, String receiver, HashMap<String, BaseDBData> baseDBDataHashMap) {
        super(key, receiver);
        this.baseDBDataHashMap = baseDBDataHashMap;
    }

    public HashMap<String, BaseDBData> getBaseDBDataHashMap() {
        return baseDBDataHashMap;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
}
