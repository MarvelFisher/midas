package com.cyanspring.common.staticdata.strategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
		if (refData != null) {
			setCurrMonthSettleCalendar(refData);
			List<RefData> lstRefData = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				RefData r = (RefData)refData.clone();
				lstRefData.add(r);
			}

			return lstRefData;
		}

		return null;
	}

	@Override
	public void setRequireData(Object... objects) {
		super.setRequireData(objects);
	}

	private String getRefSymbol(RefData refData) throws ParseException {
		Calendar currCalendar = Calendar.getInstance();
		int currMon = currCalendar.get(Calendar.MONTH);
		int currYear = currCalendar.get(Calendar.YEAR);

		if (currMonthSettleCalendar == null) {
			SimpleDateFormat settleSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// Get the settlement date in current month
			String settleDateInCurrMonth = RefDataUtil.calSettlementDateByWeekDay(
					refData, currCalendar, weekOfMonth, dayOfWeek);
			settleDateInCurrMonth = settleDateInCurrMonth + " " + settlementTime;
			currMonthSettleCalendar = Calendar.getInstance();
			currMonthSettleCalendar.setTime(settleSdf.parse(settleDateInCurrMonth));

			// Recognize if current date is before settlement date,
			// if yes, current month is the first trade month, need to plus 1
			// if no, the first trade month would be next month, need not to plus 1
			// ex: current date: 20150901, first tradable is TXFI5, where monthDiff would be 0
			//     thus we plus 1 into monthDiff for being aligned with switch/case
			if (currCalendar.before(currMonthSettleCalendar)) {
				gracePeriod = 1;
			}
		}

		SimpleDateFormat codeSdf = new SimpleDateFormat("yyyyMM");
		// ICE code would look like ICE.TWF.FITX.201606, we take the date part
		String date = refData.getCode().replaceAll("[^0-9]", "");
		Calendar refCalendar = Calendar.getInstance();
		refCalendar.setTime(codeSdf.parse(date));
		int refMon = refCalendar.get(Calendar.MONTH);
		int refYear = refCalendar.get(Calendar.YEAR);
		int monthDiff = ((refYear - currYear) * 12) + refMon - currMon + gracePeriod;

		// For FITX, RefSymbol should look like: TXF00, TXF01...
		// Original RefSymbo: TXFF6, where F6 means 2016 June (ABCDEF, F is the 6th)
		String refSymbol = getRefSymbol(refData.getRefSymbol());
		refSymbol = refSymbol.substring(0, refSymbol.length() - 2).toUpperCase();

		// Append corresponding sequence string for recent 2 months and coming 3 quarter months
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

	private List<String> getTradableMonths() {
		// recent 2 months and coming 3 quarter months
		Calendar currCalendar = Calendar.getInstance();
		int firstTrdMonth = currCalendar.get(Calendar.MONTH);
		if (currCalendar.after(currMonthSettleCalendar)) {
			firstTrdMonth += 1;
		}

		String thisYear = (currCalendar.get(Calendar.YEAR) + "").substring(2);
		String nextYear = ((currCalendar.get(Calendar.YEAR) + 1) + "").substring(2);

		switch (firstTrdMonth) {
		case 0:
			return new ArrayList<>(
					Arrays.asList(thisYear + "01", thisYear + "02",
							thisYear + "03", thisYear + "06", thisYear + "09"));
		case 1:
			return new ArrayList<>(
					Arrays.asList(thisYear + "02", thisYear + "03",
							thisYear + "06", thisYear + "09", thisYear + "12"));
		case 2:
			return new ArrayList<>(
					Arrays.asList(thisYear + "03", thisYear + "04",
							thisYear + "06", thisYear + "09", thisYear + "12"));
		case 3:
			return new ArrayList<>(
					Arrays.asList(thisYear + "04", thisYear + "05",
							thisYear + "06", thisYear + "09", thisYear + "12"));
		case 4:
			return new ArrayList<>(
					Arrays.asList(thisYear + "05", thisYear + "06",
							thisYear + "09", thisYear + "12", nextYear + "03"));
		case 5:
			return new ArrayList<>(
					Arrays.asList(thisYear + "06", thisYear + "07",
							thisYear + "09", thisYear + "12", nextYear + "03"));
		case 6:
			return new ArrayList<>(
					Arrays.asList(thisYear + "07", thisYear + "08",
							thisYear + "09", thisYear + "12", nextYear + "03"));
		case 7:
			return new ArrayList<>(
					Arrays.asList(thisYear + "08", thisYear + "09",
							thisYear + "12", nextYear + "03", nextYear + "06"));
		case 8:
			return new ArrayList<>(
					Arrays.asList(thisYear + "09", thisYear + "10",
							thisYear + "12", nextYear + "03", nextYear + "06"));
		case 9:
			return new ArrayList<>(
					Arrays.asList(thisYear + "10", thisYear + "11",
							thisYear + "12", nextYear + "03", nextYear + "06"));
		case 10:
			return new ArrayList<>(
					Arrays.asList(thisYear + "11", thisYear + "12",
							nextYear + "03", nextYear + "06", nextYear + "09"));
		case 11:
			return new ArrayList<>(
					Arrays.asList(thisYear + "12", nextYear + "01",
							nextYear + "03", nextYear + "06", nextYear + "09"));
		default:
			return null;
		}

	}

	private void setCurrMonthSettleCalendar(RefData refData) throws ParseException {
		Calendar currCalendar = Calendar.getInstance();
		if (currMonthSettleCalendar == null ||
				(currCalendar.get(Calendar.MONTH) - currMonthSettleCalendar.get(Calendar.MONTH) > 0)) {
			SimpleDateFormat settleSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// Get the settlement date in current month
			String settleDateInCurrMonth = RefDataUtil.calSettlementDateByWeekDay(
					refData, currCalendar, weekOfMonth, dayOfWeek);
			settleDateInCurrMonth = settleDateInCurrMonth + " " + settlementTime;
			currMonthSettleCalendar = Calendar.getInstance();
			currMonthSettleCalendar.setTime(settleSdf.parse(settleDateInCurrMonth));
		}
	}

}
