package com.cyanspring.cstw.event;

import java.util.List;
import java.util.Map;

public class SignalSelectionEvent extends ObjectSelectionEvent {

	public SignalSelectionEvent(Map<String, Object> data,
			List<String> editableFields) {
		super(data, editableFields);
	}

}
