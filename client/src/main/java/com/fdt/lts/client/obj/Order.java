package com.fdt.lts.client.obj;

import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;

public class Order {
	public String id;
	public String symbol;
	public OrderSide side;
	public OrderType type;
	public double value;
	public int quantity;	
	public double stopLossPrice;
}
