package com.cyanspring.common.staticdata.strategy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;

public class CZCStrategy extends AbstractRefDataStrategy {

	protected static final Logger log = LoggerFactory.getLogger(CZCStrategy.class);

	@Override
	public void init(Calendar cal, RefData template) {
		super.init(cal, template);
	}

    @Override
    public List<RefData> updateRefData(RefData refData) {

		try {

			setTemplateData(refData);
			String combineCnName = refData.getCNDisplayName();
			Calendar cal = getContractDate(combineCnName);
			if(refData.getCategory().equals("TC")){//動力煤
				refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,5));
			}else{
				refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,10));
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
