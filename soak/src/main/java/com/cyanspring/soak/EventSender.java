package com.cyanspring.soak;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.info.HistoricalPriceEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;

public class EventSender extends ClientAdaptor {
	private static Logger log = LoggerFactory.getLogger(ClientAdaptor.class);
	private AsyncEvent event;
	private String eventClass = null;
	
	@Override
	public void subscribeToEvents() {
		super.subscribeToEvents();
		subscribeToEvent(ServerReadyEvent.class, null);
	}

	@Override
	public void processServerStatusEvent(String server, boolean up) {
	}
	
	@Override
	public void processNodeInfoEvent(NodeInfoEvent event) {
		super.processNodeInfoEvent(event);
	}
	
	public void processServerReadyEvent(ServerReadyEvent event) {
		try 
		{
			this.event = (AsyncEvent)XMLUtils.eventFromXML("events\\" + getEventClass() + ".xml");
			sendEvent(this.event);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} 
		catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			System.exit(0);
		}
	}
	
	public AsyncEvent getEvent() {
		return event;
	}

	public void setEvent(AsyncEvent event) {
		this.event = event;
	}

	public String getEventClass() {
		return eventClass;
	}

	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("conf/evts_log4j.xml");
		String configFile = "conf/eventSender.xml";
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		EventSender bean = (EventSender)context.getBean("sender");
		if (bean.getEventClass() == null)
		{
			throw new Exception("Uninitial eventClass, please add in eventSender.xml");
		}
		bean.init();
	}

}
