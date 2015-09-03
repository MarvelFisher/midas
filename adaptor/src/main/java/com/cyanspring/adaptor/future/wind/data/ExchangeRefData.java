package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.common.staticdata.RefData;

import java.util.HashMap;

public class ExchangeRefData {
    private String name;
    private boolean status = false;
    private long hashCode = 0;
    private boolean refDataUpdate = false;
    private HashMap<String, RefData> refDataHashMap = new HashMap<>();

    public ExchangeRefData(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public long getHashCode() {
        return hashCode;
    }

    public void setHashCode(long hashCode) {
        this.hashCode = hashCode;
    }

    public boolean isRefDataUpdate() {
        return refDataUpdate;
    }

    public void setRefDataUpdate(boolean refDataUpdate) {
        this.refDataUpdate = refDataUpdate;
    }

    public HashMap<String, RefData> getRefDataHashMap() {
        return refDataHashMap;
    }

    public void setRefDataHashMap(HashMap<String, RefData> refDataHashMap) {
        this.refDataHashMap = refDataHashMap;
    }
}
