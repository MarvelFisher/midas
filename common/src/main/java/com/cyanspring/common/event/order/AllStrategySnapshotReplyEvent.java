package com.cyanspring.common.event.order;

import java.util.List;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllStrategySnapshotReplyEvent extends RemoteAsyncEvent{
	
	private boolean isOk;
	private String message;
	private List<ParentOrder> orders;
	private List<Instrument> instruments;
	private List<MultiInstrumentStrategyData> strategyData;
	public AllStrategySnapshotReplyEvent(String key, String receiver,List<ParentOrder> orders
			,List<Instrument> instruments,List<MultiInstrumentStrategyData> strategyData,boolean isOk
			,String message) {
		super(key, receiver);
		this.orders = orders;
		this.instruments = instruments;
		this.strategyData = strategyData;
		this.isOk = isOk;
		this.message = message;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMessage() {
		return message;
	}
	public List<ParentOrder> getOrders() {
		return orders;
	}
	public List<Instrument> getInstruments() {
		return instruments;
	}
	public List<MultiInstrumentStrategyData> getStrategyData() {
		return strategyData;
	}
}
