package com.cyanspring.common.util;

public class RoundRobin {
	private int size;
	private int count;
	public RoundRobin(int size) {
		this.size = size;
	}
	public synchronized int next() {
		count = ++count % size;
		return count;
	}
}
