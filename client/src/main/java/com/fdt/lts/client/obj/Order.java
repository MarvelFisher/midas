package com.fdt.lts.client.obj;

public class Order {

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
		this.status = status;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public com.cyanspring.common.type.OrderSide getSide() {
		return com.cyanspring.common.type.OrderSide.valueOf(side.toString());
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public com.cyanspring.common.type.OrderType getType() {
		return com.cyanspring.common.type.OrderType.valueOf(type.toString());
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getStopLossPrice() {
		return stopLossPrice;
	}

	public void setStopLossPrice(double stopLossPrice) {
		this.stopLossPrice = stopLossPrice;
	}

	public String toString(){
		double price = this.price != 0 ? this.price : this.stopLossPrice;
		return id + ", " + symbol + ", " + side.toString() + ", " + type.toString() + ", " + price
		+ ", " + quantity + ", " + status + ", " + state;
	}
}
