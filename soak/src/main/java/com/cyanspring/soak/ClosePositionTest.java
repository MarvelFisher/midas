package com.cyanspring.soak;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.client.ClientAdaptor;
import com.cyanspring.common.account.PositionCloseReason;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;

public class ClosePositionTest extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(ClosePositionTest.class);
	private String user = "test3";
	private String account = "test3";
	private String symbol = "AUDUSD";
	private String tobeAmended;

	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(ServerReadyEvent.class, null);
		subscribeToEvent(ClosePositionReplyEvent.class, null);
		subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
		subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
		subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
	}
	
	@Override
	public void processServerStatusEvent(String server, boolean up) {
	}
	
	public void processServerReadyEvent(ServerReadyEvent event) {
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				
				try {
					action1();
					Thread.sleep(1000);
					action2();
					Thread.sleep(1000);
					action3();
					Thread.sleep(1000);
					action4();
				} catch (InterruptedException e) {
				}
			}
			
		});
		thread.start();
	}
	

	private void action1() {
		sendEvent(getEnterOrderEvent(0.9));
		sendEvent(getEnterOrderEvent(0.9));
		sendEvent(getEnterOrderEvent(0.7));
	}
	
	private void action2() {
		sendEvent(new ClosePositionRequestEvent(account, null, account, 
				symbol, PositionCloseReason.ManualClose, IdGenerator.getInstance().getNextID()));
		//this should get rejected
		sendEvent(getEnterOrderEvent(0.9));
		
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.PRICE.value(), 0.8);
		fields.put(OrderField.QUANTITY.value(), 3000.0); // note: you must put xxx.0 to tell java this is a double type here!!
		log.debug("Before sending amend: " + tobeAmended);
		AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null, 
				tobeAmended, fields, IdGenerator.getInstance().getNextID());
		//this should get rejected
		sendEvent(amendEvent);

		//this should be rejected
		sendEvent(new ClosePositionRequestEvent(account, null, account, 
				symbol, PositionCloseReason.ManualClose, IdGenerator.getInstance().getNextID()));

		CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null, 
				tobeAmended, IdGenerator.getInstance().getNextID());
		//this should be ok
		sendEvent(cancelEvent);
		
	}
	
	private void action3() {
		//this should be accepted
		sendEvent(getEnterOrderEvent(0.7));
	}

	private void action4() {
		//this should be accepted
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.PRICE.value(), 0.8);
		fields.put(OrderField.QUANTITY.value(), 3000.0); // note: you must put xxx.0 to tell java this is a double type here!!
		log.debug("Before sending amend: " + tobeAmended);
		AmendParentOrderEvent amendEvent = new AmendParentOrderEvent(getId(), null, 
				tobeAmended, fields, IdGenerator.getInstance().getNextID());
		sendEvent(amendEvent);
	}

	public void processClosePositionReplyEvent(ClosePositionReplyEvent event) {
		log.debug("ClosePositionReplyEvent: " + event.isOk() + ", " + event.getMessage());
	}

	EnterParentOrderEvent getEnterOrderEvent(double price) {
		// SDMA 
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), symbol);
		fields.put(OrderField.SIDE.value(), OrderSide.Buy);
		fields.put(OrderField.TYPE.value(), OrderType.Limit);
		fields.put(OrderField.PRICE.value(), price);
		//fields.put(OrderField.PRICE.value(), 0.87980);
		fields.put(OrderField.QUANTITY.value(), 2000.0); // note: you must put xxx.0 to tell java this is a double type here!!
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		fields.put(OrderField.USER.value(), user);
		fields.put(OrderField.ACCOUNT.value(), account);
		enterOrderEvent = new EnterParentOrderEvent(getId(), null, fields, IdGenerator.getInstance().getNextID(), false);
		return enterOrderEvent;
	}
	
	public void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		if(!event.isOk()) {
			log.debug("Received EnterParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		} else {
			log.debug("Received EnterParentOrderReplyEvent(ACK): " + event.isOk() + ", order id: " + event.getOrder().getId());
			if(PriceUtils.Equal(event.getOrder().getPrice(), 0.7)) {
				tobeAmended = event.getOrder().getId();
				log.debug("Setting id to be amended: " + tobeAmended);
				action2();
			}
		}
	}
	
	public void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
		} else {
			log.debug("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		if(event.isOk()) {
			log.debug("Received CancelParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
		} else {
			log.debug("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/client.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		ClosePositionTest closePositionTest = (ClosePositionTest)context.getBean("closePositionTest");
		closePositionTest.init();
	}

}
