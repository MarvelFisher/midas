package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

public class MarketSessionChecker implements IMarketSessionChecker {

	private static final Logger log = LoggerFactory.getLogger(MarketSessionChecker.class);
	private final String DEFAULT_OPENING = "08:30:00";
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    protected Date tradeDate;
    protected Map<String, MarketSession> stateMap;
    protected ITradeDate tradeDateManager;
    protected MarketSessionType currentType;
    protected String index;

    @Override
    public void init(Date date, RefData refData) throws Exception {
        if (tradeDateManager != null) {
            String currentIndex = getCurrentIndex(date, refData);
            MarketSession session = stateMap.get(currentIndex);
            if (session == null) {
				session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
			}

            String openingTime = session.getOpeningTime();
            if (openingTime == null) {
				openingTime = DEFAULT_OPENING;
			}

            Date opening = getDate(openingTime);
        	if (TimeUtil.getTimePass(date, opening) > 0) {
        		tradeDate = tradeDateManager.currTradeDate(date);
			} else {
				tradeDate = tradeDateManager.preTradeDate(date);
			}
        }
    }

    @Override
    public synchronized MarketSessionData getState(RefData refData) throws Exception {
    	Date date = Clock.getInstance().now();
        MarketSessionData sessionData = null;
        String currentIndex = getCurrentIndex(date, refData);
        MarketSession session = stateMap.get(currentIndex);
        if (session == null) {
			session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
		}
        for (MarketSessionData data : session.getSessionDatas()) {
            if (!compare(data, date)) {
				continue;
			}

            if (data.getSessionType().equals(MarketSessionType.PREMARKET) && tradeDateManager != null) {
                if (currentType != null && !currentType.equals(data.getSessionType())){
                	if( tradeDate == null) {
						continue;
					}
                	log.debug("Change trade date for index: " + index + ", from: " + tradeDate);
                	tradeDate = tradeDateManager.nextTradeDate(tradeDate);
                	log.debug("Change trade date for index: " + index + ", to: " + tradeDate);
                }
            }
            sessionData = new MarketSessionData(data.getSessionType(), data.getStart(), data.getEnd());
            sessionData.setDate(tradeDate);
            currentType = data.getSessionType();
            break;
        }
        return sessionData;
    }

    @Override
    public MarketSessionData searchState(Date date, RefData refData) throws Exception {
        MarketSessionData sessionData = null;
        String currentIndex = getCurrentIndex(date, refData);
        MarketSession session = stateMap.get(currentIndex);
        if (session == null) {
			session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
		}
        for (MarketSessionData data : session.getSessionDatas()) {
            if (!compare(data, date)) {
				continue;
			}
            sessionData = new MarketSessionData(data.getSessionType(), data.getStart(), data.getEnd());
            sessionData.setDate(date);
        }
        return sessionData;
    }

    @Override
	public MarketSession getMarketSession(RefData refData, String date) throws Exception {
    	Date search = sdf.parse(date);
		String currentIndex = getCurrentIndex(search, refData);
		MarketSession session = stateMap.get(currentIndex);
        if (session == null) {
			session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
		}
        return session;
	}

    @Override
    public String getTradeDate() {
        return sdf.format(this.tradeDate);
    }

    protected String getCurrentIndex(Date date, RefData refData) throws ParseException {
        if (refData != null && refData.getSettlementDate() != null){
            String settlementDay = refData.getSettlementDate();
            if (TimeUtil.sameDate(date, sdf.parse(settlementDay))) {
				return MarketSessionIndex.SETTLEMENT_DAY.toString();
			}
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

    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public ITradeDate getTradeDateManager() {
        return tradeDateManager;
    }

    protected boolean compare(MarketSessionData data, Date compare) throws ParseException {

    	data.setDate(compare);
        if (TimeUtil.getTimePass(data.getStartDate(), compare) <= 0 &&
                TimeUtil.getTimePass(data.getEndDate(), compare) >= 0) {
            return true;
        }
        return false;
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

    protected Date getDate(String opening) {
    	String[] time = opening.split(":");
		int hr = Integer.parseInt(time[0]);
		int min = Integer.parseInt(time[1]);
		int sec = Integer.parseInt(time[2]);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hr);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);

		return cal.getTime();
    }

}
