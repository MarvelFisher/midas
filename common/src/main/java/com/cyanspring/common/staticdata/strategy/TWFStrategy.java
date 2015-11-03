package com.cyanspring.common.staticdata.strategy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataUtil;

public class TWFStrategy extends AbstractRefDataStrategy {

	protected static final Logger log = LoggerFactory
			.getLogger(TWFStrategy.class);

	private int weekOfMonth = 3;
	private int dayOfWeek = Calendar.WEDNESDAY;
	// The value must be aligned with the CLOSE time
	// of SettlementSession in FITXSessionState.xml/FIMTXSessionState.xml
	private String settlementTime = "13:30:00";
	Calendar currMonthSettleCalendar = null;
	int gracePeriod = 0;

	public int getWeekOfMonth() {
		return weekOfMonth;
	}

	public void setWeekOfMonth(int weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getSettlementTime() {
		return settlementTime;
	}

	public void setSettlementTime(String settlementTime) {
		this.settlementTime = settlementTime;
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
		refData.setSettlementDate(RefDataUtil.calSettlementDateByWeekDay(
    			refData, cal, weekOfMonth, dayOfWeek));
	}

}
