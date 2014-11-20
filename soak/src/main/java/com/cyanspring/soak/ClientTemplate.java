package com.cyanspring.soak;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.client.ClientAdaptor;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.order.ClosePositionReplyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;

public class ClientTemplate extends ClientAdaptor {

	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(ServerReadyEvent.class, null);
		subscribeToEvent(UserLoginReplyEvent.class, null);
		subscribeToEvent(ClosePositionReplyEvent.class, null);
	}
	
	@Override
	public void processServerStatusEvent(String server, boolean up) {
		// TODO Auto-generated method stub

	}
	
	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/client.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		SoakTest soakTest = (SoakTest)context.getBean("clientTemplate");
		soakTest.init();
	}
	
}
