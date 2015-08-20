package com.cyanspring.apievent.obj;

import java.util.Date;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class Order {

	private String id;
	private String symbol;
	private OrderSide side;
	private OrderType type;
	private double price;
	private long quantity;
	private double stopLossPrice;
	private String status;
	private long cumQty;
	private double avgPx;
    private Date created;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public OrderType getType() {
		return type;
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

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }
    
    public long getCumQty() {
		return cumQty;
	}

	public void setCumQty(long cumQty) {
		this.cumQty = cumQty;
	}

	public double getAvgPx() {
		return avgPx;
	}

	public void setAvgPx(double avgPx) {
		this.avgPx = avgPx;
	}

	public String toString(){
		double price = this.price != 0 ? this.price : this.stopLossPrice;
		return id + ", " + symbol + ", " + side.toString() + ", " + type.toString() + ", " + price
		+ ", " + quantity + ", " + status + ", " + created.toString();
	}
}
