package com.fdt.lts.client;


import com.cyanspring.apievent.obj.Order;

public interface ITrade {
	public void putNewOrder(Order order);
	public void putStopOrder(Order order);
	public void putAmendOrder(Order order);
	public void putCancelOrder(Order order);
	public void terminate();
}

