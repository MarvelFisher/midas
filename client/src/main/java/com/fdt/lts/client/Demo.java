package com.fdt.lts.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.fdt.lts.client.obj.Quote;

public class Demo{
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/apilog4j.xml");
		String configFile = "conf/api.xml";
		if (args.length > 0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		LtsApi adaptor = (LtsApi) context.getBean("apiAdaptor");
		ArrayList<String> symbolLst = new ArrayList<String>();
		symbolLst.add("USDJPY");
		symbolLst.add("AUDUSD");

		TradeAdaptor act = new TradeAdaptor() {
			@Override
			public void onQuote(Quote quote) {

			}

			@Override
			public void onTradeOrderReply() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAmendOrderReply() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCancelOrderReply() {
				// TODO Auto-generated method stub
				
			}

		};

		adaptor.init(symbolLst, act);
	}	
}
