package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.AsyncEvent;

public class PresubscribeEvent extends AsyncEvent {
	public PresubscribeEvent(String key) {
		super(key);
	}

}
