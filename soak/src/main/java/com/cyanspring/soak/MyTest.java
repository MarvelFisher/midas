package com.cyanspring.soak;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.MaxOrderQtyAllowedReplyEvent;
import com.cyanspring.common.event.account.MaxOrderQtyAllowedRequestEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;

public class MyTest extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(ClientAdaptor.class);
	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(ServerReadyEvent.class, null);
		subscribeToEvent(UserLoginReplyEvent.class, null);
		subscribeToEvent(ChangeAccountSettingReplyEvent.class, null);
		subscribeToEvent(MaxOrderQtyAllowedReplyEvent.class, null);
	}
	
	public void processServerReadyEvent(ServerReadyEvent event) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					log.info("=== Sending requests ===");
					MaxOrderQtyAllowedRequestEvent request;
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "AUDUSD", OrderSide.Buy, OrderType.Limit, 10.0);
//					sendEvent(request);
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "AUDUSD", OrderSide.Sell, OrderType.Limit, 10.0);
//					sendEvent(request);
//
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "USDJPY", OrderSide.Buy, OrderType.Limit, 10.0);
//					sendEvent(request);
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "USDJPY", OrderSide.Sell, OrderType.Limit, 10.0);
//					sendEvent(request);
//
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "EURJPY", OrderSide.Buy, OrderType.Limit, 10.0);
//					sendEvent(request);
//					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
//							"test1-FX", "EURJPY", OrderSide.Sell, OrderType.Limit, 10.0);
//					sendEvent(request);
					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
							"test1-FC", "IF1512.CF", OrderSide.Buy, OrderType.Limit, 3500);
					sendEvent(request);
					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
							"test1-FC", "IF1512.CF", OrderSide.Sell, OrderType.Limit, 3500);
					sendEvent(request);
					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
							"test1-FC", "IF1512.CF", OrderSide.Buy, OrderType.Market, 3500);
					sendEvent(request);
					request = new MaxOrderQtyAllowedRequestEvent("Mytest", getServer(), "txId1",
							"test1-FC", "IF1512.CF", OrderSide.Sell, OrderType.Market, 3500);
					sendEvent(request);
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		
		thread.start();
	}
	
	@Override
	public void processServerStatusEvent(String server, boolean up) {
	}
	
	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		
	}
	
	public void processMaxOrderQtyAllowedReplyEvent(MaxOrderQtyAllowedReplyEvent event) {
		log.info("Received: " + event);
		
	}
	public void processChangeAccountSettingReplyEvent(ChangeAccountSettingReplyEvent event) {
		log.info("ChangeAccountSettingReplyEvent: " + event.getAccountSetting() + ", " + event.isOk() + ", " + event.getMessage());
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/myclient.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		MyTest bean = (MyTest)context.getBean("myTest");
		bean.init();
	}

}
