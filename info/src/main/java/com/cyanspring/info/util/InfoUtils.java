package com.cyanspring.info.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoUtils 
{
	private static final Logger log = LoggerFactory
			.getLogger(InfoUtils.class);
	public static String utf8Encode(String org)
	{
		String encode;
		try 
		{
			encode = URLEncoder.encode(org, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			log.warn("CDPutf8Encode: Unsupported Encoding UTF-8, origin: " + org);
			encode = org;
		}
		return encode;
	}
	
	public static String utf8Decode(String org)
	{
		String decode;
		try 
		{
			decode = URLDecoder.decode(org, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			log.warn("CDPutf8Encode: Unsupported Encoding UTF-8, origin: " + org);
			decode = org;
		}
		return decode;
	}
	
	public static int getWeek(String strDate) 
	{
		// strDate: yyyy-MM-dd
		int year,month,day,total_day;
		int  monthday[] = {0,31,59,90,120,151,181,212,243,273,304,334,365};
		int smonthday[] = {0,31,60,91,121,152,182,213,244,274,305,335,366};

	 	try 
	 	{
	 		year  = Integer.parseInt(strDate.substring(0,4));
	 		month = Integer.parseInt(strDate.substring(5,7));
			day   = Integer.parseInt(strDate.substring(8,10));
		} 
	 	catch (Exception e) 
	 	{
	 		return 0;
		}
	        
		if (year%4==0)
			total_day = (year-1)*365+(year-1)/4+smonthday[month-1]+(day-1);
		else
			total_day = (year-1)*365+(year-1)/4+ monthday[month-1]+(day-1);

		return total_day / 7;
	}
	
	public static boolean deleteDirectory(File directory) 
	{
	    if(directory.exists())
	    {
	        File[] files = directory.listFiles();
	        if(null!=files)
	        {
	            for(int i=0; i<files.length; i++) 
	            {
	                if(files[i].isDirectory()) 
	                {
	                    deleteDirectory(files[i]);
	                }
	                else 
	                {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
	public static List<String> getDirectorys(File directory) 
	{
		List<String> retList = new ArrayList<String>();
	    if(directory.exists())
	    {
	        File[] files = directory.listFiles();
	        for (File file : files)
	        {
	        	retList.add(file.getName());
	        }
	    }
	    return retList;
	}
}
