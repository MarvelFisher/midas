package com.cyanspring.cstw.ui.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/07/17
 *
 */
public final class PatternUtils {
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否是保留2位的Double类型
	 * @param str
	 * @return
	 */
	public static boolean isDoubleMaxTwo(String str) {
		Pattern pattern = Pattern.compile("[0-9]+\\.?[0-9]{0,2}");
		return pattern.matcher(str).matches();
	}
}
