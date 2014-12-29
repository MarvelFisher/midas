package com.cyanspring.id.Library.Util;

import org.slf4j.Logger;

public class LogUtil {

	public static boolean log4J = true;

	public static void logDebug(Logger log, String f, Object... args) {
		if (log4J) {
			log.debug(String.format(f, args));
		} else {
			com.cyanspring.id.Library.Util.Logger.logDebug(f, args);
		}
	}

	public static void logInfo(Logger log, String f, Object... args) {
		if (log4J) {
			log.info(String.format(f, args));
		} else {
			com.cyanspring.id.Library.Util.Logger.logInfo(f, args);
		}

	}

	public static void logError(Logger log, String f, Object... args) {
		if (log4J) {
			log.error(String.format(f, args));
		} else {
			com.cyanspring.id.Library.Util.Logger.logError(f, args);
		}

	}

	public static void logException(Logger log, Exception e) {
		if (log4J) {
			log.error(getTrace(e));
		} else {
			com.cyanspring.id.Library.Util.Logger.logException(e);
		}

	}

	public static String getTrace(Exception e) {
		String result = String.format("Exception : %s%n%n", e.getMessage());
		result = String.format(
				"%s==========================================%n%n", result);
		StackTraceElement[] arr = e.getStackTrace();
		for (StackTraceElement elm : arr) {
			result = String.format("%s%s%n", result, elm);
		}
		return result;
	}
}
