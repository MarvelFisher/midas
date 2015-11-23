package com.cyanspring.common.staticdata.strategy;

import java.text.ParseException;
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
    	Calendar cal = super.getCal();
		setSettlementDate(refData, cal);
    	List<RefData> lstRefData = super.updateRefData(refData);

		try {
			for (RefData data : lstRefData) {
				String enName = data.getENDisplayName();
				if (enName != null && enName.length() > 4) {
					String yymm = enName.substring(enName.length() - 4);
					try {
						Date d = new SimpleDateFormat("yyMM").parse(yymm);
						cal.setTime(d);
						setSettlementDate(data, cal);
						data.setIndexSessionType(getIndexSessionType(data));
					} catch (ParseException e) {
						log.warn("Fail to parse yymm for RefData ENName/Symbol: " +
								data.getENDisplayName() + "/" + data.getSymbol());
					}
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
    	String category = refData.getCategory();
    	if (category.equals("TC") || category.equals("ZC")) { //動力煤
			refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,5));
		} else {
			refData.setSettlementDate(RefDataUtil.calSettlementDateByTradeDate(refData, cal,10));
		}
    }

}
