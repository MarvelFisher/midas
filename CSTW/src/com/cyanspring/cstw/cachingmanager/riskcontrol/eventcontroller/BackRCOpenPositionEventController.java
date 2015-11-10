package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionSnapshotListRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.OpenPositionUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BackRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public class BackRCOpenPositionEventController implements IAsyncEventListener {

	private static BackRCOpenPositionEventController instance;

	private Business business;

	private IRCOpenPositionEventAdapter adapter;

	private List<RCOpenPositionModel> openPositionModelList;

	public static BackRCOpenPositionEventController getInstance() {
		if (instance == null) {
			instance = new BackRCOpenPositionEventController();
		}
		return instance;
	}

	private BackRCOpenPositionEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getBackRCOpenPositionEventAdapter();
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
		if (event instanceof FrontRCPositionUpdateCachingLocalEvent) {
			FrontRCPositionUpdateCachingLocalEvent updateLocalEvent = (FrontRCPositionUpdateCachingLocalEvent) event;
			openPositionModelList = adapter
					.getOpenPositionModelListByEvent(updateLocalEvent);
			sendPositionUpdateEvent();
		} else if (event instanceof OpenPositionSnapshotListRequestLocalEvent) {
			OpenPositionSnapshotListRequestLocalEvent request = (OpenPositionSnapshotListRequestLocalEvent) event;
			if (request.getRoleType() == UserRole.BackEndRiskManager) {
				sendPositionSnapshotListReplyEvent();
			}
		}
	}

	private void sendPositionSnapshotListReplyEvent() {
		OpenPositionSnapshotListReplyLocalEvent replyEvent = new OpenPositionSnapshotListReplyLocalEvent(
				openPositionModelList, UserRole.BackEndRiskManager.name());
		business.getEventManager().sendEvent(replyEvent);
	}

	private void sendPositionUpdateEvent() {
		OpenPositionUpdateLocalEvent updateEvent = new OpenPositionUpdateLocalEvent(
				UserRole.BackEndRiskManager.name());
		updateEvent.setAllPositionModelList(openPositionModelList);
		business.getEventManager().sendEvent(updateEvent);
	}

}
