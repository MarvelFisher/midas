package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.WindBaseDBData;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shuwei on 2015/9/8.
 */
public class WindBaseInfoEvent extends RemoteAsyncEvent {
    private HashMap<String, WindBaseDBData> windBaseDBDataHashMap;
    private Date timeStamp = Clock.getInstance().now();

    public WindBaseInfoEvent(String key, String receiver, HashMap<String, WindBaseDBData> windBaseDBDataHashMap) {
        super(key, receiver);
        this.windBaseDBDataHashMap = windBaseDBDataHashMap;
    }

    public HashMap<String, WindBaseDBData> getWindBaseDBDataHashMap() {
        return windBaseDBDataHashMap;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
}
