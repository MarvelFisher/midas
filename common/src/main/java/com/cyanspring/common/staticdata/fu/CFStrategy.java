package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;

public class CFStrategy extends AbstractRefDataStrategy  {
	
	protected static final Logger log = LoggerFactory.getLogger(CFStrategy.class);
	
    @Override
    public void init(Calendar cal, RefData template) {
    	super.init(cal, template);
    }
    
    @Override
    public void updateRefData(RefData refData) {
		try {
			if( null == getMarketSessionUtil() || null == getTradeDateManager()){
				log.info("refData:{}- marketsessoinutil is null",refData.getCNDisplayName());
				return;
			}
			String combineCnName = refData.getCNDisplayName();
			setTemplateData(refData);
			refData.setSettlementDate(calSettlementDay(refData.getSymbol(),getContractDate(combineCnName)));
			refData.setIndexSessionType(getIndexSessionType(refData));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    }

    @Override
    public void setRequireData(Object... objects) {
		super.setRequireData(objects);
    }
      
    private String calSettlementDay(String symbol,Calendar cal) throws Exception {
        int dayCount = 0;
        while (dayCount != 3) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                dayCount++;
        }

        while (getMarketSessionUtil().isHoliday(symbol, cal.getTime())) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return getSettlementDateFormat().format(cal.getTime());
    }
    
}
