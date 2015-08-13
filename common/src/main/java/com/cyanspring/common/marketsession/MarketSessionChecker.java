package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

public class MarketSessionChecker implements IMarketSession {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Date tradeDate;
    private Map<String, MarketSession> stateMap;
    private ITradeDate tradeDateManager;
    private MarketSessionType currentType;
    private String index;

    @Override
    public void init(Date date, RefData refData) throws Exception {
        if (tradeDateManager != null) {
            String currentIndex = getCurrentIndex(date, refData);
            MarketSession session = stateMap.get(currentIndex);
            if (session == null)
                session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
            for (MarketSessionData data : session.getSessionDatas()) {
                if (!data.getSessionType().equals(MarketSessionType.PREOPEN))
                    continue;
                if (TimeUtil.getTimePass(date, data.getStartDate()) > 0) {
                    tradeDate = date;
                    return;
                }
            }
            tradeDate = tradeDateManager.preTradeDate(date);
        }
    }

    @Override
    public MarketSessionData getState(Date date, RefData refData) throws Exception {
        MarketSessionData sessionData = null;
        String currentIndex = getCurrentIndex(date, refData);
        MarketSession session = stateMap.get(currentIndex);
        if (session == null)
            session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
        for (MarketSessionData data : session.getSessionDatas()) {
            if (!compare(data, date))
                continue;
            sessionData = new MarketSessionData(data.getSessionType(), data.getStart(), data.getEnd());
            sessionData.setDate(date);
            if (data.getSessionType().equals(MarketSessionType.PREOPEN) && tradeDateManager != null) {
                if (currentType != null && !currentType.equals(data.getSessionType())){
                	if( tradeDate == null)
                		continue;
                	tradeDate = tradeDateManager.nextTradeDate(tradeDate);                	
                }
            }
            currentType = data.getSessionType();
        }
        return sessionData;
    }

    @Override
    public MarketSessionData searchState(Date date, RefData refData) throws Exception {
        MarketSessionData sessionData = null;
        String currentIndex = getCurrentIndex(date, refData);
        MarketSession session = stateMap.get(currentIndex);
        if (session == null)
            session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
        for (MarketSessionData data : session.getSessionDatas()) {
            if (!compare(data, date))
                continue;
            sessionData = new MarketSessionData(data.getSessionType(), data.getStart(), data.getEnd());
            sessionData.setDate(date);
        }
        return sessionData;
    }

    @Override
    public String getTradeDate() {
        return sdf.format(this.tradeDate);
    }

    private String getCurrentIndex(Date date, RefData refData) throws ParseException {
        if (refData != null && refData.getSettlementDate() != null){
            String settlementDay = refData.getSettlementDate();
            if (TimeUtil.sameDate(date, sdf.parse(settlementDay)))
                return MarketSessionIndex.SETTLEMENT_DAY.toString();
        }

        if (tradeDateManager == null) {
            return MarketSessionIndex.DEFAULT.toString();
        }

        Date preDate = TimeUtil.getPreviousDay(date);
        if (tradeDateManager.isHoliday(date) && tradeDateManager.isHoliday(preDate)) {
            return MarketSessionIndex.HOLIDAY.toString();
        } else if (tradeDateManager.isHoliday(date) && !tradeDateManager.isHoliday(preDate)) {
            return MarketSessionIndex.FIRST_HOLIDAY.toString();
        } else if (!tradeDateManager.isHoliday(date) && tradeDateManager.isHoliday(preDate)) {
            return MarketSessionIndex.FIRST_WORKING_DAY.toString();
        } else {
            return MarketSessionIndex.DEFAULT.toString();
        }
    }

    @Override
    public Map<String, MarketSession> getStateMap() {
        return stateMap;
    }

    private boolean compare(MarketSessionData data, Date compare) throws ParseException {
    	
    	data.setDate(compare);   	
        if (TimeUtil.getTimePass(data.getStartDate(), compare) <= 0 &&
                TimeUtil.getTimePass(data.getEndDate(), compare) >= 0) {
            return true;
        }
        return false;
    }

    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }

    public ITradeDate getTradeDateManager() {
        return tradeDateManager;
    }

    public void setTradeDateManager(ITradeDate tradeDateManager) {
        this.tradeDateManager = tradeDateManager;
    }

    public void setStateMap(Map<String, MarketSession> stateMap) {
        this.stateMap = stateMap;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
