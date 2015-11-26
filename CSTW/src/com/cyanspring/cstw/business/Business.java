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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.cstw.position.AllPositionManager;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.CSTWUserLoginReplyEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.info.RateConverterReplyEvent;
import com.cyanspring.common.event.info.RateConverterRequestEvent;
import com.cyanspring.common.event.order.InitClientEvent;
import com.cyanspring.common.event.order.InitClientRequestEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotReplyEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotRequestEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleOrderStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.marketsession.DefaultStartEndTime;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.cachingmanager.cache.OrderCachingManager;
import com.cyanspring.cstw.cachingmanager.quote.QuoteCachingManager;
import com.cyanspring.cstw.cachingmanager.riskcontrol.FrontRCOrderCachingManager;
import com.cyanspring.cstw.cachingmanager.riskcontrol.FrontRCPositionCachingManager;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.BackRCOpenPositionEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.FrontRCOpenPositionEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.RCIndividualEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.RCInstrumentStatisticsEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.RCInstrumentSummaryEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.RCOrderEventController;
import com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller.RCTradeEventController;
import com.cyanspring.cstw.keepermanager.InstrumentPoolKeeperManager;
import com.cyanspring.cstw.localevent.SelectUserAccountLocalEvent;
import com.cyanspring.cstw.localevent.ServerStatusLocalEvent;
import com.cyanspring.cstw.session.CSTWSession;
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

	private List<String> singleOrderDisplayFieldList;
	private List<String> singleInstrumentDisplayFieldList;
	private List<String> multiInstrumentDisplayFieldList;

	private Map<String, Map<String, FieldDef>> singleOrderFieldDefMap;
	private Map<String, Map<String, FieldDef>> singleInstrumentFieldDefMap;
	private Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentFieldDefMap;

	private ScheduleManager scheduleManager;
	private AsyncTimerEvent timerEvent;
	private int heartBeatInterval; // 5 seconds
	private HashMap<String, Date> lastHeartBeatMap;
	private DefaultStartEndTime defaultStartEndTime;
	private Map<AlertType, Integer> alertColorConfig;
	private String userId;
	private String accountId;
	private Account loginAccount;

	private UserGroup userGroup;
	private List<String> accountGroupList;
	private List<Account> accountList;

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
		servers = new HashMap<String, Boolean>();
		listener = new EventListenerImpl();
		scheduleManager = new ScheduleManager();
		timerEvent = new AsyncTimerEvent();
		userId = Default.getUser();
		accountId = Default.getAccount();
		lastHeartBeatMap = new HashMap<String, Date>();
		heartBeatInterval = 10000;
		accountGroupList = new ArrayList<String>();
		accountList = new ArrayList<Account>();
		userGroup = new UserGroup("Admin", UserRole.Admin);
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

		orderManager = new OrderCachingManager();

		ServerStatusDisplay.getInstance().init();

		eventManager.subscribe(NodeInfoEvent.class, listener);
		eventManager.subscribe(InitClientEvent.class, listener);
		eventManager.subscribe(UserLoginReplyEvent.class, listener);
		eventManager.subscribe(SelectUserAccountLocalEvent.class, listener);
		eventManager.subscribe(ServerHeartBeatEvent.class, listener);
		eventManager.subscribe(ServerReadyEvent.class, listener);
		eventManager.subscribe(SingleOrderStrategyFieldDefUpdateEvent.class,
				listener);
		eventManager.subscribe(
				MultiInstrumentStrategyFieldDefUpdateEvent.class, listener);
		eventManager.subscribe(CSTWUserLoginReplyEvent.class, listener);
		eventManager
				.subscribe(AccountSettingSnapshotReplyEvent.class, listener);
		eventManager.subscribe(RateConverterReplyEvent.class, listener);
		eventManager.subscribe(AccountInstrumentSnapshotReplyEvent.class,
				listener);
		// schedule timer
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval, listener,
				timerEvent);
		log.info("TraderInfoListener not init version");
		// traderInfoListener = new TraderInfoListener();
		// initSessionListener();

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
				singleOrderFieldDefMap = initClientEvent
						.getSingleOrderFieldDefs();
				singleOrderDisplayFieldList = initClientEvent
						.getSingleOrderDisplayFields();
				singleInstrumentFieldDefMap = initClientEvent
						.getSingleInstrumentFieldDefs();
				singleInstrumentDisplayFieldList = initClientEvent
						.getSingleInstrumentDisplayFields();
				defaultStartEndTime = initClientEvent.getDefaultStartEndTime();
				multiInstrumentDisplayFieldList = initClientEvent
						.getMultiInstrumentDisplayFields();
				multiInstrumentFieldDefMap = initClientEvent
						.getMultiInstrumentStrategyFieldDefs();

			} else if (event instanceof CSTWUserLoginReplyEvent) {
				CSTWUserLoginReplyEvent evt = (CSTWUserLoginReplyEvent) event;
				processCSTWUserLoginReplyEvent(evt);
				if (evt.isOk()) {
					beanPool.getTickManager().init(getFirstServer());
					requestRateConverter();
					requestStrategyInfo(evt.getSender());
					// if(null != loginAccount);
					// traderInfoListener.init(loginAccount);

				}

			} else if (event instanceof AccountSettingSnapshotReplyEvent) {

				AccountSettingSnapshotReplyEvent evt = (AccountSettingSnapshotReplyEvent) event;
				processAccountSettingSnapshotReplyEvent(evt);
			} else if (event instanceof UserLoginReplyEvent) {
				UserLoginReplyEvent evt = (UserLoginReplyEvent) event;
				processUserLoginReplyEvent(evt);
				if (evt.isOk()) {
					requestStrategyInfo(evt.getSender());
				}
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
			} else if (event instanceof SingleOrderStrategyFieldDefUpdateEvent) {
				processingSingleOrderStrategyFieldDefUpdateEvent((SingleOrderStrategyFieldDefUpdateEvent) event);
			} else if (event instanceof SingleInstrumentStrategyFieldDefUpdateEvent) {
				processingSingleInstrumentStrategyFieldDefUpdateEvent((SingleInstrumentStrategyFieldDefUpdateEvent) event);
			} else if (event instanceof MultiInstrumentStrategyFieldDefUpdateEvent) {
				processingMultiInstrumentStrategyFieldDefUpdateEvent((MultiInstrumentStrategyFieldDefUpdateEvent) event);
			} else if (event instanceof AsyncTimerEvent) {
				processAsyncTimerEvent((AsyncTimerEvent) event);
			} else if (event instanceof SelectUserAccountLocalEvent) {
				processSelectUserAccountEvent((SelectUserAccountLocalEvent) event);
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

	private void requestRateConverter() {
		RateConverterRequestEvent request = new RateConverterRequestEvent(
				IdGenerator.getInstance().getNextID(), getFirstServer());
		try {
			getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void processSelectUserAccountEvent(SelectUserAccountLocalEvent event) {
		log.info("Setting current user/account to: " + this.userId + "/"
				+ this.accountId);
		this.userId = event.getUser();
		this.accountId = event.getAccount();
	}

	private void requestStrategyInfo(String server) {
		try {
			orderManager.init();
			eventManager.sendEvent(new ServerStatusLocalEvent(server, true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	synchronized private void processingSingleOrderStrategyFieldDefUpdateEvent(
			SingleOrderStrategyFieldDefUpdateEvent event) {
		singleOrderFieldDefMap.put(event.getName(), event.getFieldDefs());
		log.info("Single-order strategy field def update: " + event.getName());
	}

	private void processingSingleInstrumentStrategyFieldDefUpdateEvent(
			SingleInstrumentStrategyFieldDefUpdateEvent event) {
		singleInstrumentFieldDefMap.put(event.getName(), event.getFieldDefs());
		log.info("Single-instrument strategy field def update: "
				+ event.getName());
	}

	synchronized private void processingMultiInstrumentStrategyFieldDefUpdateEvent(
			MultiInstrumentStrategyFieldDefUpdateEvent event) {
		multiInstrumentFieldDefMap.put(event.getConfig().getStrategy(),
				event.getConfig());
		log.info("Multi-Instrument strategy field def update: "
				+ event.getConfig().getStrategy());
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

	synchronized public List<String> getParentOrderDisplayFields() {
		return singleOrderDisplayFieldList;
	}

	synchronized public DefaultStartEndTime getDefaultStartEndTime() {
		return defaultStartEndTime;
	}

	synchronized public List<String> getSingleOrderDisplayFields() {
		return singleOrderDisplayFieldList;
	}

	synchronized public Map<String, Map<String, FieldDef>> getSingleOrderFieldDefs() {
		return singleOrderFieldDefMap;
	}

	synchronized public List<String> getSingleInstrumentDisplayFields() {
		return singleInstrumentDisplayFieldList;
	}

	synchronized public Map<String, Map<String, FieldDef>> getSingleInstrumentFieldDefs() {
		return singleInstrumentFieldDefMap;
	}

	synchronized public Map<String, MultiInstrumentStrategyDisplayConfig> getMultiInstrumentFieldDefs() {
		return multiInstrumentFieldDefMap;
	}

	synchronized public List<String> getMultiInstrumentDisplayFields() {
		return multiInstrumentDisplayFieldList;
	}

	synchronized public List<String> getSingleOrderAmendableFields(String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleOrderFieldDefMap.get(key);
		if (null != fieldDefs) {
			for (FieldDef fieldDef : fieldDefs.values()) {
				if (fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
	}

	synchronized public List<String> getSingleInstrumentAmendableFields(
			String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleInstrumentFieldDefMap.get(key);
		if (null != fieldDefs) {
			for (FieldDef fieldDef : fieldDefs.values()) {
				if (fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
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

	private boolean processCSTWUserLoginReplyEvent(
			CSTWUserLoginReplyEvent loginReplyEvent) {
		if (!loginReplyEvent.isOk())
			return false;

		List<Account> accountList = loginReplyEvent.getAccountList();
		if (null != accountList && !accountList.isEmpty()) {
			loginAccount = loginReplyEvent.getAccountList().get(0);
			log.info("loginAccount:{}", loginAccount.getId());
			sendAccountSettingRequestEvent(loginAccount.getId());
		}
		Map<String, Account> user2AccoutMap = loginReplyEvent
				.getUser2AccountMap();
		if (null != user2AccoutMap && !user2AccoutMap.isEmpty()) {
			accountList.addAll(user2AccoutMap.values());
			for (Account acc : user2AccoutMap.values()) {
				accountGroupList.add(acc.getId());
			}
		}
		UserGroup userGroup = loginReplyEvent.getUserGroup();
		this.userId = userGroup.getUser();

		if (null != loginAccount) {
			this.accountId = loginAccount.getId();
		} else {
			this.accountId = userGroup.getUser();
		}

		this.userGroup = userGroup;
		beanPool.setUserGroup(userGroup);
		log.info("login user:{},{}", userId, userGroup.getRole());

		QuoteCachingManager.getInstance().init();
		if (this.userGroup.getRole() == UserRole.RiskManager
				|| this.userGroup.getRole() == UserRole.BackEndRiskManager) {
			allPositionManager.init(eventManager, getFirstServer(),
					accountList, getUserGroup());
			FrontRCPositionCachingManager.getInstance().init();
			FrontRCOrderCachingManager.getInstance().init();

			if (this.userGroup.getRole() == UserRole.RiskManager) {
				FrontRCOpenPositionEventController.getInstance().init();
			} else if (this.userGroup.getRole() == UserRole.BackEndRiskManager) {
				BackRCOpenPositionEventController.getInstance().init();
			}
			RCTradeEventController.getInstance().init();
			RCInstrumentStatisticsEventController.getInstance().init();
			RCIndividualEventController.getInstance().init();
			RCInstrumentSummaryEventController.getInstance().init();
			RCOrderEventController.getInstance().init();
		}
		// inject RiskManagerNGroupUser List from loginReplyEvent
		InstrumentPoolKeeperManager.getInstance().setRiskManagerNGroupUser(
				loginReplyEvent.getRiskManagerNGroupUsers());

		AccountInstrumentSnapshotRequestEvent request = new AccountInstrumentSnapshotRequestEvent(
				IdGenerator.getInstance().getNextID(), Business.getInstance()
						.getFirstServer(), IdGenerator.getInstance()
						.getNextID());

		try {
			this.getEventManager().sendRemoteEvent(request);
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return true;
	}

	private void processAccountInstrumentSnapshotReplyEvent(
			AccountInstrumentSnapshotReplyEvent replyEvent) {
		InstrumentPoolKeeperManager.getInstance().init();
		InstrumentPoolKeeperManager.getInstance().setInstrumentPoolKeeper(
				replyEvent.getInstrumentPoolKeeper());
	}

	private void sendAccountSettingRequestEvent(String accountId) {
		AccountSettingSnapshotRequestEvent settingRequestEvent = new AccountSettingSnapshotRequestEvent(
				IdGenerator.getInstance().getNextID(), Business.getInstance()
						.getFirstServer(), accountId, null);
		try {
			eventManager.sendRemoteEvent(settingRequestEvent);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	private void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		this.userId = event.getUser().getId();
		if (event.getDefaultAccount() != null) {
			this.accountId = event.getDefaultAccount().getId();
		} else if (null != event.getAccounts()
				&& event.getAccounts().size() > 0) {
			this.accountId = event.getAccounts().get(0).getId();
		}

	}

	public String getUser() {
		return userId;
	}

	public String getAccount() {
		return accountId;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public List<String> getAccountGroup() {
		return accountGroupList;
	}

	public boolean isManagee(String account) {
		if (userGroup.isAdmin() || userGroup.isGroupPairExist(account)
				|| userGroup.isManageeExist(account)) {
			return true;
		}
		return false;
	}

	public Account getLoginAccount() {
		return loginAccount;
	}

	public AllPositionManager getAllPositionManager() {
		return allPositionManager;
	}

	public List<Account> getAccountList() {
		return this.accountList;
	}

	public static IBusinessService getBusinessService() {
		return instance.beanPool;
	}

}
