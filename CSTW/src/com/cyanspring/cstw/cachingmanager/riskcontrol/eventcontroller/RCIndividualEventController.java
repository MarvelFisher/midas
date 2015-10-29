package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCIndividualEventAdaptor;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/08
 *
 */
public final class RCIndividualEventController implements IAsyncEventListener {

	private static RCIndividualEventController instance;

	private List<RCUserStatisticsModel> individualStatisticsModelList;

	private IRCIndividualEventAdaptor adapter;

	private Business business;

	public static RCIndividualEventController getInstance() {
		if (instance == null) {
			instance = new RCIndividualEventController();
		}
		return instance;
	}

	private RCIndividualEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getRCIndividualEventAdaptor();
		individualStatisticsModelList = new ArrayList<RCUserStatisticsModel>();
	}

	public void init() {
		// 本地监听
		business.getEventManager().subscribe(
				FrontRCPositionUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				IndividualStatisticsSnapshotRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		// 本地Event
		if (event instanceof FrontRCPositionUpdateCachingLocalEvent) {
			FrontRCPositionUpdateCachingLocalEvent updateLocalEvent = (FrontRCPositionUpdateCachingLocalEvent) event;
			individualStatisticsModelList = adapter
					.getIndividualModelListByEvent(updateLocalEvent);
			sendIndividualStatisticsUpdateEvent();
		}

		if (event instanceof IndividualStatisticsSnapshotRequestLocalEvent) {
			
			sendIndividualStatisticsSnapshotReplyLocalEvent();
		}
	}

	private void sendIndividualStatisticsSnapshotReplyLocalEvent() {
		IndividualStatisticsSnapshotReplyLocalEvent replyEvent = new IndividualStatisticsSnapshotReplyLocalEvent(
				individualStatisticsModelList);
		business.getEventManager().sendEvent(replyEvent);
	}

	private void sendIndividualStatisticsUpdateEvent() {
		IndividualStatisticsUpdateLocalEvent updateEvent = new IndividualStatisticsUpdateLocalEvent(
				individualStatisticsModelList);
		business.getEventManager().sendEvent(updateEvent);
	}

}
