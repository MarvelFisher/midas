package com.fdt.lts.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.fdt.lts.client.obj.QuoteData;

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

		TradeAdaptor trade = new TradeAdaptor() {			
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onQuote(QuoteData quote) {

			}

			@Override
			public void onNewOrderReply() {
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

			@Override
			public void onError(int code, String msg) {
				// TODO Auto-generated method stub
				
			}
		};

		String user = "test";
		String pwd = "xxx";
		adaptor.init(user, pwd, symbolLst, trade);
	}	
}
