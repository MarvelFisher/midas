package com.cyanspring.info;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.cyanspring.common.event.system.DuplicateSystemIdEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.marketdata.MarketDataReceiver;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.event.AsyncEventProcessor;

public class InfoServer 
{
	private static final Logger log = LoggerFactory
			.getLogger(InfoServer.class);
	private ReadyList readyList;
	private String inbox;
	private String uid;
	private String channel;
	private String nodeInfoChannel;
	private int heartBeatInterval = 3000; // 3000 miliseconds
//	private String shutdownTime;
//	private AsyncTimerEvent shutdownEvent = new AsyncTimerEvent(); 
	private ServerHeartBeatEvent heartBeat = new ServerHeartBeatEvent(null, null);
	private Map<String, Boolean> readyMap = new HashMap<String, Boolean>();
	private boolean serverReady;
	
	
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
	
	@Autowired
	MarketDataReceiver mdReceiver;
	
	@Autowired
	private Boolean useLocalMdManager;
	
	@Autowired
	private Boolean useLocalMdReceiver;
	
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private List<IPlugin> plugins;
	
//	@Autowired
//	private CentralDbProcessor centralDbProcessor;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(NodeInfoEvent.class, null);
			subscribeToEvent(DuplicateSystemIdEvent.class, null);
//			subscribeToEvent(DownStreamReadyEvent.class, null);
//			subscribeToEvent(MarketDataReadyEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	
	public void processNodeInfoEvent(NodeInfoEvent event) throws Exception {
		log.debug("NodeInfoEvent: " + event.getSender());
		if(event.getFirstTime() && 
				!event.getUid().equals(InfoServer.this.uid)) { // not my own message
			//check duplicate system id
			if (event.getServer() && event.getInbox().equals(InfoServer.this.inbox)) {
				log.error("Duplicated system id detected: " + event.getSender());
				DuplicateSystemIdEvent de = 
					new DuplicateSystemIdEvent(null, null, event.getUid());
				de.setSender(InfoServer.this.uid);
				eventManager.publishRemoteEvent(nodeInfoChannel, de);
			} else {
				// publish my node info
				NodeInfoEvent myInfo = 
					new NodeInfoEvent(null, null, true, false, 
							InfoServer.this.inbox, InfoServer.this.uid);
					eventManager.publishRemoteEvent(nodeInfoChannel, myInfo);
				log.info("Replied my nodeInfo");
			}
			if(!event.getServer() && readyList.allUp()) {
				try {
					waitForCDbPReady();
					eventManager.publishRemoteEvent(channel, new ServerReadyEvent(true));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}	
	}
	public void processDuplicateSystemIdEvent(DuplicateSystemIdEvent event) {
		if(event.getUid().equals(InfoServer.this.uid)) {
			log.error("System id duplicated: " + systemInfo.getId());
			log.error("Fatal error, existing system");
			System.exit(1);
		}
	}
	
	public void init() throws Exception {
		
		IdGenerator.getInstance().setPrefix(systemInfo.getId()+"-");
		
		// setting ready List
		readyList = new ReadyList(readyMap);
		
		// create eventManager as server
		log.info("SystemInfo: " + systemInfo);
		this.channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "channel"; 
		this.nodeInfoChannel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "node";
		this.inbox = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + systemInfo.getId();
		IdGenerator.getInstance().setSystemId(inbox);
		
		InetAddress addr = InetAddress.getLocalHost();
		String hostName = addr.getHostName();
		this.uid = hostName + "." + IdGenerator.getInstance().getNextID();
		eventManager.init(channel, inbox);
		eventManager.addEventChannel(nodeInfoChannel);
		
		// create MD eventManager as client
		log.info("SystemInfo: " + systemInfoMD);		
		String channelMD = systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + "channel"; 
		String nodeInfoChannelMD = systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + "node";
		IdGenerator.getInstance().setSystemId(inbox);
		eventManagerMD.init(channelMD, inbox);
		eventManagerMD.addEventChannel(channelMD); //receiver channel
		eventManagerMD.addEventChannel(nodeInfoChannelMD);

		
		// publish my node info
		NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, true, true, inbox, uid);
		// Set sender as uid. This is to cater the situation when
		// duplicate inbox happened, the other node can receive the NodeInfoEvent and detect it.
		// For this reason, one should never use NodeInfoEvent.getSender() to reply anything for this event
		nodeInfo.setSender(uid); 
		
		eventManager.publishRemoteEvent(nodeInfoChannel, nodeInfo);
		log.info("Published my node info");

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("InfoServer");
		
		// ScheduleManager initialization
		log.debug("ScheduleManager initialized");
		scheduleManager.init();
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval, eventProcessor, timerEvent);
		
		if(null != plugins) {
			for(IPlugin plugin: plugins) {
				plugin.init();
			}
		}
		if (useLocalMdReceiver)
		{
			mdReceiver.setServerInfo(systemInfoMD.getEnv() + "." + systemInfoMD.getCategory() + "." + systemInfoMD.getId());
			mdReceiver.setEventManager(eventManagerMD);
			mdReceiver.init();
		}
	}
	
	class ReadyList {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		
		ReadyList(Map<String, Boolean> map) {
			this.map = map;
		}
		
		synchronized void update(String key, boolean value) {
			if(!map.containsKey(key))
				return;
			map.put(key, value);
			boolean now = allUp();
			if(!serverReady && now) {
				waitForCDbPReady();
				serverReady = true;
				log.info("Server is ready: " + now);
				ServerReadyEvent event = new ServerReadyEvent(now);
				eventManager.sendEvent(event);
				try {
					eventManager.publishRemoteEvent(channel, event);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		synchronized boolean isUp(String component) {
			return map.get(component);
		}
		
		synchronized boolean allUp() {
			for(Entry<String, Boolean> entry: map.entrySet()) {
				if(!entry.getValue())
					return false;
			}
			return true;
		}
	}
	
	
	public void processQuoteEvent(QuoteEvent event) {
		log.debug("Quote: " + event.getQuote());
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) throws Exception {
		if(event == timerEvent) {
			eventManager.publishRemoteEvent(nodeInfoChannel, heartBeat);
		}
		/*
		else if(event == shutdownEvent) {
			log.info("System hits end time, shutting down...");
			System.exit(0);
		}
		*/
	}
	
	public void waitForCDbPReady()
	{
		try 
		{
			while(CentralDbProcessor.isStartup)
			{
				Thread.sleep(1);
			}
		} 
		catch (InterruptedException e) 
		{
			log.error(e.getMessage(), e);
		}
	}
	
	
	//getters and setters
	public List<IPlugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<IPlugin> plugins) {
		this.plugins = plugins;
	}
	
	public static void main(String[] args) throws Exception {
		String configFile = "conf/info_fxserver.xml";
		String logConfigFile = "conf/info_slog4j.xml";
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
