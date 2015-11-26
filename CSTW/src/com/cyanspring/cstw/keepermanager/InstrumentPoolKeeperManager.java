package com.cyanspring.cstw.keepermanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.User;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.pool.AccountPoolsUpdateEvent;
import com.cyanspring.common.event.pool.ExchangeAccountOperationReplyEvent;
import com.cyanspring.common.event.pool.ExchangeAccountUpdateEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountOperationReplyEvent;
import com.cyanspring.common.event.pool.ExchangeSubAccountUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolRecordsUpdateEvent;
import com.cyanspring.common.event.pool.InstrumentPoolUpdateEvent;
import com.cyanspring.common.pool.InstrumentPoolHelper;
import com.cyanspring.common.pool.InstrumentPoolKeeper;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.CSTWEventManager;
import com.cyanspring.cstw.localevent.InstrumentPoolUpdateLocalEvent;
import com.cyanspring.cstw.localevent.SubAccountStructureUpdateLocalEvent;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.16
 *
 */
public final class InstrumentPoolKeeperManager {

	private static InstrumentPoolKeeperManager instance;

	private InstrumentPoolKeeper instrumentPoolKeeper;
	
	private List<User> riskManagerNGroupUser;
	
	private Map<String, User> riskManagerNGroupUserMap = new HashMap<String, User>();;

	private IAsyncEventListener listener;

	public static InstrumentPoolKeeperManager getInstance() {
		if (instance == null) {
			instance = new InstrumentPoolKeeperManager();
		}
		return instance;
	}

	public void init() {
		listener = new IAsyncEventListener() {
			@Override
			public void onEvent(AsyncEvent event) {
				if (event instanceof ExchangeAccountUpdateEvent) {
					processExchangeAccountUpdateEvent((ExchangeAccountUpdateEvent) event);
				} else if (event instanceof ExchangeSubAccountUpdateEvent) {
					processExchangeSubAccountUpdateEvent((ExchangeSubAccountUpdateEvent) event);
				} else if (event instanceof InstrumentPoolUpdateEvent) {
					processInstrumentPoolUpdateEvent((InstrumentPoolUpdateEvent) event);
				} else if (event instanceof InstrumentPoolRecordsUpdateEvent) {
					processInstrumentPoolRecordsUpdateEvent((InstrumentPoolRecordsUpdateEvent) event);
				} else if (event instanceof InstrumentPoolRecordUpdateEvent) {
					processInstrumentPoolRecordUpdateEvent((InstrumentPoolRecordUpdateEvent) event);
				} else if (event instanceof AccountPoolsUpdateEvent) {
					processAccountPoolsUpdateEvent((AccountPoolsUpdateEvent) event);
				}
			}
		};
		CSTWEventManager.subscribe(ExchangeAccountOperationReplyEvent.class, listener);
		CSTWEventManager.subscribe(ExchangeAccountUpdateEvent.class, listener);
		
		CSTWEventManager.subscribe(ExchangeSubAccountOperationReplyEvent.class, listener);
		CSTWEventManager.subscribe(ExchangeSubAccountUpdateEvent.class,listener);
		
		CSTWEventManager.subscribe(InstrumentPoolUpdateEvent.class, listener);
		CSTWEventManager.subscribe(InstrumentPoolRecordsUpdateEvent.class,
				listener);
		CSTWEventManager.subscribe(InstrumentPoolRecordUpdateEvent.class,
				listener);
		CSTWEventManager.subscribe(AccountPoolsUpdateEvent.class, listener);
	}

	public void processExchangeAccountUpdateEvent(
			ExchangeAccountUpdateEvent event) {
		InstrumentPoolHelper.updateExchangeAccount(instrumentPoolKeeper,
				event.getExchangeAccount(), event.getOperationType());
		sendSubAccountStructureUpdateEvent();
	}

	public void processExchangeSubAccountUpdateEvent(
			ExchangeSubAccountUpdateEvent event) {
		InstrumentPoolHelper.updateExchangeSubAccount(instrumentPoolKeeper,
				event.getExchangeSubAccount(), event.getOperationType());
		sendSubAccountStructureUpdateEvent();
	}

	public void processInstrumentPoolUpdateEvent(InstrumentPoolUpdateEvent event) {
		InstrumentPoolHelper.updateInstrumentPool(instrumentPoolKeeper,
				event.getInstrumentPool(), event.getOperationType());
		sendInstrumentPoolUpdateEvent();
	}

	public void processInstrumentPoolRecordsUpdateEvent(
			InstrumentPoolRecordsUpdateEvent event) {
		InstrumentPoolHelper.updateInstrumentPoolRecords(instrumentPoolKeeper,
				event.getInstrumentPoolRecords(), event.getOperationType());
		sendInstrumentPoolUpdateEvent();
	}

	public void processInstrumentPoolRecordUpdateEvent(
			InstrumentPoolRecordUpdateEvent event) {
		instrumentPoolKeeper.update(event.getInstrumentPoolRecord());
		sendInstrumentPoolUpdateEvent();
	}

	public void processAccountPoolsUpdateEvent(AccountPoolsUpdateEvent event) {
		InstrumentPoolHelper.updateAccountPools(null, instrumentPoolKeeper,
				event.getAccountPools(), event.getOperationType());
		sendInstrumentPoolUpdateEvent();
	}

	public IInstrumentPoolKeeper getInstrumentPoolKeeper() {
		return instrumentPoolKeeper;
	}

	public void setInstrumentPoolKeeper(
			InstrumentPoolKeeper instrumentPoolKeeper) {
		this.instrumentPoolKeeper = instrumentPoolKeeper;
	}
	
	public List<User> getRiskManagerNGroupUser() {
		return riskManagerNGroupUser;
	}
	
	public User getRiskManagerOrGroupUser(String userId) {
		return riskManagerNGroupUserMap.get(userId);
	}

	public void setRiskManagerNGroupUser(List<User> riskManagerNGroupUser) {
		this.riskManagerNGroupUser = riskManagerNGroupUser;
		if (this.riskManagerNGroupUser != null) {
			riskManagerNGroupUserMap.clear();
			for (User usr : this.riskManagerNGroupUser) {
				riskManagerNGroupUserMap.put(usr.getId(), usr);
			}
		}
	}

	private void sendInstrumentPoolUpdateEvent() {
		InstrumentPoolUpdateLocalEvent event = new InstrumentPoolUpdateLocalEvent(
				IdGenerator.getInstance().getNextID());
		CSTWEventManager.sendEvent(event);

	}
	
	private void sendSubAccountStructureUpdateEvent() {
		SubAccountStructureUpdateLocalEvent event = new SubAccountStructureUpdateLocalEvent(
				IdGenerator.getInstance().getNextID());
		CSTWEventManager.sendEvent(event);

	}

}
