package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentSummaryEventAdaptor;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummarySnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummarySnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummaryUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BackRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;

/**
 * 股票汇总
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/31
 *
 */
public final class RCInstrumentSummaryEventController implements
		IAsyncEventListener {

	private static RCInstrumentSummaryEventController instance;

	private Business business;

	private IRCInstrumentSummaryEventAdaptor adapter;

	private List<RCInstrumentModel> instrumentModeList;

	public static RCInstrumentSummaryEventController getInstance() {
		if (instance == null) {
			instance = new RCInstrumentSummaryEventController();
		}

		return instance;
	}

	private RCInstrumentSummaryEventController() {
		business = Business.getInstance();
		adapter = EventAdaptorPool.getRCinstrumentSummaryEventAdaptor();
		instrumentModeList = new ArrayList<RCInstrumentModel>();
	}

	public void init() {
		// 本地监听
		business.getEventManager().subscribe(
				BasicRCPositionUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				FrontRCPositionUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				BackRCPositionUpdateCachingLocalEvent.class, this);

		business.getEventManager().subscribe(
				InstrumentSummarySnapshotRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		// 本地Event
		if (event instanceof BasicRCPositionUpdateCachingLocalEvent) {
			BasicRCPositionUpdateCachingLocalEvent updateLocalEvent = (BasicRCPositionUpdateCachingLocalEvent) event;
			instrumentModeList = adapter
					.getInstrumentSummaryModelListByEvent(updateLocalEvent);
			sendPositionUpdateEvent();
		}

		else if (event instanceof InstrumentSummarySnapshotRequestLocalEvent) {
			sendInstrumentSnapshotReply();
		}

	}

	private void sendPositionUpdateEvent() {
		InstrumentSummaryUpdateLocalEvent updateEvent = new InstrumentSummaryUpdateLocalEvent(
				instrumentModeList);
		business.getEventManager().sendEvent(updateEvent);
	}

	private void sendInstrumentSnapshotReply() {
		InstrumentSummarySnapshotReplyLocalEvent replyEvent = new InstrumentSummarySnapshotReplyLocalEvent(
				instrumentModeList);
		business.getEventManager().sendEvent(replyEvent);
	}

}
