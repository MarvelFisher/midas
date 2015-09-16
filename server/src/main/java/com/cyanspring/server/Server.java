/*******************************************************************************

 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import webcurve.client.MarketReplay;
import webcurve.exchange.Exchange;
import webcurve.fix.ExchangeFixGateway;
import webcurve.ui.ExchangeJFrame;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PositionPeakPrice;
import com.cyanspring.common.account.User;
import com.cyanspring.common.business.CoinControl;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.system.DuplicateSystemIdEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.TickField;
import com.cyanspring.common.server.event.DownStreamReadyEvent;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.server.event.ServerShutdownEvent;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.account.AccountPositionManager;
import com.cyanspring.server.account.CoinManager;
import com.cyanspring.server.account.TrailingStopManager;
import com.cyanspring.server.persistence.PersistenceManager;

public class Server implements ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(Server.class);

	@Autowired
	private SystemInfo systemInfo;

	@Autowired(required = false)
	@Qualifier("globalMQInfo")
	private SystemInfo globalMQInfo;

	@Autowired
	private OrderManager orderManager;

	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired(required = false)
	@Qualifier("globalEventManager")
	private IRemoteEventManager globalEventManager;

	@Autowired
	BusinessManager businessManager;

	@Autowired
	ScheduleManager scheduleManager;
	
	@Autowired(required = false)
	CoinManager coinManager;

	@Autowired(required = false)
	IRecoveryProcessor<Execution> executionRecoveryProcessor;

	@Autowired(required = false)
	IRecoveryProcessor<DataObject> strategyRecoveryProcessor;

	@Autowired(required = false)
	@Qualifier("userRecoveryProcessor")
	IRecoveryProcessor<User> userRecoveryProcessor;

	@Autowired(required = false)
	@Qualifier("userGroupRecoveryProcessor")
	IRecoveryProcessor<GroupManagement> userGroupRecoveryProcessor;
	
	@Autowired(required = false)
	@Qualifier("coinControlRecoveryProcessor")
	IRecoveryProcessor<CoinControl> coinControlRecoveryProcessor;

	@Autowired(required = false)
	IRecoveryProcessor<Account> accountRecoveryProcessor;

	@Autowired(required = false)
	IRecoveryProcessor<AccountSetting> accountSettingRecoveryProcessor;

	@Autowired(required = false)
	AccountPositionManager accountPositionManager;

	@Autowired(required = false)
	PositionRecoveryProcessor positionRecoveryProcessor;

	@Autowired(required = false)
	TrailingStopManager trailingStopManager;

	@Autowired
	private ApplicationContext applicationContext;

	private List<IPlugin> plugins;
	private boolean simulatorMode;
	private boolean headless;
	private ReadyList readyList;
	private String inbox;
	private String uid;
	private String channel;
	private String nodeInfoChannel;
	private int heartBeatInterval = 3000; // 3000 miliseconds
	private String shutdownTime;
	private AsyncTimerEvent shutdownEvent = new AsyncTimerEvent();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private ServerHeartBeatEvent heartBeat = new ServerHeartBeatEvent(null,
			null);
	private Map<String, Boolean> readyMap = new HashMap<String, Boolean>();
	private boolean serverReady;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(NodeInfoEvent.class, null);
			subscribeToEvent(DuplicateSystemIdEvent.class, null);
			subscribeToEvent(DownStreamReadyEvent.class, null);
			subscribeToEvent(MarketDataReadyEvent.class, null);
			subscribeToEvent(RefDataEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}

	};

	public void processNodeInfoEvent(NodeInfoEvent event) throws Exception {
		if (event.getFirstTime() && !event.getUid().equals(Server.this.uid)) { // not
																				// my
																				// own
																				// message
			// check duplicate system id
			if (event.getServer() && event.getInbox().equals(Server.this.inbox)) {
				log.error("Duplicated system id detected: " + event.getSender());
				DuplicateSystemIdEvent de = new DuplicateSystemIdEvent(null,
						null, event.getUid());
				de.setSender(Server.this.uid);
				eventManager.publishRemoteEvent(nodeInfoChannel, de);
			} else {
				// publish my node info
				NodeInfoEvent myInfo = new NodeInfoEvent(null, null, true,
						false, Server.this.inbox, Server.this.uid);
				eventManager.publishRemoteEvent(nodeInfoChannel, myInfo);
				log.info("Replied my nodeInfo");
			}
			if (!event.getServer() && readyList.allUp()) {
				try {
					eventManager.publishRemoteEvent(channel,
							new ServerReadyEvent(true));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	public void processDuplicateSystemIdEvent(DuplicateSystemIdEvent event) {
		if (event.getUid().equals(Server.this.uid)) {
			log.error("System id duplicated: " + systemInfo.getId());
			log.error("Fatal error, existing system");
			System.exit(1);
		}
	}

	public void processDownStreamReadyEvent(DownStreamReadyEvent event) {
		log.info("Down stream is ready: " + event.isReady());
		readyList.update("DownStream", event.isReady());
	}

	public void processMarketDataReadyEvent(MarketDataReadyEvent event) {
		log.info("Market data is ready: " + event.isReady());
		readyList.update("MarketData", event.isReady());
	}
	
	public void processRefDataEvent(RefDataEvent event) {
		log.info("RefData is ready: " + event.isOk());
		readyList.update("RefData", event.isOk());
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) throws Exception {
		if (event == timerEvent) {
			eventManager.publishRemoteEvent(nodeInfoChannel, heartBeat);
		} else if (event == shutdownEvent) {
			log.info("System hits end time, shutting down...");
			System.exit(0);
		}
	}

	class ReadyList {
		Map<String, Boolean> map = new HashMap<String, Boolean>();

		ReadyList(Map<String, Boolean> map) {
			this.map = map;
			// map.put("DownStream", false);
			// map.put("MarketData", false);
			// map.put("Recovery", false);
		}

		synchronized void update(String key, boolean value) {
			if (!map.containsKey(key))
				return;
			map.put(key, value);
			boolean now = allUp();
			if (!serverReady && now) {
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
			for (Entry<String, Boolean> entry : map.entrySet()) {
				if (!entry.getValue())
					return false;
			}
			return true;
		}
	}

	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	private Server() {
	}

	private void recover() {
		if (null != userRecoveryProcessor) {
			List<User> list = userRecoveryProcessor.recover();
			log.info("Users loaded: " + list.size());
			accountPositionManager.injectUsers(list);
		}

		if (null != userGroupRecoveryProcessor) {
			List<GroupManagement> list = userGroupRecoveryProcessor.recover();
			log.info("User Groups loaded:" + list.size());
			accountPositionManager.injectGroups(list);
		}

		if (null != accountRecoveryProcessor) {
			List<Account> list = accountRecoveryProcessor.recover();
			log.info("Accounts loaded: " + list.size());
			accountPositionManager.injectAccounts(list);
		}

		if (null != accountSettingRecoveryProcessor) {
			List<AccountSetting> list = accountSettingRecoveryProcessor
					.recover();
			log.info("AccountSettings loaded: " + list.size());
			accountPositionManager.injectAccountSettings(list);
		}

		if (null != coinControlRecoveryProcessor && null != coinManager) {
			List<CoinControl> list = coinControlRecoveryProcessor
					.recover();
			log.info("CoinControl loaded: " + list.size());
			coinManager.injectCoinControls(list);
		}
		
		if (null != positionRecoveryProcessor) {
			List<OpenPosition> list1 = positionRecoveryProcessor
					.recoverOpenPositions();
			log.info("Open positions loaded: " + list1.size());
			List<ClosedPosition> list2 = positionRecoveryProcessor
					.recoverClosedPositions();
			log.info("Closed positions loaded: " + list2.size());
			accountPositionManager.injectPositions(list1, list2);
		}

		accountPositionManager.endAcountPositionRecovery();

		if (null != executionRecoveryProcessor) {
			List<Execution> executions = executionRecoveryProcessor.recover();
			log.info("Executions recovered: " + executions.size());
			orderManager.injectExecutions(executions);
			accountPositionManager.injectExecutions(executions);
		}

		if (null != trailingStopManager) {
			List<PositionPeakPrice> list = persistenceManager
					.recoverPositionPeakPrices();
			trailingStopManager.injectPositionPeakPrices(list);
		}
	}
	
	private void recoverStrategies() {
		if (null != strategyRecoveryProcessor) {
			List<DataObject> list = strategyRecoveryProcessor.recover();

			// setting all strategy to stopped state
			for (DataObject obj : list) {
				StrategyState state = obj.get(StrategyState.class,
						OrderField.STATE.value());
				if (state.equals(StrategyState.Terminated))
					continue;

				obj.put(OrderField.STATE.value(), StrategyState.Stopped);
			}
			orderManager.injectStrategies(list);
			businessManager.injectStrategies(list);
			log.info("Strategies recovered: " + list.size());
		}
	}

	private void registerShutdownTime() throws ParseException {
		if (null == shutdownTime)
			return;
		Date endTime = TimeUtil.parseTime("HH:mm:ss", shutdownTime);
		scheduleManager.scheduleTimerEvent(endTime, eventProcessor,
				shutdownEvent);
	}

	public void init() throws Exception {
		OrderField.validate();
		TickField.validate();
		QuoteExtDataField.validate();

		IdGenerator.getInstance().setPrefix(systemInfo.getId() + "-");

		// setting ready List
		readyList = new ReadyList(readyMap);

		// create node.info subscriber and publisher
		log.info("SystemInfo: " + systemInfo);
		this.channel = systemInfo.getEnv() + "." + systemInfo.getCategory()
				+ "." + "channel";
		this.nodeInfoChannel = systemInfo.getEnv() + "."
				+ systemInfo.getCategory() + "." + "node";
		InetAddress addr = InetAddress.getLocalHost();
		String hostName = addr.getHostName();
		this.inbox = systemInfo.getEnv() + "." + systemInfo.getCategory() + "."
				+ systemInfo.getId();
		IdGenerator.getInstance().setSystemId(this.inbox);
		this.uid = hostName + "." + IdGenerator.getInstance().getNextID();
		eventManager.init(channel, inbox);
		eventManager.addEventChannel(nodeInfoChannel);

		if (null != globalEventManager) {
			String globalChannel = globalMQInfo.getEnv() + "."
					+ globalMQInfo.getCategory() + "." + "channel";
			String globalInbox = globalMQInfo.getEnv() + "."
					+ globalMQInfo.getCategory() + "." + globalMQInfo.getId();
			globalEventManager.init(globalChannel, globalInbox);
			globalEventManager.addEventChannel(globalChannel);
		}

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("Server");

		// ScheduleManager initialization
		log.debug("ScheduleManager initialized");
		scheduleManager.init();

		// PersistenceManager initialization
		log.debug("PersistenceManager initialized");
		persistenceManager.init();

		// OrderManager initialization
		log.debug("OrderManager initialized");
		orderManager.init();

		// Business Manager initialization
		log.debug("BusinessManager initialized");
		businessManager.init();

		if (null != plugins) {
			for (IPlugin plugin : plugins) {
				plugin.init();
			}
		}

		// publish my node info
		NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, true, true,
				inbox, uid);
		// Set sender as uid. This is to cater the situation when
		// duplicate inbox happened, the other node can receive the
		// NodeInfoEvent and detect it.
		// For this reason, one should never use NodeInfoEvent.getSender() to
		// reply anything for this event
		nodeInfo.setSender(uid);

		eventManager.publishRemoteEvent(nodeInfoChannel, nodeInfo);
		log.info("Published my node info");

		// recovery
		recover();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// wait for downstream and refData ready before recover
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
					
					if (!readyList.map.get("DownStream")) {
						log.debug("waiting for down stream ready before recovery...");
						continue;
					}
					
					if (!readyList.map.get("RefData")) {
						log.debug("waiting for refData ready before recovery...");
						continue;
					}
					break;
				}

				try {
					recoverStrategies();
					readyList.update("Recovery", true);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					System.exit(-1);
				}
			}

		});
		thread.start();

		// start heart beat
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval,
				eventProcessor, timerEvent);
		registerShutdownTime();
		if (isSimulatorMode())
			runSim();

	}

	private void runSim() throws InterruptedException {
		// start exchange simulator
		final Exchange exchange = (Exchange) applicationContext
				.getBean("simExchange");
		MarketReplay mr = new MarketReplay(exchange, 0);
		mr.load("conf/sim/depth.txt");
		mr.run();

		// start fix gateway for exchange simulator
		ExchangeFixGateway fixGW;
		if (applicationContext.containsBean("simExchangeFixSettings")) {
			String simExchangeFixSettings = (String) applicationContext
					.getBean("simExchangeFixSettings");
			fixGW = new ExchangeFixGateway(exchange);
			if (!fixGW.open(simExchangeFixSettings)) {
				log.error("Cant open FIX gateway for simulator");
			}
		}

		if (!isHeadless()) {
			ExchangeJFrame frame = new ExchangeJFrame(exchange);
			class RunFrame implements Runnable {
				ExchangeJFrame frame;

				public RunFrame(ExchangeJFrame frame) {
					this.frame = frame;
				}

				@Override
				public void run() {
					frame.setVisible(true);
				}

			}
			java.awt.EventQueue.invokeLater(new RunFrame(frame));

			synchronized (frame) {
				frame.wait();
			}
		} else {
			synchronized (this) {
				this.wait();
			}
		}
		shutdown();
	}

	public void uninit() {
		log.info("uninitialising server");
		if (null != plugins) {
			// uninit in reverse order
			for (int i = plugins.size(); i > 0; i--) {
				plugins.get(i - 1).uninit();
			}
		}
		scheduleManager.uninit();
		eventProcessor.uninit();
		businessManager.uninit();
		orderManager.uninit();
		persistenceManager.uninit();
		eventManager.uninit();

	}

	public void shutdown() {
		log.debug("");
		log.debug(">>>> CLOSING DOWN SERVER, PLEASE WAIT FOR ALL COMPONENTS CLEAN SHUTDOWN <<<");
		log.debug("");
		// stop heart beat
		scheduleManager.cancelTimerEvent(timerEvent);
		eventManager.sendEvent(new ServerShutdownEvent());
		try { // give it 2 seconds for an opportunity of clean shutdown
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		uninit();
		System.exit(0);
	}

	public String getInbox() {
		return inbox;
	}

	public List<IPlugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<IPlugin> plugins) {
		this.plugins = plugins;
	}

	public int getHeartBeatInterval() {
		return heartBeatInterval;
	}

	public void setHeartBeatInterval(int heartBeatInterval) {
		this.heartBeatInterval = heartBeatInterval;
	}

	public boolean isSimulatorMode() {
		return simulatorMode;
	}

	public void setSimulatorMode(boolean simulatorMode) {
		this.simulatorMode = simulatorMode;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getShutdownTime() {
		return shutdownTime;
	}

	public void setShutdownTime(String shutdownTime) {
		this.shutdownTime = shutdownTime;
	}

	public boolean isHeadless() {
		return headless;
	}

	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	public Map<String, Boolean> getReadyMap() {
		return readyMap;
	}

	public void setReadyMap(Map<String, Boolean> readyMap) {
		this.readyMap = readyMap;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String configFile = "conf/lfx_server.xml";
		String logConfigFile = "conf/common/log4j.xml";
		if (args.length == 1) {
			configFile = args[0];
		} else if (args.length == 2) {
			configFile = args[0];
			logConfigFile = args[1];
		}
		DOMConfigurator.configure(logConfigFile);
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		Server server = (Server) context.getBean("server");
		server.init();
	}

}
