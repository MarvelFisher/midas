package com.cyanspring.server.statistic;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.event.GenericAsyncEventProcessor;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.statistic.AccountNumberReplyEvent;
import com.cyanspring.common.event.statistic.AccountNumberRequestEvent;
import com.cyanspring.common.event.statistic.AccountStatisticReplyEvent;
import com.cyanspring.common.event.statistic.AccountStatisticRequestEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;

public class StatisticManager implements IPlugin {

	private static final Logger log = LoggerFactory
			.getLogger(StatisticManager.class);

	private static final String ID = "StatisticMgr-"
			+ IdGenerator.getInstance().getNextID();
	private static final String SENDER = StatisticManager.class.getSimpleName();

	@Autowired
	protected IRemoteEventManager eventManager;

	@Autowired
	private GenericAsyncEventProcessor genericAsyncEventProcessor;

	@Autowired
	AccountKeeper accountKeeper;

	@Override
	public void init() throws Exception {
		genericAsyncEventProcessor.subscribeToEvent(
				AccountStatisticRequestEvent.class, this, null);
		genericAsyncEventProcessor.subscribeToEvent(
				AccountNumberRequestEvent.class, this, null);
	}

	@Override
	public void uninit() {
	}

	public void processAccountStatisticRequestEvent(
			AccountStatisticRequestEvent event) {

		AccountStatisticCollector cal = new AccountStatisticCollector();
		List<Account> accounts = accountKeeper.getAllAccounts();
		try {
			if (null == accounts) {
				return;
			}
			log.info("AccountStatistic Account Size:{}", accounts.size());
			for (Account account : accounts) {
				cal.calculate(account,
						accountKeeper.getAccountSetting(account.getId()));
			}
			AccountStatisticReplyEvent reply = new AccountStatisticReplyEvent(
					event.getKey(), event.getSender(),
					cal.toDefaultCaculatedMap());
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processAccountNumberRequestEvent(AccountNumberRequestEvent event) {
		try {
			boolean isOk = true;
			String errorMessage = null;
			if (null == accountKeeper) {
				isOk = false;
				errorMessage = MessageLookup
						.buildEventMessage(ErrorMessage.EXCEPTION_MESSAGE,
								"AccountKeeper is null");
			}
			AccountNumberReplyEvent reply = new AccountNumberReplyEvent(
					event.getKey(), event.getSender(), accountKeeper
							.getAllAccounts().size(), isOk, errorMessage);
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
