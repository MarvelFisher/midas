package com.cyanspring.cstw.ui.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/07/24
 *
 */
public final class LTWStringUtils {

	/**
	 * ######0.00
	 * 
	 * @param d
	 * @return
	 */
	public static String doubleToString(double d) {
		DecimalFormat df = new DecimalFormat("######0.00");
		return df.format(d);
	}

	/**
	 * ######0
	 * 
	 * @param d
	 * @return
	 */
	public static String doubleToString1(double d) {
		DecimalFormat df = new DecimalFormat("######0");
		return df.format(d);
	}

	/**
	 * #,##0.00
	 * 
	 * @param d
	 * @return
	 */
	public static String cashDoubleToString(double d) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormatSymbols.setGroupingSeparator(',');
		DecimalFormat df = new DecimalFormat("#,##0.00", decimalFormatSymbols);
		return df.format(d);
	}

	/**
	 * #,##0.000万
	 * 
	 * @param d
	 * @return
	 */
	public static String cashDoubleToStringWith10000(double d) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormatSymbols.setGroupingSeparator(',');
		DecimalFormat df = new DecimalFormat("#,##0.000'万'",
				decimalFormatSymbols);
		d = d / 10000;
		return df.format(d);
	}

	public static String productivityDoubleToString(double d) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormatSymbols.setGroupingSeparator(',');
		DecimalFormat df = new DecimalFormat("0.00%", decimalFormatSymbols);
		return df.format(d);
	}

	public static String dateToString(Date date) {
		if (date == null) {
			return "";
		}
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return df.format(date);
	}

	public static String dateToNoDateString(Date date) {
		if (date == null) {
			return "";
		}
		DateFormat df = new SimpleDateFormat(" HH:mm:ss");
		return df.format(date);
	}

	/**
	 * 判断前个字符串整体是否和后个字符串末端对齐。
	 * 
	 * @param value
	 * @param longValue
	 * @return
	 */
	public static boolean isLastEqual(String value, String longValue) {
		int length = value.length();
		if (length > longValue.length()) {
			return false;
		}
		String tempValue = longValue.substring(longValue.length() - length,
				longValue.length());

		if (tempValue.equals(value)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断前个字符串整体是否和前个字符串前端对齐。
	 * 
	 * @param value
	 * @param longValue
	 * @return
	 */
	public static boolean isFirstEqual(String value, String longValue) {
		int length = value.length();
		if (length > longValue.length()) {
			return false;
		}
		String tempValue = longValue.substring(0, length);
		if (tempValue.equals(value)) {
			return true;
		}
		return false;
	}
}
