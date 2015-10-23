package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;

/**
 * @author GuoWei
 * @since 08/17/2015
 */
public final class OrderRecordsSnapshotReplyLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = -991843573516147356L;

	private List<RCOrderRecordModel> orderList;

	public OrderRecordsSnapshotReplyLocalEvent() {
		orderList = new ArrayList<RCOrderRecordModel>();
	}

	public List<RCOrderRecordModel> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<RCOrderRecordModel> orderList) {
		this.orderList = orderList;
	}

}