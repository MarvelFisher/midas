package com.cyanspring.common.event.account;

import com.cyanspring.common.account.PositionPeakPrice;
import com.cyanspring.common.event.AsyncEvent;

public class PmPositionPeakPriceDeleteEvent extends AsyncEvent {
	private PositionPeakPrice item;

	public PmPositionPeakPriceDeleteEvent(PositionPeakPrice item) {
		super();
		this.item = item;
	}

	public PositionPeakPrice getItem() {
		return item;
	}
	
}
