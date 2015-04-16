package com.fdt.lts.client;


import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.Quote;

public interface IAdaptor {

	public void onStart();
	public void onAccountUpdate();
	public void onQuote(Quote quote);
	public void onNewOrderReply(Order order);
	public void onAmendOrderReply(Order order);
	public void onCancelOrderReply(Order order);
	public void onOrderUpdate(Order order);
	public void onError(int code, String msg);

}
