package com.fdt.lts.client;

import java.util.Map;

import com.fdt.lts.client.obj.AccountInfo;
import com.fdt.lts.client.obj.Order;

public abstract class TradeAdaptor implements IAdaptor {
	
	protected AccountInfo accountInfo; // need implement thread safe method
	protected Map<String, Order> orderMap;
	private ITrade adaptor;
	
	public void newOrder(Order order){
		adaptor.putNewOrder(order);
	}
	public void newStopOrder(Order order){
		adaptor.putStopOrder(order);
	}
	public void amendOrder(Order order){
		adaptor.putAmendOrder(order);
	}
	public void cancelOrder(Order order){
		adaptor.putCancelOrder(order);
	}
	public void terminate(){
		adaptor.terminate();
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
	public Map<String, Order> getOrderMap(){
		return orderMap;
	}
	public void setOrderMap(Map<String, Order> orderMap){
		this.orderMap = orderMap;
	}
}

