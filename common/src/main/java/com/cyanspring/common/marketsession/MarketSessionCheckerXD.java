package com.cyanspring.common.marketsession;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
