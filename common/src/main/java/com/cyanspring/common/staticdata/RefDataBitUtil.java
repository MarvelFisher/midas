package com.cyanspring.common.staticdata;

public class RefDataBitUtil {
	public static final long STOCK = 0x0001;
	public static final long FUTURES = 0x0002;
	public static final long FOREX = 0x0004;
	public static final long COMMODITY = 0x0008;
	public static final long INDEX = 0x0010;
	public static final long WARRANT = 0x0020;
	public static final long OPTION = 0x0040;
	public static final long CALL_PUT = 0x0080;
	public static final long HOT = 0x0100;
	public static final long CONTINUES = 0x0200;
	public static final long TRADABLE = 0x0400;

	public static boolean isStock(final long type){
		return (type & STOCK) > 0;
	}

	public static boolean isFutures(final long type) {
		return (type & FUTURES) > 0;
	}

	public static boolean isForex(final long type) {
		return (type & FOREX) > 0;
	}

	public static boolean isIndex(final long type) {
		return (type & INDEX) > 0;
	}

}
