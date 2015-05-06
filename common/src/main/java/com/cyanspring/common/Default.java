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
	private static double marginCut = 100000;
	private static double commission = 0.0;
	private static double commissionMin = 0.0;
	private static TimeZone timeZone;
	private static double orderQuantity = 100000;
	private static double positionStopLoss = 1000.0;
	private static double marginCall = 0.95;
	private static int settlementDays = 0;
	
	private static double stopLossPercent = 0.2;
	private static double freezePercent = 0.2;
	private static double terminatePecent = 0.2;

	private static boolean liveTrading = false;
	private static boolean userLiveTrading = false;
	
	
	protected static void setValues(
		String user,  
		String account,
		String market,
		String currency,
		String accountPrefix,
		double accountCash,
		double marginTimes,
		double marginCut,
		double commission,
		double commissionMin,
		TimeZone timeZone,
		double orderQuantity,
		double positionStopLoss,
		double marginCall,
		int settlementDays,
		double stopLossPercent,
		double freezePercent,
		double terminatePecent,
		boolean liveTrading,
		boolean userLiveTrading) {
		Default.user = user;
		Default.account = account;
		Default.market = market;
		Default.currency = currency;
		Default.accountPrefix = accountPrefix;
		Default.accountCash = accountCash;
		Default.marginTimes = marginTimes;
		Default.marginCut = marginCut;
		Default.commission = commission;
		Default.commissionMin = commissionMin;
		Default.timeZone = timeZone;
		Default.orderQuantity = orderQuantity;
		Default.positionStopLoss = positionStopLoss;
		Default.marginCall = marginCall;
		Default.settlementDays = settlementDays;
		Default.stopLossPercent  = stopLossPercent;
		Default.freezePercent = freezePercent;
		Default.terminatePecent = terminatePecent;
		Default.liveTrading = liveTrading;
		Default.userLiveTrading = userLiveTrading;
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

	public static double getCommission() {
		return commission;
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

	public static double getCommissionMin() {
		return commissionMin;
	}
	
	public static double getCommission(double value) {
		return Math.max(commissionMin, value * commission);
	}	

	public static double getMarginCut() {
		return marginCut;
	}

	public static int getSettlementDays() {
		return settlementDays;
	}

	public static double getStopLossPercent() {
		return stopLossPercent;
	}

	public static double getFreezePercent() {
		return freezePercent;
	}

	public static double getTerminatePecent() {
		return terminatePecent;
	}

	public static boolean isLiveTrading() {
		return liveTrading;
	}

	public static boolean isUserLiveTrading() {
		return userLiveTrading;
	}
	
}
