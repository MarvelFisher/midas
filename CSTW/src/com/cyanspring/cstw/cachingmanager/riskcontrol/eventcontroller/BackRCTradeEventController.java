package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.localevent.riskmgr.BackRCParentOrderUpdateLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordUpdateEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public class BackRCTradeEventController implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(BackRCTradeEventController.class);

	private static BackRCTradeEventController instance;

	private Business business;

	private List<RCTradeRecordModel> tradeRecordModelList;

	private IRCTradeEventAdaptor adaptor;

	public static BackRCTradeEventController getInstance() {
		if (instance == null) {
			instance = new BackRCTradeEventController();
		}
		return instance;
	}

	private BackRCTradeEventController() {
		business = Business.getInstance();
		adaptor = EventAdaptorPool.getBackRCTradeEventAdaptor();
		tradeRecordModelList = new ArrayList<RCTradeRecordModel>();
	}

	public void init() {
		log.info("FrontRCTradeCachingManager init...");
		// 本地监听
		business.getEventManager().subscribe(
				BackRCParentOrderUpdateLocalEvent.class, this);
		business.getEventManager().subscribe(
				TradeRecordsSnapshotRequestLocalEvent.class, this);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof BackRCParentOrderUpdateLocalEvent) {
			BackRCParentOrderUpdateLocalEvent updateLocalEvent = (BackRCParentOrderUpdateLocalEvent) event;
			tradeRecordModelList = adaptor
					.getTradeRecordModelListByOrderList(updateLocalEvent);
			sendTradeRecordsUpdateEvent();
		} else if (event instanceof TradeRecordsSnapshotRequestLocalEvent) {
			sendTradeRecordsSnapshotReplyLocalEvent();
		}
	}

	private void sendTradeRecordsSnapshotReplyLocalEvent() {
		TradeRecordsSnapshotReplyLocalEvent replyEvent = new TradeRecordsSnapshotReplyLocalEvent(
				UserRole.BackEndRiskManager.name());
		replyEvent.setTradeRecordModelList(tradeRecordModelList);
		business.getEventManager().sendEvent(replyEvent);
	}

	private void sendTradeRecordsUpdateEvent() {
		TradeRecordUpdateEvent replyEvent = new TradeRecordUpdateEvent(
				UserRole.BackEndRiskManager.name());
		replyEvent.setTradeRecordModelList(tradeRecordModelList);
		business.getEventManager().sendEvent(replyEvent);
	}

}
