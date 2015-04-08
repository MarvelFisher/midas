package com.cyanspring.common.event.account;

import java.util.Collection;

import com.cyanspring.common.account.PositionPeakPrice;
import com.cyanspring.common.event.AsyncEvent;

public class PmPositionPeakPriceUpdateEvent extends AsyncEvent {
	private Collection<PositionPeakPrice> updates;

	public PmPositionPeakPriceUpdateEvent(Collection<PositionPeakPrice> updates) {
		this.updates = updates;
	}

	public Collection<PositionPeakPrice> getUpdates() {
		return updates;
	}
	
	
}
