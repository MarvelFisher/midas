package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class FrontRCOpenPositionEventController implements
		IAsyncEventListener {

	private static FrontRCOpenPositionEventController instance;

	private Business business;

	private List<RCOpenPositionModel> openPositionModelList;

	private IRCOpenPositionEventAdapter adapter;

	public static FrontRCOpenPositionEventController getInstance() {
		if (instance == null) {
			instance = new FrontRCOpenPositionEventController();
		}
		return instance;
	}

	private FrontRCOpenPositionEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getFrontRCOpenPositionEventAdapter();
		openPositionModelList = new ArrayList<RCOpenPositionModel>();
	}

	public void init() {
		// 本地监听
		business.getEventManager().subscribe(
				FrontRCPositionUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				OpenPositionSnapshotListRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		// 本地Event
		if (event instanceof FrontRCPositionUpdateCachingLocalEvent) {
			FrontRCPositionUpdateCachingLocalEvent updateLocalEvent = (FrontRCPositionUpdateCachingLocalEvent) event;
			openPositionModelList = adapter
					.getOpenPositionModelListByEvent(updateLocalEvent);
			sendPositionUpdateEvent();
		}

		if (event instanceof OpenPositionSnapshotListRequestLocalEvent) {
			OpenPositionSnapshotListRequestLocalEvent request = (OpenPositionSnapshotListRequestLocalEvent) event;
			if (request.getRoleType() == UserRole.RiskManager) {
				sendPositionSnapshotListReplyEvent();
			}
		}
	}

	private void sendPositionSnapshotListReplyEvent() {
		OpenPositionSnapshotListReplyLocalEvent replyEvent = new OpenPositionSnapshotListReplyLocalEvent(
				openPositionModelList, UserRole.RiskManager.name());
		business.getEventManager().sendEvent(replyEvent);
	}

	private void sendPositionUpdateEvent() {
		OpenPositionUpdateLocalEvent updateEvent = new OpenPositionUpdateLocalEvent(
				UserRole.RiskManager.name());
		updateEvent.setAllPositionModelList(openPositionModelList);
		business.getEventManager().sendEvent(updateEvent);
	}
}
