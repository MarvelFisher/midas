package com.cyanspring.adaptor.future.ctp.trader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TotalPosition {
	private Map<String, Double> positions = new ConcurrentHashMap<String, Double>();

	private String getKey(String symbol, boolean isBuy) {
		return symbol + (isBuy?"-B":"-S");
	}
	
	void openPosition(String symbol, boolean isBuy, double qty) {
		Double current = positions.get(getKey(symbol, isBuy));
		if(null != current) {
			qty += current;
		} 
		
		positions.put(getKey(symbol, isBuy), qty);
	}
	
	void contraPosition(String symbol, boolean isBuy, double qty) {
		
	}
}
