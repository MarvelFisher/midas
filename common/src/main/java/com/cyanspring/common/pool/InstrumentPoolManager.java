package com.cyanspring.common.pool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotReplyEvent;
import com.cyanspring.common.event.pool.AccountInstrumentSnapshotRequestEvent;
import com.cyanspring.common.event.pool.AccountPoolsOperationReplyEvent;
import com.cyanspring.common.event.pool.AccountPoolsOperationRequestEvent;
import com.cyanspring.common.event.pool.ExchangeAccountOperationReplyEvent;
import com.cyanspring.common.event.pool.ExchangeAccountOperationRequestEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountOperationReplyEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountOperationRequestEvent;
import com.cyanspring.common.event.pool.InstrumentPoolOperationReplyEvent;
import com.cyanspring.common.event.pool.InstrumentPoolOperationRequestEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordUpdateRequestEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordsOperationReplyEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordsOperationRequestEvent;
import com.cyanspring.common.event.pool.PmAccountPoolsDeleteEvent;
import com.cyanspring.common.event.pool.PmAccountPoolsInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountDeleteEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeAccountUpdateEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountDeleteEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountInsertEvent;
import com.cyanspring.common.event.pool.PmExchangeSubAccountUpdateEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolDeleteEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolInsertEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordUpdateEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordsDeleteEvent;
import com.cyanspring.common.event.pool.PmInstrumentPoolRecordsInsertEvent;
import com.cyanspring.common.util.IdGenerator;

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

	@Autowired
	private AccountKeeper accountKeeper;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(AccountInstrumentSnapshotRequestEvent.class, null);
			subscribeToEvent(ExchangeAccountOperationRequestEvent.class, null);
			subscribeToEvent(ExchangeSubAccountOperationRequestEvent.class,
					null);
			subscribeToEvent(InstrumentPoolOperationRequestEvent.class, null);
			subscribeToEvent(InstrumentPoolRecordsOperationRequestEvent.class,
					null);
			subscribeToEvent(InstrumentPoolRecordUpdateRequestEvent.class, null);
			subscribeToEvent(AccountPoolsOperationRequestEvent.class, null);
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

		boolean ifExists = instrumentPoolKeeper.ifExists(exchangeAccount);
		switch (type) {
		case CREATE:
			if (!ifExists) {
				PmExchangeAccountInsertEvent insertEvent = new PmExchangeAccountInsertEvent(
						exchangeAccount);
				eventManager.sendEvent(insertEvent);
			} else {
				ok = false;
				errorCode = 4;
				message = exchangeAccount.getId();
			}
			break;
		case UPDATE:
			if (ifExists) {
				PmExchangeAccountUpdateEvent updateEvent = new PmExchangeAccountUpdateEvent(
						exchangeAccount);
				eventManager.sendEvent(updateEvent);
			} else {
				ok = false;
				errorCode = 5;
				message = exchangeAccount.getId();
			}
			break;
		case DELETE:
			if (ifExists) {
				List<ExchangeSubAccount> subAccounts = instrumentPoolKeeper
						.getExchangeSubAccountList(exchangeAccount.getId());
				if (subAccounts != null && !subAccounts.isEmpty()) {
					ok = false;
					errorCode = 6;
					message = "ExchangeSubAccount(s)";
				} else {
					PmExchangeAccountDeleteEvent deleteEvent = new PmExchangeAccountDeleteEvent(
							exchangeAccount);
					eventManager.sendEvent(deleteEvent);
				}
			} else {
				ok = false;
				errorCode = 5;
				message = exchangeAccount.getId();
			}
			break;
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

	public void processExchangeSubAccountOperationRequestEvent(
			ExchangeSubAccountOperationRequestEvent event) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		ExchangeSubAccount exchangeSubAccount = event.getExchangeSubAccount();
		OperationType type = event.getOperationType();
		log.info("Received ExchangeSubAccountOperationRequestEvent: "
				+ exchangeSubAccount + ", " + type);

		boolean ifExists = instrumentPoolKeeper.ifExists(exchangeSubAccount);
		switch (type) {
		case CREATE:
			if (!ifExists) {
				PmExchangeSubAccountInsertEvent insertEvent = new PmExchangeSubAccountInsertEvent(
						exchangeSubAccount);
				eventManager.sendEvent(insertEvent);
			} else {
				ok = false;
				errorCode = 4;
				message = exchangeSubAccount.getId();
			}
			break;
		case UPDATE:
			if (ifExists) {
				PmExchangeSubAccountUpdateEvent updateEvent = new PmExchangeSubAccountUpdateEvent(
						exchangeSubAccount);
				eventManager.sendEvent(updateEvent);
			} else {
				ok = false;
				errorCode = 4;
				message = exchangeSubAccount.getId();
			}
			break;
		case DELETE:
			if (ifExists) {
				PmExchangeSubAccountDeleteEvent deleteEvent = new PmExchangeSubAccountDeleteEvent(
						exchangeSubAccount);
				eventManager.sendEvent(deleteEvent);
			} else {
				ok = false;
				errorCode = 5;
				message = exchangeSubAccount.getId();
			}
			break;
		}

		ExchangeSubAccountOperationReplyEvent replyEvent = new ExchangeSubAccountOperationReplyEvent(
				event.getKey(), event.getSender(), ok, message, errorCode,
				event.getTxId());
		if (ok) {
			InstrumentPoolHelper.updateExchangeSubAccount(instrumentPoolKeeper,
					exchangeSubAccount, type);
			replyEvent.setExchangeSubAccount(exchangeSubAccount);
			replyEvent.setOperationType(type);
		}
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void processInstrumentPoolOperationRequestEvent(
			InstrumentPoolOperationRequestEvent event) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		InstrumentPool instrumentPool = event.getInstrumentPool();
		OperationType type = event.getOperationType();
		log.info("Received InstrumentPoolOperationRequestEvent: "
				+ instrumentPool + ", " + type);

		boolean ifExists = instrumentPoolKeeper.ifExists(instrumentPool);
		switch (type) {
		case CREATE:
			if (!ifExists) {
				instrumentPool.setId(genNextInstrumentPoolId());
				PmInstrumentPoolInsertEvent insertEvent = new PmInstrumentPoolInsertEvent(
						instrumentPool);
				eventManager.sendEvent(insertEvent);
			} else {
				ok = false;
				errorCode = 4;
				message = instrumentPool.getName();
			}
			break;
		case DELETE:
			if (ifExists) {
				List<InstrumentPoolRecord> records = instrumentPoolKeeper
						.getInstrumentPoolRecordList(instrumentPool.getId());
				if (!records.isEmpty()) {
					PmInstrumentPoolRecordsDeleteEvent deleteEvent = new PmInstrumentPoolRecordsDeleteEvent(
							records);
					eventManager.sendEvent(deleteEvent);
				}
				PmInstrumentPoolDeleteEvent deleteEvent = new PmInstrumentPoolDeleteEvent(
						instrumentPool);
				eventManager.sendEvent(deleteEvent);
			} else {
				ok = false;
				errorCode = 5;
				message = instrumentPool.getName();
			}
			break;
		}
		InstrumentPoolOperationReplyEvent replyEvent = new InstrumentPoolOperationReplyEvent(
				event.getKey(), event.getSender(), ok, message, errorCode,
				event.getTxId());
		if (ok) {
			InstrumentPoolHelper.updateInstrumentPool(instrumentPoolKeeper,
					instrumentPool, type);
			replyEvent.setInstrumentPool(instrumentPool);
			replyEvent.setOperationType(type);
		}
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void processInstrumentPoolRecordsOperationRequestEvent(
			InstrumentPoolRecordsOperationRequestEvent event) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		List<InstrumentPoolRecord> records = event.getInstrumentPoolRecords();
		OperationType type = event.getOperationType();
		log.info("Received InstrumentPoolRecordsOperationRequestEvent: "
				+ records + ", " + type);

		switch (type) {
		case CREATE:
			PmInstrumentPoolRecordsInsertEvent insertEvent = new PmInstrumentPoolRecordsInsertEvent(
					records);
			eventManager.sendEvent(insertEvent);
			break;
		case DELETE:
			PmInstrumentPoolRecordsDeleteEvent deleteEvent = new PmInstrumentPoolRecordsDeleteEvent(
					records);
			eventManager.sendEvent(deleteEvent);
			break;
		}
		InstrumentPoolHelper.updateInstrumentPoolRecords(instrumentPoolKeeper,
				records, type);
		InstrumentPoolRecordsOperationReplyEvent replyEvent = new InstrumentPoolRecordsOperationReplyEvent(
				event.getKey(), event.getSender(), ok, message, errorCode,
				event.getTxId());
		replyEvent.setInstrumentPoolRecords(records);
		replyEvent.setOperationType(type);
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void processInstrumentPoolRecordUpdateRequestEvent(
			InstrumentPoolRecordUpdateRequestEvent event) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		InstrumentPoolRecord record = event.getInstrumentPoolRecord();
		log.info("Received InstrumentPoolRecordUpdateRequestEvent: " + record);
		if (instrumentPoolKeeper.ifExists(record)) {
			instrumentPoolKeeper.update(record);
			PmInstrumentPoolRecordUpdateEvent updateEvent = new PmInstrumentPoolRecordUpdateEvent(
					record);
			eventManager.sendEvent(updateEvent);
		} else {
			ok = false;
			errorCode = 5;
			message = record.getSymbol();
		}
		InstrumentPoolRecordUpdateEvent replyEvent = new InstrumentPoolRecordUpdateEvent(
				event.getKey(), event.getSender(), ok, message, errorCode,
				event.getTxId());
		if (ok) {
			replyEvent.setInstrumentPoolRecord(record);
		}
		eventManager.sendRemoteEvent(replyEvent);
	}

	public void processAccountPoolsOperationRequestEvent(
			AccountPoolsOperationRequestEvent event) throws Exception {
		boolean ok = true;
		int errorCode = -1;
		String message = "";
		List<AccountPool> accountPools = event.getAccountPools();
		OperationType type = event.getOperationType();
		log.info("Received AccountPoolOperationRequestEvent: " + accountPools
				+ ", " + type);

		if (accountPools != null && !accountPools.isEmpty()) {
			InstrumentPoolHelper.updateAccountPools(accountKeeper,
					instrumentPoolKeeper, accountPools, type);
			switch (type) {
			case CREATE:
				PmAccountPoolsInsertEvent insertEvent = new PmAccountPoolsInsertEvent(
						accountPools);
				eventManager.sendEvent(insertEvent);
				break;
			case DELETE:
				PmAccountPoolsDeleteEvent deleteEvent = new PmAccountPoolsDeleteEvent(
						accountPools);
				eventManager.sendEvent(deleteEvent);
				break;
			}
		} else {
			ok = false;
			errorCode = 7;
		}
		AccountPoolsOperationReplyEvent replyEvent = new AccountPoolsOperationReplyEvent(
				event.getKey(), event.getSender(), ok, message, errorCode,
				event.getTxId());
		if (ok) {
			replyEvent.setAccountPools(accountPools);
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

	public String genNextInstrumentPoolId() {
		return "P" + IdGenerator.getInstance().getNextID();
	}

}
