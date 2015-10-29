/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.ITradeRecordService;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordUpdateEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

/**
 * @author Yu-Junfeng
 * @create 12 Aug 2015
 */
public final class TradeRecordServiceImpl extends BasicServiceImpl implements
		ITradeRecordService {

	private List<RCTradeRecordModel> tradeRecordList;

	public TradeRecordServiceImpl() {
		tradeRecordList = new ArrayList<RCTradeRecordModel>();
	}

	@Override
	public void queryTradeRecord() {
		TradeRecordsSnapshotRequestLocalEvent event = new TradeRecordsSnapshotRequestLocalEvent();
		sendEvent(event);
//		List<Map<String, Object>> orders = Business.getInstance().getOrderManager().getAllParentOrders();
//		for(Map<String, Object> fields : orders) {
//			ParentOrder parentOrder = new ParentOrder((HashMap<String, Object>) fields);
//		}
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(TradeRecordsSnapshotReplyLocalEvent.class);
		list.add(TradeRecordUpdateEvent.class);
		return list;
	}

	@Override
	public List<RCTradeRecordModel> getTradeRecordModelList() {
		return tradeRecordList;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof TradeRecordsSnapshotReplyLocalEvent) {
			TradeRecordsSnapshotReplyLocalEvent replyEvent = (TradeRecordsSnapshotReplyLocalEvent) event;
			tradeRecordList = replyEvent.getTradeRecordModelList();
			return RefreshEventType.RWTradeRecordList;
		}
		if (event instanceof TradeRecordUpdateEvent) {
			TradeRecordUpdateEvent replyEvent = (TradeRecordUpdateEvent) event;
			tradeRecordList = replyEvent.getTradeRecordModelList();
			return RefreshEventType.RWTradeRecordList;
		}
		return RefreshEventType.Default;
	}

}
