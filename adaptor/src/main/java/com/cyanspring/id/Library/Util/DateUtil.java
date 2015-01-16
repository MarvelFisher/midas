package com.cyanspring.id.Library.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtil {
	
	/**
	 * convert local time to gmt time
	 * @param date local time
	 * @return gmt time
	 */
	public static Date toGmt(Date date) {
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date(date.getTime() - tz.getRawOffset());

		// if we are now in DST, back off by the delta. Note that we are
		// checking the GMT date, this is the KEY.
		if (tz.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());

			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before
			// the change for PDT)
			if (tz.inDaylightTime(dstDate)) {
				ret = dstDate;
			}
		}
		return ret;
	}
	
	public static Date toLocal(Date date) {
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date(date.getTime() + tz.getRawOffset());

		// if we are now in DST, back off by the delta. Note that we are
		// checking the GMT date, this is the KEY.
		if (tz.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() + tz.getDSTSavings());

			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before
			// the change for PDT)
			if (tz.inDaylightTime(dstDate)) {
				ret = dstDate;
			}
		}
		return ret;
	}
	
	/**
	 * get certain date  day of week 
	 * @param dt certain date
	 * @return day of week
	 */
	public static int getDayofWeek(Date dt)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return c.get(Calendar.DAY_OF_WEEK);	
	}

	public static int getCurTimeValue(Date dt, int field)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return c.get(field);			
	}
	
	public static int getCurTimeValue(int field)
	{
		Calendar c = Calendar.getInstance();
		return c.get(field);			
	}
	
	
	/**
	 * Return current date
	 */
	public static Date now() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * Format current Date (now) as string via strFmt strFmt is in
	 * SimpleDateTime format, e.g. "yyyyMMdd-HHmmss".
	 */
	public static String formatDate(String strFmt) {
		return formatDate(now(), strFmt);
	}

	/**
	 * Format datetime as string via strFmt strFmt SimpleDateTime format, e.g.
	 * "yyyyMMdd-HHmmss".
	 */
	public static String formatDate(Date dt, String strFmt) {
		SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
		return sdf.format(dt);
	}

	/**
	 * Parse string as a Date object via strFmt. Exception is thrown if any
	 * parsing error.
	 * 
	 * @param strValue
	 *            String representation of a Date, e.g. "2010-10-01"
	 * @param strFmt
	 *            SimpleDateTime format, e.g. "yyyyMMdd"
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String strValue, String strFmt)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
		return sdf.parse(strValue);
	}

	/**
	 * Parse string as a Date object via strFmt. If any parsing error,
	 * defaultValue is returned.
	 * 
	 * @param strValue
	 *            String representation of a Date, e.g. "2010-10-01"
	 * @param strFmt
	 *            SimpleDateTime format, e.g. "yyyyMMdd"
	 * @param defaultValue
	 *            Default value to return if any parsing error.
	 * @return
	 */
	public static Date parseDate(String strValue, String strFmt,
			Date defaultValue) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
			return sdf.parse(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return a Date object as year/month/day.
	 * 
	 * @param year
	 *            Year of the Date object. For example 2011.
	 * @param month
	 *            Month of the Date object. 1 = Jan, 2 = Feb, etc.
	 * @param day
	 *            Day of the Date object. 1-based.
	 * @return
	 */
	public static Date makeDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Return a Date object as year/month/day hour:minute:second
	 * 
	 * @param year
	 *            Year of the Date object. For example 2011.
	 * @param month
	 *            Month of the Date object. 1 = Jan, 2 = Feb, etc.
	 * @param day
	 *            Day of the Date object. 1-based.
	 * @param hour
	 *            0 - 23
	 * @param minute
	 *            0 - 59
	 * @param sec
	 *            0 - 59
	 * @return
	 */
	public static Date makeDate(int year, int month, int day, int hour,
			int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Add time to 'dt' and return the adjusted DateTime.
	 * 
	 * @param dt
	 *            original datetime
	 * @param calendarField
	 *            Calendar.xxx
	 * @param amount
	 * @return
	 */
	public static Date add(Date dt, int calendarField, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(calendarField, amount);
		return c.getTime();
	}

	/**
	 * �������s
	 * 
	 * @param Millisecond
	 * @return Date after add
	 */
	public static Date add(long millisecond) {
		return new Date(System.currentTimeMillis() + millisecond);
	}// add

	/**
	 * Compare 2 datetime. Return < 0 if dt1 < dt2, > 0 if dt1 > dt2, 0 if dt1
	 * == dt2
	 */
	public static int compareDate(Date dt1, Date dt2) {
		return dt1.compareTo(dt2);
	}

	public static Date addDate(Date dt, int span, TimeUnit unit) {
		return new Date(dt.getTime() + unit.toMillis(span));
	}

	public static Date addDate(int span, TimeUnit unit) {
		return addDate(new Date(), span, unit);
	}

	public static Date subDate(Date dt, int span, TimeUnit unit) {
		return new Date(dt.getTime() - unit.toMillis(span));
	}

	public static Date subDate(int span, TimeUnit unit) {
		return subDate(new Date(), span, unit);
	}

	public static Date today() {
		Calendar now = Calendar.getInstance();
		// Month in java.util.Calendar is 0-based, so add 1 to simulate .NET:
		return dateForYMDHMS(now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DATE), 0, 0, 0);
	}

	public static Date dateForYMDHMS(int year, int month, int day, int hour,
			int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		// Month in java.util.Calendar is 0-based, so subtract 1:
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}

	public static String formatTime(Date date, boolean bDate) {
		SimpleDateFormat sdf = bDate ? new SimpleDateFormat("yyyy-MM-dd")
				: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	public static int HHMMSS2Time(int nHHMMSS) {
		int nHH = nHHMMSS / 10000;
		int nMM = nHHMMSS / 100 % 100;
		int nSS = nHHMMSS % 100;

		return (nHH * 60 + nMM) * 60 + nSS;
	}

	public static int dateTime2Time(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) * 60
				+ c.get(Calendar.SECOND);
	}
}
