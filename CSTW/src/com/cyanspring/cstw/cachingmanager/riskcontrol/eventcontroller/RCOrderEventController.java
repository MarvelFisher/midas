package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOrderEventAdapter;
import com.cyanspring.cstw.service.localevent.riskmgr.OrderRecordsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OrderRecordsUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCParentOrderUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/07
 *
 */
public final class RCOrderEventController implements IAsyncEventListener {

	private static RCOrderEventController instance;

	private Business business;

	private List<RCOrderRecordModel> orderRecordList;

	private IRCOrderEventAdapter adapter;

	public static RCOrderEventController getInstance() {
		if (instance == null) {
			instance = new RCOrderEventController();
		}
		return instance;
	}

	private RCOrderEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getRCOrderEventAdapter();
		orderRecordList = new ArrayList<RCOrderRecordModel>();
	}

	public void init() {
		// 本地监听
		business.getEventManager().subscribe(
				FrontRCParentOrderUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				OrderRecordsSnapshotRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof FrontRCParentOrderUpdateCachingLocalEvent) {
			FrontRCParentOrderUpdateCachingLocalEvent updateEvent = (FrontRCParentOrderUpdateCachingLocalEvent) event;
			orderRecordList = adapter
					.getOrderModelListByUpdateEvent(updateEvent);
			sendOrderRecordsUpdateLocalEvent();
		} else if (event instanceof OrderRecordsSnapshotRequestLocalEvent) {
			sendOrderRecordsUpdateLocalEvent();
		}
	}

	private void sendOrderRecordsUpdateLocalEvent() {
		OrderRecordsUpdateLocalEvent updateEvent = new OrderRecordsUpdateLocalEvent();
		updateEvent.setOrderList(orderRecordList);
		business.getEventManager().sendEvent(updateEvent);
	}

}
