package com.cyanspring.common.type;

import java.util.HashMap;

public enum TimeInForce {
	DAY('0'),
	GOOD_TILL_CANCEL('1'),
	AT_THE_OPENING('2'),
	IMMEDIATE_OR_CANCEL('3'),
	FILL_OR_KILL('4'),
	GOOD_TILL_CROSSING('5'),
	GOOD_TILL_DATE('6'),
	AT_THE_CLOSE('7'),
	;
	
	static private HashMap<Character, TimeInForce> map = new HashMap<Character, TimeInForce>();
	static {
		for (TimeInForce tif: TimeInForce.values()) {
			map.put(tif.value(), tif);
		}
	}
	
	private char value;
	TimeInForce(char value) {
		this.value = value;
	}
	
	public char value() {
		return value;
	}
	
	static public TimeInForce getTif(char c) {
		return map.get(c);
	}
	  
}
