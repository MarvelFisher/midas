package com.cyanspring.common.staticdata.fu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataException;
import com.cyanspring.common.staticdata.RefDataUtil;

public class TWFStrategy extends AbstractRefDataStrategy {

	protected static final Logger log = LoggerFactory
			.getLogger(TWFStrategy.class);

	private int weekOfMonth = 3;
	private int dayOfWeek = Calendar.WEDNESDAY;
	// The value must be aligned with the CLOSE time
	// of SettlementSession in FITXSessionState.xml/FIMTXSessionState.xml
	private String settlementTime = "13:30:00";

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
					refData, cal, weekOfMonth, dayOfWeek));
			refData.setIndexSessionType(getIndexSessionType(refData));
			refData.setRefSymbol(getFITXRefSymbol(refData));
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

	private String getFITXRefSymbol(RefData refData) throws ParseException {
		Calendar currCalendar = Calendar.getInstance();
		int currMon = currCalendar.get(Calendar.MONTH);
		int currYear = currCalendar.get(Calendar.YEAR);

		SimpleDateFormat settleSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Get the settlement date in current month
		String settleDateInCurrMonth = RefDataUtil.calSettlementDateByWeekDay(
				refData, currCalendar, weekOfMonth, dayOfWeek);
		settleDateInCurrMonth = settleDateInCurrMonth + " " + settlementTime;
		Calendar currMonthSettleCalendar = Calendar.getInstance();
		currMonthSettleCalendar.setTime(settleSdf.parse(settleDateInCurrMonth));

		// For FITX, RefSymbol should look like: TXF00, TXF01...
		// Original RefSymbo: TXFF6, where F6 means 2016 June (ABCDEF, F is the 6th)
		String refSymbol = getRefSymbol(refData.getRefSymbol());
		refSymbol = refSymbol.substring(0, refSymbol.length() - 2).toUpperCase();

		SimpleDateFormat codeSdf = new SimpleDateFormat("yyyyMM");
		// ICE code would look like ICE.TWF.FITX.201606, we take the date part
		String date = refData.getCode().replaceAll("[^0-9]", "");
		Calendar refCalendar = Calendar.getInstance();
		refCalendar.setTime(codeSdf.parse(date));
		int refMon = refCalendar.get(Calendar.MONTH);
		int refYear = refCalendar.get(Calendar.YEAR);

		// Recognize if current date is after settlement date,
		// if yes, the first tradable month would be next month, need not to plus 1
		// if no, current month is the first trade month, need to plus 1
		// ex: current date: 20150901, first tradable is TXFI5, where monthDiff would be 0
		//     thus we plus 1 into monthDiff for being aligned with switch/case
		int gracePeriod = 1;
		if (currCalendar.after(currMonthSettleCalendar)) {
			gracePeriod = 0;
		}
		int monthDiff = ((refYear - currYear) * 12) + refMon - currMon + gracePeriod;

		// Recognize recent 2 months and coming 3 quarter months
		switch (monthDiff) {
		case 1:
			refSymbol += "00";
			break;
		case 2:
			refSymbol += "01";
			break;
		case 3:
		case 4:
		case 5:
			refSymbol += "02";
			break;
		case 6:
		case 7:
		case 8:
			refSymbol += "03";
			break;
		case 9:
		case 10:
		case 11:
			refSymbol += "04";
			break;

		default:
			break;
		}

		return refSymbol;
	}

}
