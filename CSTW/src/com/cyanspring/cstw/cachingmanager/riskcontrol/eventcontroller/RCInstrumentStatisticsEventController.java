package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentStatisticsEventAdaptor;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentStatisticsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentStatisticsUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/02
 *
 */
public final class RCInstrumentStatisticsEventController implements
		IAsyncEventListener {

	private static RCInstrumentStatisticsEventController instance;

	private Business business;

	private IRCInstrumentStatisticsEventAdaptor adapter;

	private List<RCInstrumentModel> instrumentModeList;

	public static RCInstrumentStatisticsEventController getInstance() {
		if (instance == null) {
			instance = new RCInstrumentStatisticsEventController();
		}

		return instance;
	}

	private RCInstrumentStatisticsEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getFrontRCInstrumentStatisticsEventAdaptor();
		instrumentModeList = new ArrayList<RCInstrumentModel>();
	}

	public void init() {
		// 本地监听
		business.getEventManager().subscribe(
				FrontRCPositionUpdateCachingLocalEvent.class, this);

		business.getEventManager().subscribe(
				InstrumentStatisticsSnapshotRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		// 本地Event
		if (event instanceof FrontRCPositionUpdateCachingLocalEvent) {
			FrontRCPositionUpdateCachingLocalEvent updateLocalEvent = (FrontRCPositionUpdateCachingLocalEvent) event;
			instrumentModeList = adapter
					.getInstrumentModelListByRCEvent(updateLocalEvent);
			sendPositionUpdateEvent();
		}

		else if (event instanceof InstrumentStatisticsSnapshotRequestLocalEvent) {
			sendInstrumentSnapshotReply();
		}

	}

	private void sendPositionUpdateEvent() {
		InstrumentStatisticsUpdateLocalEvent updateEvent = new InstrumentStatisticsUpdateLocalEvent(
				instrumentModeList, UserRole.RiskManager.name());
		business.getEventManager().sendEvent(updateEvent);
	}

	private void sendInstrumentSnapshotReply() {
		InstrumentSnapshotReplyLocalEvent replyEvent = new InstrumentSnapshotReplyLocalEvent(
				instrumentModeList, UserRole.RiskManager.name());
		business.getEventManager().sendEvent(replyEvent);
	}

}
