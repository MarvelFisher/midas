package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

/**
 * @author Junfeng
 * @create 26 Nov 2015
 */
public class SubAccountStructureUpdateLocalEvent extends AsyncEvent {
	
	private static final long serialVersionUID = 1L;

	public SubAccountStructureUpdateLocalEvent(String key) {
		super(key);
	}
}
