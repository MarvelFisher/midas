package com.cyanspring.server.order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskOrderController {
    private static final Logger log = LoggerFactory
            .getLogger(RiskOrderController.class);

	private int maxRiskOrderCount = 20;
	Map<String, AtomicInteger> userRiskOrderCounts = new ConcurrentHashMap<String, AtomicInteger>();
	private AtomicInteger maxTotalOrderCount = new AtomicInteger(200);
	
	public boolean chek(String account){
		AtomicInteger atomicCount = userRiskOrderCounts.get(account);
		if(null == atomicCount) {
			userRiskOrderCounts.put(account, new AtomicInteger(1));
			return true;
		}
		int count = atomicCount.incrementAndGet();
		int totalCount = maxTotalOrderCount.incrementAndGet();
		if(count > maxRiskOrderCount || totalCount > maxTotalOrderCount.get()) {
			log.error("Risk order count reach max, please contact support immediately!!! account: "
					+ account + "," + maxRiskOrderCount + ", " + count);
			return false;
		}
		
		return true;
	}

	public int getMaxRiskOrderCount() {
		return maxRiskOrderCount;
	}

	public void setMaxRiskOrderCount(int maxRiskOrderCount) {
		this.maxRiskOrderCount = maxRiskOrderCount;
	}

	public int getMaxTotalOrderCount() {
		return maxTotalOrderCount.get();
	}

	public void setMaxTotalOrderCount(int maxTotalOrderCount) {
		this.maxTotalOrderCount = new AtomicInteger(maxTotalOrderCount) ;
	}
	
}
