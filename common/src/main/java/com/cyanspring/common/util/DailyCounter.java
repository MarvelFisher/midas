package com.cyanspring.common.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.cyanspring.common.Clock;

public class DailyCounter {
	private int limit;
	private Date currentDate;
	private AtomicInteger count = new AtomicInteger(0);

	public DailyCounter(int limit) {
		this.limit = limit;
		currentDate = getToday();
	}
	
	private Date getToday() {
		Date now = Clock.getInstance().now();
		return TimeUtil.getOnlyDate(now);
	}
	
	public boolean check(){
		Date now = getToday();
		if(!currentDate.equals(now)) {
			currentDate = now;
			count = new AtomicInteger(0);
			return true;
		}
		
		int current = count.get();
		if(current > limit)
			return false;
		
		count.incrementAndGet();
		return true;
		
	}
}
