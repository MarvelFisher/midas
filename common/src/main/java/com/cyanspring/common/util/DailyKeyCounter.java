package com.cyanspring.common.util;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;

public class DailyKeyCounter {
	private static final Logger log = LoggerFactory
			.getLogger(DailyKeyCounter.class);

	private int limit;
	private Date currentDate;
	private Map<String, Integer> countMap = new ConcurrentHashMap<String, Integer>();

	public DailyKeyCounter(int limit) {
		this.limit = limit;
		currentDate = getToday();
	}
	
	private Date getToday() {
		Date now = Clock.getInstance().now();
		return TimeUtil.getOnlyDate(now);
	}
	
	public boolean check(String key) {
		Date now = getToday();
		if(!currentDate.equals(now)) {
			countMap.clear();
			countMap.put(key, 1);
			currentDate = now;
			return true;
		}
		
		Integer count = countMap.get(key);
		if(null == count) {
			countMap.put(key, 1);
			return true;
		} 
		
		if(count >= limit)
			return false;
		
		countMap.put(key, count+1);
		return true;
	}
	
}
