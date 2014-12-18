package com.cyanspring.common.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerfDurationCounter {
	private static final Logger log = LoggerFactory
			.getLogger(PerfDurationCounter.class);
	private long count = 0;
	private long duration = 0;
	private Date start;
	private TimeThrottler throttler;
	private String name;

	public PerfDurationCounter(String name, long interval) {
		this.name = name;
		throttler = new TimeThrottler(interval);
	}
	public void start() {
		start = new Date();
	}
	
	private void reset() {
		count = 0;
		duration = 0;
		start = new Date();
	}
	
	public void end() {
		duration += new Date().getTime() - start.getTime();
		count++;
		if(throttler.check() && count > 0) {
			log.info(this.name + ": " + (duration/count));
			reset();
		}
	}

}
