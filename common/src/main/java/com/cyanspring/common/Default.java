package com.cyanspring.common;

import java.util.Calendar;
import java.util.TimeZone;

public class Default {
	private static String user = "default";
	private static String account = "default";
	private static String currency = "USD";
	private static String accountPrefix = "A";
	private static double accountCash = 100000.0;
	private static double marginTimes = 40.0;
	private static double commision = 0.0;
	private static TimeZone timeZone;
	
	protected static void setValues(
		String user,  
		String account,
		String currency,
		String accountPrefix,
		double accountCash,
		double marginTimes,
		double commision,
		TimeZone timeZone
							) {
		Default.user = user;
		Default.account = account;
		Default.currency = currency;
		Default.accountPrefix = accountPrefix;
		Default.accountCash = accountCash;
		Default.marginTimes = marginTimes;
		Default.commision = commision;
		Default.timeZone = timeZone;
	}
	
	static public String getUser() {
		return user;
	}

	static public String getAccount() {
		return account;
	}

	static public String getCurrency() {
		return currency;
	}

	static public String getAccountPrefix() {
		return accountPrefix;
	}

	static public double getAccountCash() {
		return accountCash;
	}

	static public double getMarginTimes() {
		return marginTimes;
	}

	public static double getCommision() {
		return commision;
	}
	
	public static TimeZone getTimeZone() {
		return timeZone;
	}
	
	public static Calendar getCalendar() {
		Calendar cal = Calendar.getInstance();
		if(timeZone != null)
			cal.setTimeZone(Default.getTimeZone());

		return cal;
	}
	
}
