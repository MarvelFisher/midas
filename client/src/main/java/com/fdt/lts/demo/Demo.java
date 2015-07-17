package com.fdt.lts.demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.apievent.obj.Quote;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.lts.client.LtsApi;
import com.fdt.lts.client.TradeAdaptor;

public class Demo{
	public static String host ="";	
	public static int port =  52368;
	public static String user = "";
	public static String pwd = "";
	

	public static void main(String[] args) {
		DOMConfigurator.configure("conf/apilog4j.xml");
		LtsApi api = new LtsApi(host, port);				
		
		ArrayList<String> symbolLst = new ArrayList<String>();
		symbolLst.add("USDJPY");
		symbolLst.add("AUDUSD");
		
		TradeAdaptor adaptor;		
		Order buyOrder = getBuyOrder("USDJPY",20);
		adaptor = getBuyTradeAdaptor(buyOrder);
		
		api.start(user, pwd, symbolLst, adaptor);	

		
	}
	
	public static Order getBuyOrder(String symbol,double price){

		Order order = new Order();
		order.setSymbol(symbol);
		order.setSide(OrderSide.Buy);
		order.setPrice(price);
		order.setQuantity(1000);
		order.setType(OrderType.Limit);
		return order;
	}
	
	public static TradeAdaptor getBuyTradeAdaptor(final Order order){
		
		TradeAdaptor adaptor = new TradeAdaptor() {
			public Order buyOrder = order;
			public boolean isCreatedOrder = false;
			public boolean isAmendOrder = false;
			public boolean isCancelOrder =false;
			public int orderLimit = 1;
			public int orderCount = 0;
			
			
			public List getOrders(){
				List <Order>list = new ArrayList<Order>();
				Map map = this.getOrderMap();					
				Iterator ite  = map.keySet().iterator();
				while(ite.hasNext()){
					String key  =(String) ite.next();
					Order order = (Order) map.get(key);
					list.add(order);
				}
				return list;
			}
			
			@Override
			public void onStart() {
				System.out.println("Init Buy Trading...");
			}

			@Override
			public void onAccountUpdate() {

			}

			@Override
			public void onQuote(Quote quote) {
				List <Order>orders = getOrders();
				//AUDUSD
				if(quote.getSymbol().equals(buyOrder.getSymbol())){
					System.out.println("> quote symbol:"+quote.getSymbol()+" quote bid:"+quote.getBid()+" buyOrder:"+buyOrder.getPrice()+" order count:"+orderCount);
					
					//buy order
					if(orderLimit > orderCount){
						System.out.println("send Buy Order- price "+buyOrder.getPrice());
						//create new order
						newOrder(buyOrder);
						orderCount++;
					
					//amend order
					}else if(isCreatedOrder && !isAmendOrder){
						for(Order order:orders){
							if(order.getStatus().equals("Running")){
								System.out.println(" >>amend order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getStatus());
								order.setPrice(order.getPrice()+1);
								amendOrder(order);
							}
						}				
					}
					//cancel order	
					else if(isAmendOrder && !isCancelOrder){
						
						for(Order order:orders){
							if(order.getStatus().equals("Running")){
								System.out.println(" >>cancel order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getStatus());
								cancelOrder(order);
							}
						}
					}
					
					
				}
			}

			@Override
			public void onNewOrderReply(Order order) {
				System.out.println("Create Order Completed");
				isCreatedOrder =true;
			}

			@Override
			public void onAmendOrderReply(Order order) {
				System.out.println("Amend Order Completed");
				isAmendOrder = true;
			}

			@Override
			public void onCancelOrderReply(Order order) {
				System.out.println("Cancel Order Completed");
				isCancelOrder =true;
							
			}

			@Override
			public void onOrderUpdate(Order order) {
			}

			@Override
			public void onError(int code, String msg) {
				System.out.println("> Error, code: " + code + ", msg: " + msg);				
			}
			
			
		};
		return adaptor;
	}
}
