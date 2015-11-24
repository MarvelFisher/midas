package com.cyanspring.common.staticdata;

import java.util.HashMap;
import java.util.Map;

public class TradeDateOpeningTable {

	private Map<String, String> mapOpeningTime = new HashMap<String, String>() {{
		put("SHF", "20:30:00");
		put("DCE", "20:30:00");
		put("CZC", "20:30:00");
    }};

	public Map<String, String> getMapOpeningTime() {
		return mapOpeningTime;
	}

	public void setMapOpeningTime(Map<String, String> mapOpeningTime) {
		this.mapOpeningTime = mapOpeningTime;
	}

}
