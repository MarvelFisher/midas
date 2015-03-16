package com.fdt.lts.client;

import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.QuoteData;

public interface IAdaptor {

	public void onStart();
	public void onQuote(QuoteData quote);
	public void onNewOrderReply(Order order);
	public void onAmendOrderReply(Order order);
	public void onCancelOrderReply(Order order);
	public void onOrderUpdate(Order order);
	public void onError(int code, String msg);

}