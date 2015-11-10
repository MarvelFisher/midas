package com.cyanspring.cstw.cachingmanager.riskcontrol.eventcontroller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.model.riskmgr.RCTradeRecordModel;
import com.cyanspring.cstw.service.eventadapter.EventAdaptorPool;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordUpdateEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.TradeRecordsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCParentOrderUpdateCachingLocalEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class RCTradeEventController implements IAsyncEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(RCTradeEventController.class);

	private static RCTradeEventController instance;

	private Business business;

	private List<RCTradeRecordModel> tradeRecordModelList;

	private IRCTradeEventAdaptor adaptor;

	public static RCTradeEventController getInstance() {
		if (instance == null) {
			instance = new RCTradeEventController();
		}
		return instance;
	}

	private RCTradeEventController() {
		business = Business.getInstance();
		adaptor = EventAdaptorPool.getFrontRCTradeEventAdaptor();
		tradeRecordModelList = new ArrayList<RCTradeRecordModel>();
	}

	public void init() {
		log.info("FrontRCTradeCachingManager init...");
		// 本地监听
		business.getEventManager().subscribe(
				FrontRCParentOrderUpdateCachingLocalEvent.class, this);
		business.getEventManager().subscribe(
				TradeRecordsSnapshotRequestLocalEvent.class, this);
	}

	private void sendTradeRecordsSnapshotReplyLocalEvent() {
		TradeRecordsSnapshotReplyLocalEvent replyEvent = new TradeRecordsSnapshotReplyLocalEvent(
				UserRole.RiskManager.name());
		replyEvent.setTradeRecordModelList(tradeRecordModelList);
		business.getEventManager().sendEvent(replyEvent);
	}

	private void sendTradeRecordsUpdateEvent() {
		TradeRecordUpdateEvent replyEvent = new TradeRecordUpdateEvent(
				UserRole.RiskManager.name());
		replyEvent.setTradeRecordModelList(tradeRecordModelList);
		business.getEventManager().sendEvent(replyEvent);
	}

	@Override
	public void onEvent(AsyncEvent event) {
		// 处理本地事件
		if (event instanceof FrontRCParentOrderUpdateCachingLocalEvent) {
			FrontRCParentOrderUpdateCachingLocalEvent replyEvent = (FrontRCParentOrderUpdateCachingLocalEvent) event;
			tradeRecordModelList = adaptor
					.getTradeRecordModelListByOrderList(replyEvent);
			sendTradeRecordsUpdateEvent();
		}
		if (event instanceof TradeRecordsSnapshotRequestLocalEvent) {
			sendTradeRecordsSnapshotReplyLocalEvent();
		}

	}

}
