package com.cyanspring.adaptor.future.ctp.trader.client;


public class CtpOrderToCancel {
	private String symbol;
	private String orderRef;
	private int front;
	private int session;
	private byte status;
	public CtpOrderToCancel(String symbol, String orderRef, int front,
			int session, byte status) {
		super();
		this.symbol = symbol;
		this.orderRef = orderRef;
		this.front = front;
		this.session = session;
		this.status = status;
	}
	public String getSymbol() {
		return symbol;
	}
	public String getOrderRef() {
		return orderRef;
	}
	public int getFront() {
		return front;
	}
	public int getSession() {
		return session;
	}
	public byte getStatus() {
		return status;
	}
	
	
	
}
