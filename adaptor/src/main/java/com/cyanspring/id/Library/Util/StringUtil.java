package com.cyanspring.id.Library.Util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Collection of String related utility functions
 */
public class StringUtil
{
	private StringUtil()
	{
	}
	
	/**
	 * Split strText based on char in separators, and optionally remove the blank entries.
	 * This is similar to C# string split method.
	 * It is more flexible to use guava's Splitter object directly.
	 * TODO: maybe add an option to 'trim' the result set.
	 * @param strText
	 * @param separators contains separator characters, e.g. ";" or ",;"
	 * @param removeEmpty true if empty token is removed from the result set.
	 * @return
	 */
	public static List<String> split(String strText, CharSequence separators, boolean removeEmpty)
	{
		Splitter splitter = Splitter.on(CharMatcher.anyOf(separators));
		if (removeEmpty)
			splitter = splitter.omitEmptyStrings();
			
		Iterable<String> tokens = splitter.split(strText);
		return Lists.newArrayList(tokens);
	}

	public static String[] split(String strText, char separator)
	{
		return split(strText, separator, false);
	}
	
	public static String[] split(String strText, char separator, boolean removeEmpty)
	{
		Splitter splitter = Splitter.on(separator);
		if (removeEmpty)
			splitter = splitter.omitEmptyStrings();
			
		Iterable<String> tokens = splitter.split(strText);
		ArrayList<String> list = Lists.newArrayList(tokens);
		String[] ar = new String[list.size()];
		return list.toArray(ar);
	}
	
	public static List<String> split(String strText, CharSequence separators)
	{
		return split(strText, separators, false);
	
	}
	/**
	 * Parse a comma-separated integer list to an integer array. 
	 * @param strText
	 * @return
	 */
	public static int[] parseIntList(String strText, String chSepList)
	{
		List<String> tokens = split(strText, chSepList, true);
		int[] arrValue = new int[tokens.size()];
		int idx = 0;
		for (String token : tokens)
			arrValue[idx++] = Integer.parseInt(token);
		
		return arrValue;	
	}
	
	public static String formatDouble(int nDP, double dValue)
	{
        String sformat = String.format("%%.%df", nDP);
    	return String.format(sformat, dValue);
	}


}
