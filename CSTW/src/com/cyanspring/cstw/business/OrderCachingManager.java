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
package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.AllStrategySnapshotReplyEvent;
import com.cyanspring.common.event.order.AllStrategySnapshotRequestEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.order.StrategySnapshotEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.StrategyLogEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.event.AccountSelectionEvent;
import com.cyanspring.cstw.event.GuiMultiInstrumentStrategyUpdateEvent;
import com.cyanspring.cstw.event.GuiSingleInstrumentStrategyUpdateEvent;
import com.cyanspring.cstw.event.GuiSingleOrderStrategyUpdateEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;

public class OrderCachingManager implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(OrderCachingManager.class);
	SingleOrderStrategyCache singleOrderStrategyCache = new SingleOrderStrategyCache();
	SingleInstrumentStrategyCache singleInstrumentStrategyCache = new SingleInstrumentStrategyCache();
	IRemoteEventManager eventManager;
	private ArrayList<String> servers = new ArrayList<String>();
	private Queue<ParentOrder> singleOrderStrategyQueue = new LinkedList<ParentOrder>();
	private Queue<Instrument> singleInstrumentStrategyQueue = new LinkedList<Instrument>();
	private MultiInstrumentStrategyCache multiInstrumentStrategyCache = new MultiInstrumentStrategyCache();
	private Queue<MultiInstrumentStrategyData> multiInstrumentStrategyQueue = new LinkedList<MultiInstrumentStrategyData>();
	private HashMap<String, LinkedList<StrategyLogEvent>> logs = new HashMap<String, LinkedList<StrategyLogEvent>>();
	private boolean ready;
	private static final int maxLog = 100;
	private String currentAccount;

	private GroupOrderCache groupOrderCache = new GroupOrderCache();

	public OrderCachingManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}

	public synchronized void processStrategySnapshotEvent(
			StrategySnapshotEvent event) {
		// clearing strategy log for this server
		clearLogs(event.getSender());
		servers.add(event.getSender());

		// processing parent orders snapshot for this server
		singleOrderStrategyCache.clearOrders(event.getSender());
		for (ParentOrder order : event.getOrders()) {

			// order.put(OrderField.SERVER.value(), event.getSender());
			singleOrderStrategyCache.update(order);
		}
		processQueuedParentOrderUpdate();

		// processing SingleInstrumentStrategy snapshot for this server
		singleInstrumentStrategyCache.clearInstruments(event.getSender());
		for (Instrument instrument : event.getInstruments()) {
			// order.put(OrderField.SERVER.value(), event.getSender());
			singleInstrumentStrategyCache.update(instrument);
		}
		processQueuedSingleInstrumentStrategyUpdate();

		// processing MultiInstrumentStrategy snapshot for this server
		multiInstrumentStrategyCache.clear(event.getSender());
		for (MultiInstrumentStrategyData data : event.getStrategyData()) {
			// data.put(OrderField.SERVER.value(), event.getSender());
			multiInstrumentStrategyCache.update(data);
		}
		processQueuedMultiInstrumentStrategyUpdate();

		// setting ready
		setReady(true);
		eventManager.sendEvent(new OrderCacheReadyEvent(null));
	}

	private void clearLogs(String sender) {
		List<Map<String, Object>> orders = singleOrderStrategyCache.getOrders();
		for (Map<String, Object> map : orders) {
			String name = (String) map.get(OrderField.SERVER_ID.value());
			if (name.equals(sender)) {
				String key = (String) map.get(OrderField.ID.value());
				logs.remove(key);
				log.debug("Removed strategy log: " + key);
			}
		}
		List<Map<String, Object>> list = multiInstrumentStrategyCache
				.getStrategyList();
		for (Map<String, Object> map : list) {
			String name = (String) map.get(OrderField.SERVER_ID.value());
			if (name.equals(sender)) {
				String key = (String) map.get(OrderField.ID.value());
				logs.remove(key);
				log.debug("Removed strategy log: " + key);
			}
		}
	}

	private void processQueuedParentOrderUpdate() {
		LinkedList<ParentOrder> newQueue = new LinkedList<ParentOrder>();
		while (singleOrderStrategyQueue.size() > 0) {
			ParentOrder order = singleOrderStrategyQueue.remove();
			String server = order.get(String.class,
					OrderField.SERVER_ID.value());
			if (servers.contains(server)) {
				singleOrderStrategyCache.update(order);
			} else {
				newQueue.add(order);
			}
		}
		singleOrderStrategyQueue = newQueue;
	}

	private void processQueuedSingleInstrumentStrategyUpdate() {
		LinkedList<Instrument> newQueue = new LinkedList<Instrument>();
		while (singleInstrumentStrategyQueue.size() > 0) {
			Instrument instrument = singleInstrumentStrategyQueue.remove();
			String server = instrument.get(String.class,
					OrderField.SERVER_ID.value());
			String accountId = instrument.getAccount();
			if (servers.contains(server)) {
				if (accountId != null && accountId.equals(currentAccount)) {
					singleInstrumentStrategyCache.update(instrument);
				}
				groupOrderCache.updateInstrument(instrument);
			} else {
				newQueue.add(instrument);
			}
		}
		singleInstrumentStrategyQueue = newQueue;
	}

	private void processQueuedMultiInstrumentStrategyUpdate() {
		LinkedList<MultiInstrumentStrategyData> newQueue = new LinkedList<MultiInstrumentStrategyData>();
		while (multiInstrumentStrategyQueue.size() > 0) {
			MultiInstrumentStrategyData data = multiInstrumentStrategyQueue
					.remove();
			String server = data
					.get(String.class, OrderField.SERVER_ID.value());
			String accountId = data.getAccount();
			if (servers.contains(server)) {
				if (accountId != null && accountId.equals(currentAccount)) {
					multiInstrumentStrategyCache.update(data);
				}
				groupOrderCache.updateMultiInstrumentStrategyData(data);
			} else {
				newQueue.add(data);
			}
		}
		multiInstrumentStrategyQueue = newQueue;
	}

	private void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		ParentOrder parentOrder = ((ParentOrderUpdateEvent) event).getOrder();

		log.debug("Update parent order recieved: " + parentOrder);
		String server = parentOrder.get(String.class,
				OrderField.SERVER_ID.value());
		String accountId = parentOrder.getAccount();
		if (server == null || server.equals("") || servers.contains(server)) {
			if (accountId != null && accountId.equals(currentAccount)) {
				log.info("update currentAccoount :{}", currentAccount);
				singleOrderStrategyCache.update(parentOrder);
			}
			log.info("update groupOrdr :{},{}", parentOrder.getAccount(),
					parentOrder.getId());

			groupOrderCache.updateOrder(parentOrder);
			eventManager.sendEvent(new GuiSingleOrderStrategyUpdateEvent(
					parentOrder));
		} else {
			singleOrderStrategyQueue.add(parentOrder);
		}
	}

	private void processStrategyLogEvent(StrategyLogEvent event) {
		if (null == event.getAccount()) {
			log.error("Event has no account! " + event.getMessage());
		} else if (!event.getAccount().equals(
				Business.getInstance().getAccount()))
			return;

		LinkedList<StrategyLogEvent> list = logs.get(event.getKey());
		if (null == list) {
			list = new LinkedList<StrategyLogEvent>();
			logs.put(event.getKey(), list);
		}

		if (list.size() >= maxLog) {
			list.remove();
		}
		list.add(event);
	}

	public List<StrategyLogEvent> getLogEvents(String id) {
		LinkedList<StrategyLogEvent> list = logs.get(id);
		if (null == list) {
			list = new LinkedList<StrategyLogEvent>();
			logs.put(id, list);
		}
		return new ArrayList<StrategyLogEvent>(list);
	}

	public synchronized List<Map<String, Object>> getParentOrders() {
		return singleOrderStrategyCache.getOrders();
	}

	public synchronized List<Map<String, Object>> getInstruments() {
		return singleInstrumentStrategyCache.getInstruments();
	}

	public synchronized Instrument getInstrument(String id) {
		return singleInstrumentStrategyCache.getInstrument(id);
	}

	public synchronized List<Map<String, Object>> getMultiInstrumentStrategies() {
		return multiInstrumentStrategyCache.getStrategyList();
	}

	public synchronized List<Map<String, Object>> getMultiInstruments(String id) {
		return multiInstrumentStrategyCache.getInstrumentList(id);
	}

	public synchronized Map<String, Object> getMultiInstrumentStrategy(String id) {
		return multiInstrumentStrategyCache.getStrategy(id);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void init() {
		eventManager.subscribe(AccountSelectionEvent.class, this);
		eventManager.subscribe(StrategySnapshotEvent.class, this);
		eventManager.subscribe(StrategyLogEvent.class, this);
		subscribeAccountOrder(Business.getInstance().getAccount());
		eventManager.subscribe(SingleInstrumentStrategyUpdateEvent.class,
				Business.getInstance().getAccount(), this);
		eventManager.subscribe(MultiInstrumentStrategyUpdateEvent.class,
				Business.getInstance().getAccount(), this);
		eventManager.subscribe(AllStrategySnapshotReplyEvent.class, this);

		try {
			if (Business.getInstance().getUserGroup().isAdmin()) {
				eventManager
						.sendRemoteEvent(new AllStrategySnapshotRequestEvent(
								IdGenerator.getInstance().getNextID(), Business
										.getInstance().getFirstServer(), null));
			} else if (Business.getInstance().getUserGroup().getRole()
					.isManagerLevel()) {
				eventManager
						.sendRemoteEvent(new AllStrategySnapshotRequestEvent(
								IdGenerator.getInstance().getNextID(), Business
										.getInstance().getFirstServer(),
								Business.getInstance().getAccountGroup()));
			} else {
				eventManager.sendRemoteEvent(new StrategySnapshotRequestEvent(
						Business.getInstance().getAccount(), Business
								.getInstance().getFirstServer(), null));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void subscribeAccountOrder(String account) {
		eventManager.unsubscribe(ParentOrderUpdateEvent.class, currentAccount,
				this);
		currentAccount = account;
		eventManager.subscribe(ParentOrderUpdateEvent.class, currentAccount,
				this);
	}

	public void uninit() {
		eventManager.unsubscribe(StrategySnapshotEvent.class, this);
		eventManager.unsubscribe(StrategyLogEvent.class, this);
		eventManager.unsubscribe(ParentOrderUpdateEvent.class, Business
				.getInstance().getAccount(), this);
		eventManager.unsubscribe(SingleInstrumentStrategyUpdateEvent.class,
				Business.getInstance().getAccount(), this);
		eventManager.unsubscribe(MultiInstrumentStrategyUpdateEvent.class,
				Business.getInstance().getAccount(), this);
		eventManager.unsubscribe(AllStrategySnapshotReplyEvent.class, Business
				.getInstance().getAccount(), this);

	}

	public ArrayList<String> getServers() {
		return servers;
	}

	public Map<String, Object> getStrategyData(String id) {
		DataObject result = singleOrderStrategyCache.getParentOrder(id);
		if (null != result)
			return result.getFields();

		Map<String, Object> multi = multiInstrumentStrategyCache
				.getStrategy(id);
		if (null != multi)
			return multi;

		Instrument instr = singleInstrumentStrategyCache.getInstrument(id);
		if (null != instr)
			return instr.getFields();

		return null;
	}

	private void processMultiInstrumentStrategyUpdateEvent(
			MultiInstrumentStrategyUpdateEvent event) {
		MultiInstrumentStrategyData data = event.getStrategyData();
		// data.put(OrderField.SERVER.value(), event.getSender());
		String server = event.getSender();
		if (server == null || server.equals("") || servers.contains(server)) {
			multiInstrumentStrategyCache.update(data);
			eventManager.sendEvent(new GuiMultiInstrumentStrategyUpdateEvent(
					event.getStrategyData().getId()));
		} else {
			multiInstrumentStrategyQueue.add(data);
		}
	}

	private void processSingleInstrumentStrategyUpdateEvent(
			SingleInstrumentStrategyUpdateEvent event) {
		String server = event.getSender();
		if (server == null || server.equals("") || servers.contains(server)) {
			singleInstrumentStrategyCache.update(event.getInstrument());
			eventManager.sendEvent(new GuiSingleInstrumentStrategyUpdateEvent(
					event.getInstrument()));
		} else {
			singleInstrumentStrategyQueue.add(event.getInstrument());
		}
	}

	@Override
	synchronized public void onEvent(AsyncEvent event) {
		if (event instanceof StrategySnapshotEvent) {
			processStrategySnapshotEvent((StrategySnapshotEvent) event);
		} else if (event instanceof AccountSelectionEvent) {
			subscribeAccountOrder(((AccountSelectionEvent) event).getAccount());
		} else if (event instanceof StrategyLogEvent) {
			processStrategyLogEvent((StrategyLogEvent) event);
		} else if (event instanceof ParentOrderUpdateEvent) {
			processParentOrderUpdateEvent((ParentOrderUpdateEvent) event);
		} else if (event instanceof SingleInstrumentStrategyUpdateEvent) {
			processSingleInstrumentStrategyUpdateEvent((SingleInstrumentStrategyUpdateEvent) event);
		} else if (event instanceof MultiInstrumentStrategyUpdateEvent) {
			processMultiInstrumentStrategyUpdateEvent((MultiInstrumentStrategyUpdateEvent) event);
		} else if (event instanceof AllStrategySnapshotReplyEvent) {
			processAllStrategySnapshotReplyEvent((AllStrategySnapshotReplyEvent) event);
		}
	}

	private void processAllStrategySnapshotReplyEvent(
			AllStrategySnapshotReplyEvent event) {
		if (!event.isOk()) {
			log.warn("processAllStrategySnapshotReplyEvent is not ok:{}",
					event.getMessage());
			return;
		}
		groupOrderCache.updateOrder(event.getOrders());
		groupOrderCache.updateInstrument(event.getInstruments());
		groupOrderCache.updateMultiInstrumentStrategyData(event
				.getStrategyData());
		if (Business.getInstance().getUserGroup().isAdmin()) {
			eventManager.subscribe(ParentOrderUpdateEvent.class, this);
		} else {
			subGroupEvent(Business.getInstance().getAccountGroup());
		}

		// setting ready
		setReady(true);
		eventManager.sendEvent(new OrderCacheReadyEvent(null));
	}

	private void subGroupEvent(List<String> accountList) {
		for (String id : accountList) {
			eventManager.unsubscribe(ParentOrderUpdateEvent.class, id, this);
			eventManager.subscribe(ParentOrderUpdateEvent.class, id, this);
			log.info("sub ParentOrderUpdateEvent:{}", id);
		}
	}

	public List<Map<String, Object>> getAllParentOrders() {
		return groupOrderCache.getAllParentOrders();
	}

	public List<ParentOrder> getAllParentOrderList() {
		return groupOrderCache.getAllParentOrderList();
	}
}
