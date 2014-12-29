package com.cyanspring.id.Library.Util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeSpan {

	long timeDiff = 0;

	public static Date add(Date time, TimeSpan ts)
	{
		long lTimeDiff = time.getTime() + ts.getValue();
		return new Date(lTimeDiff);	
	}
	
	public static Date sub(Date time, TimeSpan ts)
	{
		long lTimeDiff = time.getTime() - ts.getValue();
		return new Date(lTimeDiff);
	}
	
	public static TimeSpan getTimeSpan(Date time1, Date time2)
	{
		long lTime1 = time1.getTime();
		long lTime2 = time2.getTime();
		
		return new TimeSpan(lTime1 - lTime2);		
	}	

	public TimeSpan(){		
	}
	
	public TimeSpan(long diff)
	{
		timeDiff = diff;		
	}
	
	public long getValue()
	{
		return timeDiff;
	}	
	
	public long getTotalMillis()
	{
		return (int)timeDiff;
	}
	
	public long getTotalSeconds()
	{
		return timeDiff / TimeUnit.SECONDS.toMillis(1);
	}
	
	public long getTotalMinutes()
	{
		return timeDiff / TimeUnit.MINUTES.toMillis(1);
	}
	
	public long getTotalHours()
	{
		return timeDiff / TimeUnit.HOURS.toMillis(1);
	}
	
	public long getTotalDays()
	{
		return timeDiff / TimeUnit.DAYS.toMillis(1);
	}
}
