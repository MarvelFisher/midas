package com.cyanspring.info;

import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncEventProcessor;

public class InfoServer 
{
	private static final Logger log = LoggerFactory
			.getLogger(InfoServer.class);
	
	
	@Autowired
	private SystemInfo systemInfo;
	
	@Autowired
	private SystemInfo systemInfoMD;

	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	private IRemoteEventManager eventManagerMD;
	
	@Autowired
	private ScheduleManager scheduleManager;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private List<IPlugin> plugins;
	
	public void init() throws Exception {
		// create eventManager
		log.info("SystemInfo: " + systemInfo);
		String channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "channel"; 
		String nodeInfoChannel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "node";
		String inbox = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getId();
		IdGenerator.getInstance().setSystemId(inbox);
		eventManager.init(channel, inbox);
		eventManager.addEventChannel(nodeInfoChannel);
		
		// create MD eventManager
		log.info("SystemInfo: " + systemInfoMD);
		channel = systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + "channel"; 
		nodeInfoChannel = systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + "node";
		IdGenerator.getInstance().setSystemId(inbox);
		eventManagerMD.init(channel, inbox);
		eventManagerMD.addEventChannel(channel); //receiver channel
		eventManagerMD.addEventChannel(nodeInfoChannel);

		// ScheduleManager initialization
		log.debug("ScheduleManager initialized");
		scheduleManager.init();
		
		if(null != plugins) {
			for(IPlugin plugin: plugins) {
				plugin.init();
			}
		}

	}
	
	public void processQuoteEvent(QuoteEvent event) {
		log.debug("Quote: " + event.getQuote());
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		//log.debug("AsyncTimerEvent");
	}
	
	public void processNodeInfoEvent(NodeInfoEvent event) {
		log.debug("NodeInfoEvent: " + event.getSender());
	}
	
	//getters and setters
	public List<IPlugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<IPlugin> plugins) {
		this.plugins = plugins;
	}
	
	public static void main(String[] args) throws Exception {
		String configFile = "conf/info_server.xml";
		String logConfigFile = "conf/log4j.xml";
		if(args.length == 1) {
			configFile = args[0];
		} else if (args.length == 2) {
			configFile = args[0];
			logConfigFile = args[1];
		}
		DOMConfigurator.configure(logConfigFile);
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		
		// start server
		InfoServer server = (InfoServer)context.getBean("infoServer");
		server.init();
	}

}
