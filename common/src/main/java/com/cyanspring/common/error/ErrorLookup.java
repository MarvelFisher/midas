package com.cyanspring.common.error;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLookup {
	private static final Logger log = LoggerFactory
			.getLogger(ErrorLookup.class);
	private static final Map<Integer, String> map = new HashMap<Integer, String>();
	
	private static void addAndCheck(int code, String message) throws Exception {
		if(map.put(code, message) != null)
			throw new Exception("Critical error: error code duplicate: " + code);
	}
	static {
		try {
			addAndCheck(300, "Server isn't connected");
			addAndCheck(301, "User must login before send any events");
			addAndCheck(302, "Event type not support");
			addAndCheck(303, "Account & user not match");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	public static String lookup(int code) {
		return map.get(code);
	}
}
