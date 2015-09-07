package com.cyanspring.event.api.obj;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import com.cyanspring.common.util.TimeUtil;

public class SpamController {
	private String account;
	private Calendar cal;
	private AtomicInteger count;
	private int restrict;
	
	public SpamController(String account, int restrict) {
		this.account = account;
		this.restrict = restrict;
		cal = Calendar.getInstance();
		count = new AtomicInteger();
	}
	
	public boolean checkAndCount(Calendar cal){
		if (TimeUtil.sameDate(this.cal.getTime(), cal.getTime())){
			if (count.get() >= restrict)
				return false;
			count.incrementAndGet();
		} else {
			reset();
		}			
		return true;
	}
	
	private void reset() {
		cal = Calendar.getInstance();
		count.set(1);
	}
	
	public String getAccount() {
		return account;
	}
	
	public int getCount() {
		return count.get();
	}
}
