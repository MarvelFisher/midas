package com.cyanspring.adaptor.future.ctp.trader.client;

import org.bridj.Pointer;

public class BridjUtils {
	public static Pointer<Byte> stringToBytePointer(String str) {
		Pointer<Byte> bytePointer = Pointer.allocateBytes(str.length()+1);
		bytePointer.setCString(str);
		return bytePointer;
	}
}
