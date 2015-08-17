package com.cyanspring.soak;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.Clock;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.info.HistoricalPriceEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.server.event.ServerShutdownEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;

public class EventSender
{
	private static Logger log = LoggerFactory.getLogger(EventSender.class);
	private AsyncEvent event;
	private String eventClass = null;
	private boolean globalEvent = false;
	private String id = "EventSender-" + IdGenerator.getInstance().getNextID();
	private String channel;
	private String nodeChannel;
	private String inbox;
	private HashMap<String, Boolean> servers = new HashMap<String, Boolean>();
	private HashMap<String, Date> lastHeartBeats = new HashMap<String, Date>();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private int heartBeatInterval = 10000; // 10 seconds

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	protected ScheduleManager scheduleManager;

	@Autowired
	private SystemInfo systemInfo;

	protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor()
	{

		@Override
		public void subscribeToEvents()
		{
			EventSender.this.subscribeToEvents();
		}

		@Override
		public IAsyncEventManager getEventManager()
		{
			return eventManager;
		}

	};

	public void subscribeToEvent(Class<? extends AsyncEvent> clazz, String key)
	{
		eventProcessor.subscribeToEvent(clazz, key);
	}

	public void subscribeToEvents()
	{
		subscribeToEvent(ServerHeartBeatEvent.class, null);
		subscribeToEvent(NodeInfoEvent.class, null);
		subscribeToEvent(AsyncTimerEvent.class, null);
		subscribeToEvent(ServerReadyEvent.class, null);
	}

	public void processServerStatusEvent(String server, boolean up)
	{
	}

	public void processNodeInfoEvent(NodeInfoEvent event)
	{
		log.debug("Received NodeInfoEvent: " + event.getInbox());
		if (event.getServer())
		{
			Boolean serverIsUp = servers.get(event.getInbox());
			if (serverIsUp != null && serverIsUp)
			{
				log.error("ignore since server " + event.getInbox()
						+ " is still up");
				return;
			}
			servers.put(event.getInbox(), true);
			lastHeartBeats.put(event.getInbox(), Clock.getInstance().now());
		}
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event)
	{
		if (event == timerEvent)
		{
			for (Entry<String, Date> entry : lastHeartBeats.entrySet())
			{
				if (TimeUtil.getTimePass(entry.getValue()) > heartBeatInterval)
				{
					boolean serverStatus = servers.get(entry.getKey());
					if (serverStatus)
					{ // if it is up
						log.debug("Sending server down event: "
								+ entry.getKey());
						servers.put(entry.getKey(), false);
						processServerStatusEvent(entry.getKey(), false);
					}
				}
				else
				{ // server heart beat can go back up
					Boolean up = servers.get(entry.getKey());
					if (null == up || !up)
					{
						log.debug("Sending server up event: " + entry.getKey());
						servers.put(entry.getKey(), true);
						processServerStatusEvent(entry.getKey(), true);
					}
				}
			}
		}
	}

	public void processServerHeartBeatEvent(ServerHeartBeatEvent event)
	{
		lastHeartBeats.put(event.getSender(), Clock.getInstance().now());
	}

	public void init() throws Exception
	{
		// create node.info subscriber and publisher
		this.channel = systemInfo.getEnv() + "." + systemInfo.getCategory()
				+ "." + "channel";
		this.nodeChannel = systemInfo.getEnv() + "." + systemInfo.getCategory()
				+ "." + "node";
		InetAddress addr = InetAddress.getLocalHost();
		String hostName = addr.getHostName();
		String userName = System.getProperty("user.name");
		userName = userName == null ? "" : userName;
		this.inbox = hostName + "." + userName + "."
				+ IdGenerator.getInstance().getNextID();

		boolean ok = false;
		while (!ok)
		{
			try
			{
				eventManager.init(channel, inbox);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				log.debug("Retrying in 3 seconds...");
				ok = false;
				Thread.sleep(3000);
				continue;
			}
			ok = true;
		}
		eventManager.addEventChannel(this.channel);
		eventManager.addEventChannel(this.nodeChannel);

		eventProcessor.setHandler(this);
		eventProcessor.init();

		// schedule timer
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval,
				eventProcessor, timerEvent);

		NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, false, true,
				inbox, inbox);
		eventManager.publishRemoteEvent(nodeChannel, nodeInfo);
	}

	public void processServerReadyEvent(ServerReadyEvent event)
	{
		try
		{
			log.info("load event from: ./events/" + getEventClass() + ".xml");
			this.event = (AsyncEvent) XMLUtils.eventFromXML("./events/"
					+ getEventClass() + ".xml");
			log.info("load event : " + this.event.getClass().getName());
			if (isGlobalEvent())
			{
				event.setReceiver(null);
				sendGlobalEvent(this.event);
			}
			else
			{
				sendEvent(this.event);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			log.error(e.getMessage(), e);
		}
		finally
		{
			System.exit(0);
		}
	}

	public String getServer()
	{
		for (String server : servers.keySet())
			return server;
		return null;
	}

	public void sendEvent(AsyncEvent event)
	{
		String server = getServer();
		if (null == server)
		{
			log.error("Cannot get any server to send event");
			return;
		}

		if (event instanceof RemoteAsyncEvent)
		{
			RemoteAsyncEvent remoteEvent = (RemoteAsyncEvent) event;
			remoteEvent.setReceiver(server);
			try
			{
				eventManager.sendRemoteEvent(remoteEvent);
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
			}
		}
		else
		{
			eventManager.sendEvent(event);
		}
	}
	
	public void sendGlobalEvent(AsyncEvent event)
	{
		if (event instanceof RemoteAsyncEvent)
		{
			try
			{
				eventManager.sendGlobalEvent((RemoteAsyncEvent) event);
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	public AsyncEvent getEvent()
	{
		return event;
	}

	public void setEvent(AsyncEvent event)
	{
		this.event = event;
	}

	public String getEventClass()
	{
		return eventClass;
	}

	public void setEventClass(String eventClass)
	{
		this.eventClass = eventClass;
	}

	public AsyncEventProcessor getEventProcessor()
	{
		return this.eventProcessor;
	}

	public static void main(String[] args) throws Exception
	{
		String logconf = "conf/evts_log4j.xml";
		String configFile = "conf/eventSender.xml";
		if (args.length != 2)
		{
			throw new Exception(
					"Recommended arguments \'log4jConfig\' , \'EventSenderConfog\'");
		}
		logconf = args[0];
		configFile = args[1];
		DOMConfigurator.configure(logconf);
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		EventSender bean = (EventSender) context.getBean("sender");
		if (bean.getEventClass() == null)
		{
			throw new Exception(
					"Uninitial eventClass, please add in eventSender.xml");
		}
		bean.init();
	}

	public boolean isGlobalEvent()
	{
		return globalEvent;
	}

	public void setGlobalEvent(boolean globalEvent)
	{
		this.globalEvent = globalEvent;
	}

}
