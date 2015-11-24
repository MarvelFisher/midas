package com.cyanspring.cstw.localevent;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.event.AsyncEvent;

public class GuiSingleInstrumentStrategyUpdateLocalEvent extends AsyncEvent {
	Instrument instrument;

	public GuiSingleInstrumentStrategyUpdateLocalEvent(Instrument instrument) {
		super();
		this.instrument = instrument;
	}

	public Instrument getInstrument() {
		return instrument;
	}
	
}
