package com.cyanspring.common.order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskOrderController {
	private static final Logger log = LoggerFactory
			.getLogger(RiskOrderController.class);

	private int maxRiskOrderCount = 50;
	private int maxTotalOrderCount = 450;
	Map<String, AtomicInteger> userRiskOrderCounts = new ConcurrentHashMap<String, AtomicInteger>();
	private AtomicInteger maxTotalOrderCounts = new AtomicInteger(0);

	public boolean check(String account) {
		AtomicInteger atomicCount = userRiskOrderCounts.get(account);
		if (null == atomicCount) {
			atomicCount = new AtomicInteger(0);
			userRiskOrderCounts.put(account, atomicCount);
		}

		int count = atomicCount.get();
		int totalCount = maxTotalOrderCounts.get();
		if (count >= maxRiskOrderCount || totalCount >= maxTotalOrderCount) {
			log.error("Risk order count reach max, please contact support immediately!!! account: "
					+ account
					+ ","
					+ count
					+ " --> "
					+ maxRiskOrderCount
					+ " , total count:"
					+ totalCount
					+ " --> "
					+ maxTotalOrderCount);
			return false;
		} else {
			atomicCount.incrementAndGet();
			maxTotalOrderCounts.incrementAndGet();
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
		return maxTotalOrderCount;
	}

	public void setMaxTotalOrderCount(int maxTotalOrderCount) {
		this.maxTotalOrderCount = maxTotalOrderCount;
	}

}
