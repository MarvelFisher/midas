package com.fdt.lts.client.obj;

import java.util.concurrent.locks.ReentrantLock;


public class Order {
	// for thread safe
	private final ReentrantLock lock = new ReentrantLock();

	private String id;
	private String symbol;
	private OrderSide side;
	private OrderType type;
	private double price;
	private long quantity;
	private double stopLossPrice;
	private String status;
	private String state;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		try {
			lock.lock();
			this.status = status;
		} finally {
			lock.unlock();
		}
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		try {
			lock.lock();
			this.state = state;
		} finally {
			lock.unlock();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		try {
			lock.lock();
			this.id = id;
		} finally {
			lock.unlock();
		}
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		try {
			lock.lock();
			this.price = price;
		} finally {
			lock.unlock();
		}
	}

	public void setQuantity(long quantity) {
		try {
			lock.lock();
			this.quantity = quantity;
		} finally {
			lock.unlock();
		}
	}

	public String getSymbol() {

		return symbol;

	}

	public void setSymbol(String symbol) {
		try {
			lock.lock();
			this.symbol = symbol;
		} finally {
			lock.unlock();
		}
	}

	public com.cyanspring.common.type.OrderSide getSide() {
		return com.cyanspring.common.type.OrderSide.valueOf(side.toString());
	}

	public void setSide(OrderSide side) {
		try {
			lock.lock();
			this.side = side;
		} finally {
			lock.unlock();
		}
	}

	public com.cyanspring.common.type.OrderType getType() {
		return com.cyanspring.common.type.OrderType.valueOf(type.toString());
	}

	public void setType(OrderType type) {
		try {
			lock.lock();
			this.type = type;
		} finally {
			lock.unlock();
		}
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		try {
			lock.lock();
			this.quantity = quantity;
		} finally {
			lock.unlock();
		}
	}

	public double getStopLossPrice() {
		return stopLossPrice;
	}

	public void setStopLossPrice(double stopLossPrice) {
		try {
			lock.lock();
			this.stopLossPrice = stopLossPrice;
		} finally {
			lock.unlock();
		}
	}
}
