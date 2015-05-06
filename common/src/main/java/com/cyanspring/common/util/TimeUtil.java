/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;

/**
 * @author Dennis Chen
 *
 */
public class TimeUtil {
	public final static long millisInDay = 60 * 60 * 24 * 1000; 

	public static long getTimePass(Date time) {
		Date now = Clock.getInstance().now();
		return now.getTime() - time.getTime();
	}
	
	public static long getTimePass(Date now, Date time) {
		return now.getTime() - time.getTime();
	}
	
	public static Date parseTime(String format, String time) throws ParseException {
		Calendar today, adjust;
		today = Calendar.getInstance();
		today.setTime(new Date());
		adjust = Calendar.getInstance();
		adjust.setTime(new SimpleDateFormat(format).parse(time));
		adjust.set(Calendar.YEAR, today.get(Calendar.YEAR));
		adjust.set(Calendar.MONTH, today.get(Calendar.MONTH));
		adjust.set(Calendar.DATE, today.get(Calendar.DATE));
		return adjust.getTime();
		
	}

	public static Date getOnlyDate(Date date) {
		Calendar cal = Calendar.getInstance();
		return getOnlyDate(cal, date);
	}
	
	public static Date getOnlyDate(Calendar cal, Date date) {
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getScheduledDate(Calendar cal, Date date, int nHour, int nMin, int nSecond) {
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, nHour);
		cal.set(Calendar.MINUTE, nMin);
		cal.set(Calendar.SECOND, nSecond);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getPreviousDay() {
	   Date result = new Date (Clock.getInstance().now().getTime() -  24 * 60 * 60 * 1000);
	   return result;
	}

	public static Date getPreviousDay(Date date) {
	   Date result = new Date (date.getTime() -  24 * 60 * 60 * 1000);
	   return result;
	}
	
	public static Date getNextDay(Date date) {
		   Date result = new Date (date.getTime() +  24 * 60 * 60 * 1000);
		   return result;
		}
	
	public static boolean sameDate(Date d1, Date d2) {
		if(null == d1 || null == d2)
			return false;
		return getOnlyDate(d1).equals(getOnlyDate(d2));
	}

	public static String formatDate(Date dt, String strFmt) {
		SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
		return sdf.format(dt);
	}

	public static Date parseDate(String strValue, String strFmt)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
		return sdf.parse(strValue);
	}

	public static Date subDate(Date dt, int span, TimeUnit unit) {
		return new Date(dt.getTime() - unit.toMillis(span));
	}

	public static Date addDate(Date dt, int span, TimeUnit unit) {
		return new Date(dt.getTime() + unit.toMillis(span));
	}

	
//	public static String getTradeDate(String tradeDateTime){
//		String[] times = tradeDateTime.split(":");	
//		int nHour = Integer.parseInt(times[0]);
//		int nMin = Integer.parseInt(times[1]);
//		int nSecond = Integer.parseInt(times[2]);
//		
//		Calendar cal = Default.getCalendar();
//		Date now = Clock.getInstance().now();
//		Date scheduledToday = getScheduledDate(cal, now, nHour, nMin, nSecond);
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date ret = Clock.getInstance().now();
//		if(getTimePass(now, scheduledToday) < 0)
//			ret =  getPreviousDay(scheduledToday);
//
//		return sdf.format(ret);
//	}
}
