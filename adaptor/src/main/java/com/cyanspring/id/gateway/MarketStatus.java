package com.cyanspring.id.gateway; 

public class MarketStatus {
	public static final int NONE = -1;
	public static final int PREOPEN = 1;
	public static final int OPEN = 2;
	public static final int CLOSE = 0;

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