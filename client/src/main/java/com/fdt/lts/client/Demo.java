package com.fdt.lts.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.fdt.lts.client.obj.AccountInfo.OpenPosition;
import com.fdt.lts.client.obj.Order;
import com.fdt.lts.client.obj.QuoteData;

public class Demo{
	public static void main(String[] args) throws Exception {	
		String host = "";
		int port = 0;
		// start server
		LtsApi adaptor = new LtsApi(host, port);				
	
		ArrayList<String> symbolLst = new ArrayList<String>();
		symbolLst.add("USDJPY");
		symbolLst.add("AUDUSD");

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

		String user = "test";
		String pwd = "xxx";
		adaptor.start(user, pwd, symbolLst, trade);
	}	
}
