package com.cyanspring.common.staticdata.strategy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataUtil;

public class CFStrategy extends AbstractRefDataStrategy  {

	protected static final Logger log = LoggerFactory.getLogger(CFStrategy.class);

    @Override
    public void init(Calendar cal, Map<String, Quote> map) throws Exception {
    	super.init(cal, map);
    }

    @Override
    public List<RefData> updateRefData(RefData refData) {
    	List<RefData> lstRefData = super.updateRefData(refData);
		try {
			for (RefData data : lstRefData) {
				String enName = data.getENDisplayName();
				if (enName != null && enName.length() > 4) {
					String yymm = enName.substring(enName.length() - 4);
					Date d = new SimpleDateFormat("yyMM").parse(yymm);
					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					data.setSettlementDate(RefDataUtil.calSettlementDateByWeekDay(data, cal, 3, Calendar.FRIDAY));
					data.setIndexSessionType(getIndexSessionType(data));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}

		return lstRefData;
    }

    @Override
    public void setRequireData(Object... objects) {
		super.setRequireData(objects);
    }

}
