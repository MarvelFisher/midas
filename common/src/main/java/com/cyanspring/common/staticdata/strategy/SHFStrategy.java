package com.cyanspring.common.staticdata.strategy;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;

/**
 * This strategy is used for Futures Master to change refData settings
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class SHFStrategy extends AbstractRefDataStrategy {
	
	protected static final Logger log = LoggerFactory.getLogger(SHFStrategy.class);

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
			if(refData.getCategory().equals("FU")){
				refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,-1));
			}else{
				refData.setSettlementDate(RefDataUtil.calSettlementDateByDay(refData, cal, 15));
			}
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
}
