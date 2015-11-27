package com.cyanspring.common.marketsession;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

public class MarketSessionCheckerXD extends MarketSessionChecker {

	private static final Logger log = LoggerFactory.getLogger(MarketSessionCheckerXD.class);
	private final String DEFAULT_OPENING = "20:30:00";

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
        		tradeDate = tradeDateManager.nextTradeDate(date);
			} else {
				tradeDate = tradeDateManager.currTradeDate(date);
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

}
