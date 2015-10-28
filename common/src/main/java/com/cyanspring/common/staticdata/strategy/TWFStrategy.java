package com.cyanspring.common.staticdata.strategy;

import java.util.Calendar;
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
		Calendar cal = Calendar.getInstance();
		refData.setSettlementDate(RefDataUtil.calSettlementDateByWeekDay(
				refData, cal, weekOfMonth, dayOfWeek));
		refData.setIndexSessionType(getIndexSessionType(refData));

		return super.updateRefData(refData);
	}

	@Override
	public void setRequireData(Object... objects) {
		super.setRequireData(objects);
	}

}
