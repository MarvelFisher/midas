package com.cyanspring.common;

import java.util.HashMap;
import java.util.Map;

public class ErrorSchema {
	private static String defaultLanguage = "CN";
	private static Map<String, ErrorMsg> map = new HashMap<String, ErrorMsg>();
	private static boolean isBuilt = false;

	public static void build() throws Exception {
		// system error start from 1
		createMsg(1, "CN", "系统错误，请检查日志");
		createMsg(1, "EN", "System error, please check server log");

		createMsg(2, "CN", "用户名或密码错误");
		createMsg(2, "EN", "User name or Password is invalid");

		createMsg(3, "CN", "对不起，该用户未被激活");
		createMsg(3, "EN", "Sorry, User is not active");

		createMsg(4, "CN", "操作失败，该对象已存在");
		createMsg(4, "EN", "Operation failed, object already exists");

		createMsg(5, "CN", "操作失败，该对象不存在");
		createMsg(5, "EN", "Operation failed, object doesn't exist");

		isBuilt = true;
	}

	private static String getKey(int code, String language) {
		return language + code;
	}

	private static void createMsg(int code, String language, String msg)
			throws Exception {
		ErrorMsg errorMsg = new ErrorMsg(code, language, msg);
		ErrorMsg existing = map.put(getKey(code, language), errorMsg);
		if (null != existing)
			throw new Exception("Duplicate error message: " + errorMsg);
	}

	public static String getMsg(int code, String language) {
		if (!isBuilt) {
			try {
				build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ErrorMsg errorMsg = map.get(getKey(code, language));
		if (null != errorMsg) {
			return errorMsg.getMessage();
		}
		return null;
	}

	public static String getMsg(int code) {
		if (!isBuilt) {
			try {
				build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return getMsg(code, defaultLanguage);
	}

	public static String getDefaultLanguage() {
		return defaultLanguage;
	}

	public static void setDefaultLanguage(String defaultLanguage) {
		ErrorSchema.defaultLanguage = defaultLanguage;
	}
}