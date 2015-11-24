package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

/**
 * this event used for SubAccount Management local update event
 * @author Junfeng
 * @create 23 Nov 2015
 */
public class InstrumentPoolUpdateLocalEvent extends AsyncEvent {
	
	private static final long serialVersionUID = 1L;

	public InstrumentPoolUpdateLocalEvent(String key) {
		super(key);
	}
}
