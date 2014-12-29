package com.cyanspring.id;

public class MarketStatus {
	public static final int NONE = -1;
	public static final int PREOPEN = 1;
	public static final int OPEN = 2;
	public static final int CLOSE = 0;

	/**
	 * toString : show status in string
	 * @param nStatus
	 * @return string
	 */
	public static String toString(int nStatus) {
		switch (nStatus) {
		case MarketStatus.OPEN:
			return "Open";
		case MarketStatus.CLOSE:
			return "Close";
		case MarketStatus.PREOPEN:
			return "PreOpen";
		default:
			return "None";
		}
	}
}