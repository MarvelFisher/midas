/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel;

/**
 * @author Yu-Junfeng
 * @create 27 Aug 2015
 */
public class OrderRecordsUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;
	
	private List<RCOrderRecordModel> orderList;

	public OrderRecordsUpdateLocalEvent() {
	}

	public List<RCOrderRecordModel> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<RCOrderRecordModel> orderList) {
		this.orderList = orderList;
	}

}
