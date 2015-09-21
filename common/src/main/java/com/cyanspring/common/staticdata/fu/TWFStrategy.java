package com.cyanspring.common.staticdata.fu;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;

public class TWFStrategy extends AbstractRefDataStrategy {

	protected static final Logger log = LoggerFactory
			.getLogger(TWFStrategy.class);

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
			// The settlement date is the 3rd Wednesday in month
			refData.setSettlementDate(RefDataUtil.calSettlementDateByWeekDay(
					refData, cal, 3, Calendar.WEDNESDAY));
			refData.setIndexSessionType(getIndexSessionType(refData));
		} catch (RefDataException e) {
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void setRequireData(Object... objects) {
		super.setRequireData(objects);
	}

}
