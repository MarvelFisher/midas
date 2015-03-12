package com.fdt.lts.client;

import com.fdt.lts.client.obj.AccountInfo;
import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.Quote;

public abstract class TradeAdaptor {
	
	protected AccountInfo accountInfo; // need implement thread safe method
	private ITrade adaptor;
	
	public abstract void onQuote(Quote quote);	
	public abstract void onTradeOrderReply();
	public abstract void onAmendOrderReply();
	public abstract void onCancelOrderReply();
	
	public void sendOrder(Order order){
		adaptor.putOrder(order);
	}
	public void sendStopOrder(Order order){
		adaptor.putStopOrder(order);
	}
	public void sendAmendOrder(Order order){
		adaptor.putAmendOrder(order);
	}
	public void sendCancelOrder(Order order){
		adaptor.putCancelOrder(order);
	}
	public void setAdaptor(ITrade adaptor){
		this.adaptor = adaptor;
	}
	
	public AccountInfo getAccountInfo(){
		return accountInfo;
	}
	public void setAccountInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}
	
}
