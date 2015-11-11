package com.cyanspring.common.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.DualMap;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public class ClosePositionLock {
	private static final Logger log = LoggerFactory
			.getLogger(ClosePositionLock.class);
	
	private DualMap<String, String> closePositionActionMap = new DualMap<String, String>(); //order id, account-symbol key
	private HashMap<String, PendingClosePositionRecord> recordMap = new HashMap<String, PendingClosePositionRecord>();
	private long timeout = 20000;
	
	private class PendingClosePositionRecord {
		ParentOrder order;
		double qty;
		double cumQty;
		public PendingClosePositionRecord(ParentOrder order) {
			this.order = order;
			this.qty = order.getQuantity();
		}
		
	}
	
	private String getClosePositionKey(String account, String symbol) {
		return account + "-" + symbol;
	}
	
	synchronized public void lockAccountPosition(ParentOrder order) throws AccountException {
		log.info("Lock postion: " + order.getAccount() + ", " + order.getSymbol() + ", " + order.getId());
		String result = closePositionActionMap.putIfAbsent(order.getId(), getClosePositionKey(order.getAccount(), order.getSymbol()));
		if(null == result)
			throw new AccountException("Failed to lock account: " + getClosePositionKey(order.getAccount(), order.getSymbol()) + ", " + order.getId());
		PendingClosePositionRecord record = 
				new PendingClosePositionRecord(order);
		recordMap.put(order.getId(), record);
	}
	
	synchronized public String unlockAccountPosition(String account, String symbol) {
		String result = closePositionActionMap.removeKeyByValue(getClosePositionKey(account, symbol));
		recordMap.remove(result);
		log.info("Unlock postion: " + account + ", " + symbol + ", " + result);
		return result;
	}
	
	public String unlockAccountPosition(String orderId) {
		String result = closePositionActionMap.remove(orderId);
		recordMap.remove(orderId);
		log.info("Unlock postion: " + result + ", " + orderId);
		return result;
	}
	
	public boolean checkAccountPositionLock(String account, String symbol) {
		return closePositionActionMap.containsValue(getClosePositionKey(account, symbol));
	}
	
	public Map<String, String> getPendingClosePositions() {
		return new HashMap<String, String>(closePositionActionMap.getMap());
	}
	
	synchronized public void processParentOrder(ParentOrder order ) {
		if(!closePositionActionMap.containsKey(order.getId())) {
			return;
		}
		
		PendingClosePositionRecord record = recordMap.get(order.getId());
		record.qty = order.getQuantity();
		
		if (order.getOrdStatus().isCompleted()) {
			record.qty = order.getCumQty();
			if(PriceUtils.EqualGreaterThan(record.cumQty, record.qty)) {
				log.debug("Close position order completed: " + order);
				unlockAccountPosition(order.getId());
				return;
			}
		} 
			
	}
	
	synchronized public void processExecution(Execution execution) {
		if(!closePositionActionMap.containsKey(execution.getStrategyId())) {
			return;
		}
		
		PendingClosePositionRecord record = recordMap.get(execution.getStrategyId());
		record.cumQty += execution.getQuantity();
		if(record.order.getOrdStatus().isCompleted() && PriceUtils.EqualGreaterThan(record.cumQty, record.qty)) {
			log.debug("Close position execution completed: " + execution.getStrategyId());
			unlockAccountPosition(execution.getStrategyId());
		}
	}
	
	synchronized List<ParentOrder> getTimeoutOrders() {
		List<ParentOrder> result = new ArrayList<ParentOrder>();
		for(PendingClosePositionRecord record: recordMap.values()) {
			if(TimeUtil.getTimePass(record.order.getCreated()) > this.timeout) {
				result.add(record.order);
			}
		}
		return result;
	}
}
