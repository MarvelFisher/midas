package com.cyanspring.cstw.business;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.CSTWUserLoginReplyEvent;
import com.cyanspring.common.event.info.RateConverterRequestEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotRequestEvent;
import com.cyanspring.common.util.IdGenerator;
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

/**
 * 
 * @author NingXiaofeng
 * @date 2015.11.27
 *
 */
public final class UserLoginAssist {

	private static Logger log = LoggerFactory.getLogger(UserLoginAssist.class);

	private EventListenerImpl listener;

	private OrderCachingManager orderManager;

	public void init(OrderCachingManager orderManager) {
		listener = new EventListenerImpl();
		this.orderManager = orderManager;
		CSTWEventManager.subscribe(SelectUserAccountLocalEvent.class, listener);
		CSTWEventManager.subscribe(CSTWUserLoginReplyEvent.class, listener);
	}

	private class EventListenerImpl implements IAsyncEventListener {
		@Override
		public void onEvent(AsyncEvent event) {
			if (event instanceof SelectUserAccountLocalEvent) {
				processSelectUserAccountEvent((SelectUserAccountLocalEvent) event);
			} else if (event instanceof CSTWUserLoginReplyEvent) {
				CSTWUserLoginReplyEvent evt = (CSTWUserLoginReplyEvent) event;
				if (evt.isOk()) {
					processCSTWUserLoginReplyEvent(evt);
					Business.getCSTWBeanPool().getTickManager()
							.init(Business.getBusinessService().getFirstServer());
					requestRateConverter();
					requestStrategyInfo(evt.getSender());
				}
			}
		}
	}

	private boolean processCSTWUserLoginReplyEvent(
			CSTWUserLoginReplyEvent loginReplyEvent) {
		if (!loginReplyEvent.isOk()) {
			return false;
		}
		List<Account> accountList = loginReplyEvent.getAccountList();
		if (null != accountList && !accountList.isEmpty()) {
			Account loginAccount = loginReplyEvent.getAccountList().get(0);
			CSTWSession.getInstance().setLoginAccount(loginAccount);
			log.info("loginAccount:{}", loginAccount.getId());
			sendAccountSettingRequestEvent(loginAccount.getId());
		}
		Map<String, Account> user2AccoutMap = loginReplyEvent
				.getUser2AccountMap();
		if (null != user2AccoutMap && !user2AccoutMap.isEmpty()) {
			accountList.addAll(user2AccoutMap.values());
			for (Account acc : user2AccoutMap.values()) {
				CSTWSession.getInstance().getAccountGroupList()
						.add(acc.getId());
			}
		}
		UserGroup userGroup = loginReplyEvent.getUserGroup();
		CSTWSession.getInstance().setUserId(userGroup.getUser());

		if (CSTWSession.getInstance().getLoginAccount() != null) {
			CSTWSession.getInstance().setAccountId(
					CSTWSession.getInstance().getLoginAccount().getId());
		} else {
			CSTWSession.getInstance().setAccountId(userGroup.getUser());
		}

		CSTWSession.getInstance().setUserGroup(userGroup);
		log.info("login user:{},{}", CSTWSession.getInstance().getUserId(),
				userGroup.getRole());

		QuoteCachingManager.getInstance().init();
		if (CSTWSession.getInstance().getUserGroup().getRole() == UserRole.RiskManager
				|| CSTWSession.getInstance().getUserGroup().getRole() == UserRole.BackEndRiskManager) {
			Business.getInstance()
					.getAllPositionManager()
					.init(Business.getInstance().getEventManager(),
							Business.getBusinessService().getFirstServer(),
							accountList,
							CSTWSession.getInstance().getUserGroup());
			FrontRCPositionCachingManager.getInstance().init();
			FrontRCOrderCachingManager.getInstance().init();

			if (CSTWSession.getInstance().getUserGroup().getRole() == UserRole.RiskManager) {
				FrontRCOpenPositionEventController.getInstance().init();
			} else if (CSTWSession.getInstance().getUserGroup().getRole() == UserRole.BackEndRiskManager) {
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
				IdGenerator.getInstance().getNextID(), Business.getBusinessService()
						.getFirstServer(), IdGenerator.getInstance()
						.getNextID());
		CSTWEventManager.sendEvent(request);
		return true;
	}

	private void requestRateConverter() {
		RateConverterRequestEvent request = new RateConverterRequestEvent(
				IdGenerator.getInstance().getNextID(), Business.getBusinessService()
						.getFirstServer());
		try {
			CSTWEventManager.sendEvent(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void requestStrategyInfo(String server) {
		try {
			orderManager.init();
			CSTWEventManager
					.sendEvent(new ServerStatusLocalEvent(server, true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void sendAccountSettingRequestEvent(String accountId) {
		AccountSettingSnapshotRequestEvent settingRequestEvent = new AccountSettingSnapshotRequestEvent(
				IdGenerator.getInstance().getNextID(), Business.getBusinessService()
						.getFirstServer(), accountId, null);
		try {
			CSTWEventManager.sendEvent(settingRequestEvent);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	private void processSelectUserAccountEvent(SelectUserAccountLocalEvent event) {
		CSTWSession.getInstance().setUserId(event.getUser());
		CSTWSession.getInstance().setAccountId(event.getAccount());
		log.info("Setting current user/account to: " + event.getUser() + "/"
				+ event.getAccount());
	}

}
