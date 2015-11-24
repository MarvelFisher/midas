package com.cyanspring.cstw.localevent;

import java.util.List;
import java.util.Map;

public final class SingleInstrumentStrategySelectionLocalEvent extends
		ObjectSelectionLocalEvent {

	public SingleInstrumentStrategySelectionLocalEvent(Map<String, Object> data,
			List<String> editableFields) {
		super(data, editableFields);
	}

}
