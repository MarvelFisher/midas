package com.fdt.lts.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.fdt.lts.client.obj.AccountInfo.OpenPosition;
import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.QuoteData;

public class Demo{
	private static final Logger log = LoggerFactory
			.getLogger(Demo.class);
	
	//server info
	public static String host ="10.0.0.51";	
	public static int port =  52368;//61616(ActiveMQ),52368(Socket)
	//account info
	public static String user = "jimmydev";
	public static String pwd = "12345678";
	
	/*
	static{	
		String path = Demo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.printf("path:%s",path);
		DOMConfigurator.configure(path+""+"log4j2.xml");
	}
	*/
	public static Order getBuyOrder(String symbol,double price){

		Order order = new Order();
		order.setSymbol(symbol);
		order.setSide(OrderSide.Buy);
		order.setPrice(price);
		order.setQuantity(1000);
		order.setType(OrderType.Limit);
		return order;
	}
	public static Order getSellOrder(String symbol,double price){

		Order order = new Order();
		order.setSymbol(symbol);
		order.setSide(OrderSide.Sell);
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
			public int orderLimit = 1;
			public int orderCount = 0;
			public double buyPrice = 80;
			public double amendPrice = 100;
			public double cancelPrice = 100;
			public double stopPrice = 200;
			
			
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
				log.info("Init Buy Trading...");
			}

			@Override
			public void onQuote(QuoteData quote) {
				List <Order>orders = getOrders();
				//AUDUSD
				if(quote.getSymbol().equals(buyOrder.getSymbol())){
					System.out.println("> quote symbol:"+quote.getSymbol()+" quote bid:"+quote.getBid()+" buyOrder:"+buyOrder.getPrice()+" order count:"+orderCount);
					
					//buy order
					if(buyOrder.getPrice() >= buyPrice && orderLimit > orderCount){
						System.out.println("send Buy Order- price "+buyOrder.getPrice());
						//create new order
						newOrder(buyOrder);
						orderCount++;
					
					//amend order
					}else if(quote.getBid() > amendPrice && isCreatedOrder && !isAmendOrder){
						for(Order order:orders){
							if(order.getState().equals("Running")){
								System.out.println(" >>amend order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getState());
								amendOrder(order);
							}
						}				
					}
					//cancel order	
					else if(quote.getBid() > cancelPrice && isAmendOrder){
						
						for(Order order:orders){
							if(order.getState().equals("Running")){
								System.out.println(" >>cancel order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getState());
								cancelOrder(buyOrder);
							}
						}
					}
					
					
				}
			}

			@Override
			public void onNewOrderReply(Order order) {
				buyOrder = order;
				System.out.println("Create Order Completed");
				isCreatedOrder =true;
			}

			@Override
			public void onAmendOrderReply(Order order) {
				buyOrder = order;
				System.out.println("Amend Order Completed");
				isAmendOrder = true;
			}

			@Override
			public void onCancelOrderReply(Order order) {
				buyOrder = order;
				System.out.println("Cancel Order Completed");
							
			}

			@Override
			public void onOrderUpdate(Order order) {
				buyOrder = order;
				//System.out.println("Update Order Completed");
			}

			@Override
			public void onError(int code, String msg) {
				System.out.println("> Error, code: " + code + ", msg: " + msg);				
			}
			
			
		};
		return adaptor;
		
	}
	
	public static TradeAdaptor getSellTradeAdaptor(final Order order){
		
		TradeAdaptor adaptor = new TradeAdaptor() {
			public Order sellOrder = order;
			public boolean isCreatedOrder =false;
			public boolean isAmendOrder = false;
			public int orderLimit = 1;
			public int orderCount = 0;
			public double sellPrice = 80;
			public double amendPrice = 120;
			public double cancelPrice = 120;
			public double stopPrice = 200;
			
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
				log.info("Init Sell Trading...");
			}

			@Override
			public void onQuote(QuoteData quote) {
				List <Order>orders = getOrders();
				//AUDUSD
				if(quote.getSymbol().equals(sellOrder.getSymbol())){
					System.out.println("> quote symbol:"+quote.getSymbol()+" quote bid:"+quote.getBid()+" sellOrder:"+sellOrder.getPrice()+" order count:"+orderCount);
					//sell order
					if(sellOrder.getPrice() >= sellPrice && orderLimit > orderCount){
						System.out.println("send sell Order- price "+sellOrder.getPrice());
						//create new order
						newOrder(sellOrder);
						orderCount++;
					
					}
					//amend order
					
					else if(quote.getAsk() > amendPrice  && isCreatedOrder && !isAmendOrder){

						for(Order order:orders){
							
							if(order.getState().equals("Running")){
								System.out.println(" >>amend order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getState());
								amendOrder(order);
							}
						}
					
					}
					
					//cancel order	
					else if(quote.getAsk() > cancelPrice && isAmendOrder){
						for(Order order:orders){
							if(order.getState().equals("Running")){
								System.out.println(" >>cancel order:"+order.getId()+" order price:"+order.getPrice()+" state:"+order.getState());
								cancelOrder(order);
							}
						}
						
					}
					
					
				}
			}

			@Override
			public void onNewOrderReply(Order order) {
				sellOrder = order;
				System.out.println("Create Order Completed");
				isCreatedOrder =true;
			}

			@Override
			public void onAmendOrderReply(Order order) {
				sellOrder = order;
				System.out.println("Amend Order Completed");
				isAmendOrder = true;
			}

			@Override
			public void onCancelOrderReply(Order order) {
				sellOrder = order;
				System.out.println("Cancel Order Completed");
							
			}

			@Override
			public void onOrderUpdate(Order order) {
				sellOrder = order;
				//System.out.println("Update Order Completed");
			}

			@Override
			public void onError(int code, String msg) {
				System.out.println("> Error, code: " + code + ", msg: " + msg);				

			}
			
			
		};
		return adaptor;
		
	}
	public static TradeAdaptor getMultiOrderAdaptor(){
		TradeAdaptor trade = new TradeAdaptor() {
			String opSymbol = "AUDUSD";
			double buyPrice = 80;
			double sellPrice = 90;
			boolean buyFlag;
			boolean sellFlag;
			@Override
			public void onStart() {				
				System.out.println("> Initializing...");
				buyFlag = true;
				
			}
			
			@Override
			public void onQuote(QuoteData quote) {
				System.out.println(quote.getAsk());
				/*
				if(quote.getSymbol().equals(opSymbol)){
					if(quote.getBid() <= buyPrice && buyFlag){
						Order order = new Order();
						order.setSymbol(opSymbol);
						order.setSide(OrderSide.Buy);
						order.setPrice(buyPrice);
						order.setQuantity(10000);
						order.setType(OrderType.Limit);
						newOrder(order);
						buyFlag = false;
					}
					if(quote.getAsk() >= sellPrice && sellFlag){
						Order order = new Order();
						order.setSymbol(opSymbol);
						order.setSide(OrderSide.Sell);
						order.setPrice(sellPrice);
						order.setQuantity(10000);
						order.setType(OrderType.Limit);
						newOrder(order);
						sellFlag = false;
					}
				}
				*/
			}

			@Override
			public void onNewOrderReply(Order order) {				
				if(order.getSymbol().equals(opSymbol)){
					List<OpenPosition> list = accountInfo.getOpenPositions(order.getSymbol());
					for(OpenPosition position : list){
						if(position.getId().equals(order.getId())){
							if(order.getSide().equals(OrderSide.Buy))
								sellFlag = true;
							else if(order.getSide().equals(OrderSide.Sell))
								buyFlag = true;
						}
					}
				}
			}

			@Override
			public void onAmendOrderReply(Order order) {				
				
			}

			@Override
			public void onCancelOrderReply(Order order) {
				
			}
			
			@Override
			public void onOrderUpdate(Order order) {
				if(order.getSymbol().equals(opSymbol)){
					List<OpenPosition> list = accountInfo.getOpenPositions(order.getSymbol());
					for(OpenPosition position : list){
						if(position.getId().equals(order.getId())){
							if(order.getSide().equals(OrderSide.Buy))
								sellFlag = true;
							else if(order.getSide().equals(OrderSide.Sell))
								buyFlag = true;
						}
					}
				}				
			}

			@Override
			public void onError(int code, String msg) {
				System.out.println("> Error, code: " + code + ", msg: " + msg);				
			}
		};
		return trade;
	}
	
	public static void main(String[] args) throws Exception {

		// start server
		
		LtsApi api = new LtsApi(host, port);				
	
		ArrayList<String> symbolLst = new ArrayList<String>();
		symbolLst.add("USDJPY");
		symbolLst.add("AUDUSD");
		
		
		TradeAdaptor adaptor;
		//Buy order
		Order buyOrder = getBuyOrder("USDJPY",100);		
		//adaptor = getBuyTradeAdaptor(buyOrder);
		
		//Sell order
		Order sellOrder = getSellOrder("USDJPY",150);		
		adaptor = getSellTradeAdaptor(sellOrder);
		
		
	
		//Multi Order Control
		//adaptor = getMultiOrderAdaptor();
		//api.start(user, pwd, symbolLst, adaptor);	
		
		api.start(user, pwd, symbolLst, adaptor);	
			
		
	}	
}
