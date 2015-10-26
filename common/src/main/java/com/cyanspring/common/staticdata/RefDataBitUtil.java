package com.cyanspring.common.staticdata;

public class RefDataBitUtil {
	private static final long STOCK = 0x0001;
	private static final long FUTURES = 0x0002;
	private static final long FOREX = 0x0004;
	private static final long COMMODITY = 0x0008;
	private static final long INDEX = 0x0010;
	private static final long WARRANT = 0x0020;
	private static final long OPTION = 0x0040;
	private static final long CALL_PUT = 0x0080;
	private static final long HOT = 0x0100;
	private static final long CONTINUES = 0x0200;
	private static final long TRADABLE = 0x0400;
	
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
