package com.fdt.lts.client;

import com.fdt.lts.client.obj.QuoteData;

public interface IAdaptor {

	public void onStart();
	public void onQuote(QuoteData quote);
	public void onNewOrderReply();
	public void onAmendOrderReply();
	public void onCancelOrderReply();
	public void onError(int code, String msg);

}