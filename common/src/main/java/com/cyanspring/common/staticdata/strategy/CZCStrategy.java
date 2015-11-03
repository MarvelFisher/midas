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

public class CZCStrategy extends AbstractRefDataStrategy {

	protected static final Logger log = LoggerFactory.getLogger(CZCStrategy.class);

	@Override
	public void init(Calendar cal, Map<String, Quote> map) throws Exception {
		super.init(cal, map);
	}

    @Override
    public List<RefData> updateRefData(RefData refData) {
    	// Get settlement date in current month for contract policy use
    	Calendar cal = Calendar.getInstance();
		setSettlementDate(refData, cal);
    	List<RefData> lstRefData = super.updateRefData(refData);

		try {
			for (RefData data : lstRefData) {
				String enName = data.getENDisplayName();
				if (enName != null && enName.length() > 4) {
					String yymm = enName.substring(enName.length() - 4);
					Date d = new SimpleDateFormat("yyMM").parse(yymm);
					cal.setTime(d);
					setSettlementDate(data, cal);
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

    private void setSettlementDate(RefData refData, Calendar cal) {
    	if(refData.getCategory().equals("TC")){//動力煤
			refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,5));
		}else{
			refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,10));
		}
    }

}
