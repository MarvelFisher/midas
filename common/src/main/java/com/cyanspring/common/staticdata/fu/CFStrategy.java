package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;

public class CFStrategy extends AbstractRefDataStrategy  {
	
	protected static final Logger log = LoggerFactory.getLogger(CFStrategy.class);
	
    @Override
    public void init(Calendar cal, RefData template) {
    	super.init(cal, template);
    }
    
    @Override
    public void updateRefData(RefData refData) {
		try {
			
			setTemplateData(refData);
			String combineCnName = refData.getCNDisplayName();	
			Calendar cal = getContractDate(combineCnName);
			refData.setSettlementDate(RefDataUtil.calSettlementDateByWeekDay(refData, cal, 3, Calendar.FRIDAY));
			refData.setIndexSessionType(getIndexSessionType(refData));
			
		} catch (RefDataException e){
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    }

    @Override
    public void setRequireData(Object... objects) {
		super.setRequireData(objects);
    }
      
//    private String calSettlementDay(RefData refData,Calendar cal) throws Exception {
//    	if(!StringUtils.hasText(refData.getSymbol())){
//    		log.warn("missing symbol:{}",refData.getRefSymbol());
//    		return "";
//    	}
//    	
//        int dayCount = 0;
//        while (dayCount != 3) {
//            cal.add(Calendar.DAY_OF_MONTH, 1);
//            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
//                dayCount++;
//        }
//
//        while (getMarketSessionUtil().isHoliday(refData.getSymbol(), cal.getTime())) {
//        	
//            cal.add(Calendar.DAY_OF_YEAR, 1);
//        }
//        
//        return getSettlementDateFormat().format(cal.getTime());
//    }
    
}
