/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.IOrderRecordService;
import com.cyanspring.cstw.service.localevent.riskmgr.OrderRecordsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OrderRecordsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OrderRecordsUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;

/**
 * @author Yu-Junfeng
 * @create 27 Aug 2015
 */
public class OrderRecordServiceImpl extends BasicServiceImpl implements
		IOrderRecordService {

	private List<RCOrderRecordModel> orderPendingList;

	private List<RCOrderRecordModel> orderActivityList;

	public OrderRecordServiceImpl() {
		orderPendingList = new ArrayList<RCOrderRecordModel>();
		orderActivityList = new ArrayList<RCOrderRecordModel>();
	}

	@Override
	public void queryOrder() {
		OrderRecordsSnapshotRequestLocalEvent event = new OrderRecordsSnapshotRequestLocalEvent();
		sendEvent(event);
	}

	@Override
	public void cancelOrder(String orderId) {
		CancelParentOrderEvent event = new CancelParentOrderEvent(orderId, server, orderId, false, null);
		sendEvent(event);
	}

	@Override
	public List<RCOrderRecordModel> getActivityOrderList() {
		return orderActivityList;
	}

	@Override
	public List<RCOrderRecordModel> getPendingOrderList() {
		return orderPendingList;
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(OrderRecordsUpdateLocalEvent.class);
		list.add(OrderRecordsSnapshotReplyLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof OrderRecordsUpdateLocalEvent) {
			OrderRecordsUpdateLocalEvent replyLocalEvent = (OrderRecordsUpdateLocalEvent) event;
			List<RCOrderRecordModel> list = replyLocalEvent.getOrderList();
			orderActivityList.clear();
			orderPendingList.clear();
			for (RCOrderRecordModel model : list) {
				if (model.isPending()) {
					orderPendingList.add(model);
				} else {
					orderActivityList.add(model);
				}
			}
		}
		if (event instanceof OrderRecordsSnapshotReplyLocalEvent) {
			OrderRecordsSnapshotReplyLocalEvent replyLocalEvent = (OrderRecordsSnapshotReplyLocalEvent) event;
			List<RCOrderRecordModel> list = replyLocalEvent.getOrderList();
			orderActivityList.clear();
			orderPendingList.clear();
			for (RCOrderRecordModel model : list) {
				if (model.isPending()) {
					orderPendingList.add(model);
				} else {
					orderActivityList.add(model);
				}
			}
		}
		return RefreshEventType.RWOrderRecordList;
	}

}
