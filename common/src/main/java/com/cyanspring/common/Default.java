package com.cyanspring.common;

import java.util.Calendar;
import java.util.TimeZone;

public class Default {
	private static String user = "default";
	private static String account = "default";
	private static String market = "FX";
	private static String currency = "USD";
	private static String accountPrefix = "A";
	private static double accountCash = 100000.0;
	private static double marginTimes = 40.0;
	private static double commision = 0.0;
	private static double commisionMin = 0.0;
	private static TimeZone timeZone;
	private static double orderQuantity = 100000;
	private static double positionStopLoss = 1000.0;
	private static double marginCall = 0.95;
	private static String tradeDateTime = "06:15:00";
	
	protected static void setValues(
		String user,  
		String account,
		String market,
		String currency,
		String accountPrefix,
		double accountCash,
		double marginTimes,
		double commision,
		double commisionMin,
		TimeZone timeZone,
		double orderQuantity,
		double positionStopLoss,
		double marginCall,
		String tradeDateTime
							) {
		Default.user = user;
		Default.account = account;
		Default.market = market;
		Default.currency = currency;
		Default.accountPrefix = accountPrefix;
		Default.accountCash = accountCash;
		Default.marginTimes = marginTimes;
		Default.commision = commision;
		Default.commisionMin = commisionMin;
		Default.timeZone = timeZone;
		Default.orderQuantity = orderQuantity;
		Default.positionStopLoss = positionStopLoss;
		Default.marginCall = marginCall;
		Default.tradeDateTime = tradeDateTime;
	}
	
	static public String getUser() {
		return user;
	}

	static public String getAccount() {
		return account;
	}

	public static String getMarket() {
		return market;
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

	public static double getOrderQuantity() {
		return orderQuantity;
	}

	public static double getPositionStopLoss() {
		return positionStopLoss;
	}

	public static double getMarginCall() {
		return marginCall;
	}

	public static double getCommisionMin() {
		return commisionMin;
	}
	
	public static double getCommision(double value) {
		return Math.max(commisionMin, value * commision);
	}

	public static String getTradeDateTime() {
		return tradeDateTime;
	}
	
	
}
