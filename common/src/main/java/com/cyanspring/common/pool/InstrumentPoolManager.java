package com.cyanspring.common.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotReplyEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotRequestEvent;
import com.cyanspring.common.event.pool.ExchangeAccountOperationReplyEvent;
import com.cyanspring.common.event.pool.ExchangeAccountOperationRequestEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountUpdateEvent;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class InstrumentPoolManager implements IPlugin {

	private static final Logger log = LoggerFactory
			.getLogger(InstrumentPoolManager.class);

	private static final String ID = InstrumentPoolManager.class.getName();

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private InstrumentPoolKeeper instrumentPoolKeeper;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AccountInstrumentSnapshotRequestEvent.class, null);
			subscribeToEvent(ExchangeAccountOperationRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	@Override
	public void init() throws Exception {
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null) {
			eventProcessor.getThread().setName(ID);
		}
	}

	@Override
	public void uninit() {
		eventProcessor.uninit();
	}

	public void processAccountInstrumentSnapshotRequestEvent(
			AccountInstrumentSnapshotRequestEvent requestEvent)
			throws Exception {
		AccountInstrumentSnapshotReplyEvent replyEvent = new AccountInstrumentSnapshotReplyEvent(
				requestEvent.getKey(), requestEvent.getSender(), true, "ok",
				-1, requestEvent.getTxId());
		log.info("Received AccountInstrumentSnapshotReplyEvent");
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void processExchangeAccountOperationRequestEvent(
			ExchangeAccountOperationRequestEvent requestEvent) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		OperationType type = requestEvent.getOperationType();
		ExchangeAccount exchangeAccount = requestEvent.getExchangeAccount();
		log.info("Received ExchangeAccountOperationRequestEvent: "
				+ exchangeAccount + ", " + type);

		if (type == OperationType.CREATE) {
			if (!instrumentPoolKeeper.ifExists(exchangeAccount)) {
				PmExchangeAccountInsertEvent insertEvent = new PmExchangeAccountInsertEvent(
						exchangeAccount);
				eventManager.sendEvent(insertEvent);
			} else {
				ok = false;
				errorCode = 4;
				message = exchangeAccount.getId();
			}
		} else if (type == OperationType.UPDATE) {
			if (instrumentPoolKeeper.ifExists(exchangeAccount)) {
				PmExchangeAccountUpdateEvent updateEvent = new PmExchangeAccountUpdateEvent(
						exchangeAccount);
				eventManager.sendEvent(updateEvent);
			} else {
				ok = false;
				errorCode = 5;
				message = exchangeAccount.getId();
			}
		} else if (type == OperationType.DELETE) {
			if (instrumentPoolKeeper.ifExists(exchangeAccount)) {
				//instrumentPoolKeeper.get,
			} else {
				ok = false;
				errorCode = 5;
				message = exchangeAccount.getId();
			}
		}
		ExchangeAccountOperationReplyEvent replyEvent = new ExchangeAccountOperationReplyEvent(
				requestEvent.getKey(), requestEvent.getSender(), ok, message,
				errorCode, requestEvent.getTxId());
		if (ok) {
			InstrumentPoolHelper.updateExchangeAccount(instrumentPoolKeeper,
					exchangeAccount, type);

			replyEvent.setExchangeAccount(exchangeAccount);
			replyEvent.setOperationType(type);
		}
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void injectExchangeAccounts(List<ExchangeAccount> exchangeAccounts) {
		instrumentPoolKeeper.injectExchangeAccounts(exchangeAccounts);
	}

	public void injectExchangeSubAccounts(
			List<ExchangeSubAccount> exchangeSubAccounts) {
		instrumentPoolKeeper.injectExchangeSubAccounts(exchangeSubAccounts);
	}

	public void injectInstrumentPools(List<InstrumentPool> instrumentPools) {
		instrumentPoolKeeper.injectInstrumentPools(instrumentPools);
	}

	public void injectAccountPools(List<AccountPool> accountPools) {
		instrumentPoolKeeper.injectAccountPools(accountPools);
	}

	public void injectInstrumentPoolRecords(
			List<InstrumentPoolRecord> instrumentPoolRecords) {
		instrumentPoolKeeper.injectInstrumentPoolRecords(instrumentPoolRecords);
	}

}
