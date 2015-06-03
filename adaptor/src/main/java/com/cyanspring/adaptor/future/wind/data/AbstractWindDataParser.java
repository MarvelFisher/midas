package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;

import java.util.ArrayList;

public abstract class AbstractWindDataParser {

    private static final int INDEXSESSION_PREOPEN = 0;
    private static final int INDEXSESSION_OPEN = 1;
    private static final int INDEXSESSION_CLOSE = 2;

    public static int getItemSessionStatus(MarketSessionData marketSessionData){
        int sessionStatus = -1;
        if(MarketSessionType.PREOPEN == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_PREOPEN;
        if(MarketSessionType.OPEN == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_OPEN;
        if(MarketSessionType.CLOSE == marketSessionData.getSessionType()) sessionStatus = INDEXSESSION_CLOSE;
        return sessionStatus;
    }

    /**
     * Convert String Array To long Array
     *
     * @param str_arr
     * @return long array
     */
    public static long[] parseStringTolong(String[] str_arr) {
        long[] longs = new long[str_arr.length];
        for (int i = 0; i < str_arr.length; i++) {
            longs[i] = Long.parseLong(str_arr[i]);
        }
        return longs;
    }

    /**
     * Convert Number ArrayList to long[]
     * @param arrayList
     * @return
     */
    public static long[] parseArrayListTolongArray(ArrayList<Number> arrayList){
        long[] longs = new long[arrayList.size()];
        for(int i =0; i<arrayList.size(); i++){
            longs[i] = arrayList.get(i).longValue();
        }
        return longs;
    }
}
