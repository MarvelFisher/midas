/*******************************************************************************
' * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.cstw.business;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.cstw.position.AllPositionManager;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.info.RateConverterReplyEvent;
import com.cyanspring.common.event.order.InitClientEvent;
import com.cyanspring.common.event.order.InitClientRequestEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotReplyEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.cachingmanager.cache.OrderCachingManager;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.localevent.ServerStatusLocalEvent;
import com.cyanspring.cstw.session.CSTWSession;
import com.cyanspring.cstw.session.TraderSession;
import com.cyanspring.cstw.ui.views.ServerStatusDisplay;

/**
 * This class is used to init different server. Get data form sever. Support api
 * to visit common service and bean.
 */
public final class Business {

	private static Logger log = LoggerFactory.getLogger(Business.class);
	private static Business instance; // Singleton

	private IRemoteEventManager eventManager;
	private EventListenerImpl listener;

	private CSTWBeanPool beanPool;
	private OrderCachingManager orderManager;
	private AllPositionManager allPositionManager;

	private HashMap<String, Boolean> servers;

	private ScheduleManager scheduleManager;
	private AsyncTimerEvent timerEvent;
	private int heartBeatInterval; // 5 seconds
	private HashMap<String, Date> lastHeartBeatMap;
	private Map<AlertType, Integer> alertColorConfig;

	private UserLoginAssist userLoginAssist;

	public static Business getInstance() {
		if (null == instance) {
			instance = new Business();
		}
		return instance;
	}

	/*
	 * singleton implementation
	 */
	private Business() {
		userLoginAssist = new UserLoginAssist();
		servers = new HashMap<String, Boolean>();
		listener = new EventListenerImpl();
		scheduleManager = new ScheduleManager();
		timerEvent = new AsyncTimerEvent();
		lastHeartBeatMap = new HashMap<String, Date>();
		heartBeatInterval = 10000;

	}

	public void init() throws Exception {
		Version ver = new Version();
		log.info(ver.getVersionDetails());
		log.info("Initializing business obj...");
		SystemInfo systemInfo = BeanHolder.getInstance().getSystemInfo();
		// create node.info subscriber and publisher
		CSTWSession.getInstance().setChannel(
				systemInfo.getEnv() + "." + systemInfo.getCategory() + "."
						+ "channel");
		CSTWSession.getInstance().setNodeInfoChannel(
				systemInfo.getEnv() + "." + systemInfo.getCategory() + "."
						+ "node");
		InetAddress addr = InetAddress.getLocalHost();
		String hostName = addr.getHostName();
		String userName = System.getProperty("user.name");
		if (userName == null) {
			userName = "";
		}
		CSTWSession.getInstance().setInbox(
				hostName + "." + userName + "."
						+ IdGenerator.getInstance().getNextID());
		BeanHolder beanHolder = BeanHolder.getInstance();
		if (beanHolder == null) {
			throw new Exception("BeanHolder is not yet initialised");
		}
		beanPool = new CSTWBeanPool(beanHolder);
		alertColorConfig = beanHolder.getAlertColorConfig();
		allPositionManager = beanHolder.getAllPositionManager();
		eventManager = beanHolder.getEventManager();
		boolean ok = false;
		while (!ok) {
			try {
				eventManager.init(CSTWSession.getInstance().getChannel(),
						CSTWSession.getInstance().getInbox());
			} catch (Exception e) {
				log.error(e.getMessage());
				log.debug("Retrying in 3 seconds...");
				ok = false;
				Thread.sleep(3000);
				continue;
			}
			ok = true;
		}

		eventManager.addEventChannel(CSTWSession.getInstance().getChannel());
		eventManager.addEventChannel(CSTWSession.getInstance()
				.getNodeInfoChannel());

		ServerStatusDisplay.getInstance().init();

		eventManager.subscribe(NodeInfoEvent.class, listener);
		eventManager.subscribe(InitClientEvent.class, listener);
		eventManager.subscribe(ServerHeartBeatEvent.class, listener);
		eventManager.subscribe(ServerReadyEvent.class, listener);
		eventManager
				.subscribe(AccountSettingSnapshotReplyEvent.class, listener);
		eventManager.subscribe(RateConverterReplyEvent.class, listener);
		eventManager.subscribe(AccountInstrumentSnapshotReplyEvent.class,
				listener);
		// schedule timer
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval, listener,
				timerEvent);
		orderManager = new OrderCachingManager();
		userLoginAssist.init(orderManager);
	}

	public void start() throws Exception {
		// publish my node info
		NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, false, true,
				CSTWSession.getInstance().getInbox(), CSTWSession.getInstance()
						.getInbox());
		eventManager.publishRemoteEvent(CSTWSession.getInstance()
				.getNodeInfoChannel(), nodeInfo);
		log.info("Published my node info");

	}

	class EventListenerImpl implements IAsyncEventListener {
		@Override
		public void onEvent(AsyncEvent event) {
			// log.info("Received message: " + message);
			if (event instanceof NodeInfoEvent) {
				NodeInfoEvent nodeInfo = (NodeInfoEvent) event;
				if (nodeInfo.getServer()) {
					log.info("NodeInfoEvent received: " + nodeInfo.getSender());
					Boolean serverIsUp = servers.get(nodeInfo.getInbox());
					if (serverIsUp != null && serverIsUp) {
						log.error("ignore since server " + nodeInfo.getInbox()
								+ " is still up");
						return;
					}
					servers.put(nodeInfo.getInbox(), true);
					lastHeartBeatMap.put(nodeInfo.getInbox(), Clock
							.getInstance().now());
				}
			} else if (event instanceof InitClientEvent) {
				log.debug("Received event: " + event);
				InitClientEvent initClientEvent = (InitClientEvent) event;
				TraderSession.getInstance().initByEvnet(initClientEvent);
			} else if (event instanceof AccountSettingSnapshotReplyEvent) {
				AccountSettingSnapshotReplyEvent evt = (AccountSettingSnapshotReplyEvent) event;
				processAccountSettingSnapshotReplyEvent(evt);
			} else if (event instanceof ServerReadyEvent) {
				log.info("ServerReadyEvent  received: "
						+ ((ServerReadyEvent) event).getSender());
				InitClientRequestEvent request = new InitClientRequestEvent(
						null, ((ServerReadyEvent) event).getSender());
				try {
					eventManager.sendRemoteEvent(request);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			} else if (event instanceof ServerHeartBeatEvent) {
				processServerHeartBeatEvent((ServerHeartBeatEvent) event);
			} else if (event instanceof AsyncTimerEvent) {
				processAsyncTimerEvent((AsyncTimerEvent) event);
			} else if (event instanceof AccountInstrumentSnapshotReplyEvent) {
				processAccountInstrumentSnapshotReplyEvent((AccountInstrumentSnapshotReplyEvent) event);
			} else if (event instanceof RateConverterReplyEvent) {
				RateConverterReplyEvent e = (RateConverterReplyEvent) event;
				beanPool.setRateConverter(e.getConverter());
			} else {
				log.error("I dont expect this event: " + event);
			}
		}

	}

	private void processServerHeartBeatEvent(ServerHeartBeatEvent event) {
		lastHeartBeatMap.put(event.getSender(), Clock.getInstance().now());
	}

	private void processAsyncTimerEvent(AsyncTimerEvent event) {
		for (Entry<String, Date> entry : lastHeartBeatMap.entrySet()) {
			if (TimeUtil.getTimePass(entry.getValue()) > heartBeatInterval) {
				log.debug("Sending server down event: " + entry.getKey());
				servers.put(entry.getKey(), false);
				eventManager.sendEvent(new ServerStatusLocalEvent(entry
						.getKey(), false));
			} else { // server heart beat can go back up
				Boolean up = servers.get(entry.getKey());
				if (null != up && !up) {
					log.debug("Sending server up event: " + entry.getKey());
					servers.put(entry.getKey(), true);
					eventManager.sendEvent(new ServerStatusLocalEvent(entry
							.getKey(), true));
				}
			}
		}
	}

	public int getHeartBeatInterval() {
		return heartBeatInterval;
	}

	public void stop() {
		log.info("Stopping business object...");
		try {
			scheduleManager.uninit();
			eventManager.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public OrderCachingManager getOrderManager() {
		return orderManager;
	}

	public ScheduleManager getScheduleManager() {
		return scheduleManager;
	}

	public Map<AlertType, Integer> getAlertColorConfig() {
		return alertColorConfig;
	}

	public String getFirstServer() {
		for (Entry<String, Boolean> entry : servers.entrySet()) {
			if (entry.getValue())
				return entry.getKey();
		}
		return null;
	}

	public boolean isFirstServerReady() {
		Boolean result = servers.get(getFirstServer());
		return result == null ? false : result;
	}

	private void processAccountSettingSnapshotReplyEvent(
			AccountSettingSnapshotReplyEvent event) {
		if (null != event.getAccountSetting()) {
			CSTWSession.getInstance().setAccountSetting(
					event.getAccountSetting());
		}
	}

	private void processAccountInstrumentSnapshotReplyEvent(
			AccountInstrumentSnapshotReplyEvent replyEvent) {
		InstrumentPoolKeeperManager.getInstance().init();
		InstrumentPoolKeeperManager.getInstance().setInstrumentPoolKeeper(
				replyEvent.getInstrumentPoolKeeper());
	}

	public AllPositionManager getAllPositionManager() {
		return allPositionManager;
	}

	public static IBusinessService getBusinessService() {
		return instance.beanPool;
	}

	public static CSTWBeanPool getCSTWBeanPool() {
		return instance.beanPool;
	}

}
