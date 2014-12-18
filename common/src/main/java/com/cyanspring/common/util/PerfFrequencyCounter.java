package com.cyanspring.common.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerfFrequencyCounter {
	private static final Logger log = LoggerFactory
			.getLogger(PerfFrequencyCounter.class);
	
	private Date start;
	private long count;
	private String name;
	private TimeThrottler throttler;
	
	public PerfFrequencyCounter(String name, long interval) {
		this.name = name;
		throttler = new TimeThrottler(interval);
	}
	
	private void reset() {
		count = 0;
		start = new Date();
	}
	
	public void count() {
		if(null == start)
			start = new Date();
		
		count++;
		
		if(throttler.check() && count > 0) {
			long duration = (new Date().getTime() - start.getTime())/1000;
			log.info(this.name + ": " + (count/duration));
			reset();
		}
		
	}
}
