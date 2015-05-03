package com.cyanspring.common.util;

import java.util.Date;

import com.cyanspring.common.Clock;

public class TimeThrottler {
	private Date lastTime = Clock.getInstance().now();
	private long interval;
	
	public TimeThrottler(long interval) {
		this.interval = interval;
	}
	
	public boolean check() {
		Date now = Clock.getInstance().now();
		if(TimeUtil.getTimePass(now, lastTime) > interval) {
			lastTime = now;
			return true;
		}
		
		return false;
	}
}
