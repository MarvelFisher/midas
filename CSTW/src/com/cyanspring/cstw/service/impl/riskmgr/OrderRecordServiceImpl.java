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
		// send event
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
		
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		
		return RefreshEventType.OrderRecordList4RC;
	}

}
