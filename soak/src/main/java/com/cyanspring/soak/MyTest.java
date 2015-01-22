package com.cyanspring.soak;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;

public class MyTest extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(ClientAdaptor.class);
	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(ServerReadyEvent.class, null);
		subscribeToEvent(UserLoginReplyEvent.class, null);
		subscribeToEvent(ChangeAccountSettingReplyEvent.class, null);
	}
	
	public void processServerReadyEvent(ServerReadyEvent event) {
		AccountSetting setting = new AccountSetting("test10-FX");
		setting.setStopLossValue(2.0);
		ChangeAccountSettingRequestEvent request = new ChangeAccountSettingRequestEvent(null, null, setting);
		sendEvent(request);
	}
	
	@Override
	public void processServerStatusEvent(String server, boolean up) {
	}
	
	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		
	}
	
	public void processChangeAccountSettingReplyEvent(ChangeAccountSettingReplyEvent event) {
		log.info("ChangeAccountSettingReplyEvent: " + event.getAccountSetting() + ", " + event.isOk() + ", " + event.getMessage());
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		String configFile = "conf/client.xml";
		if(args.length>0)
			configFile = args[0];
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		MyTest bean = (MyTest)context.getBean("myTest");
		bean.init();
	}

}
