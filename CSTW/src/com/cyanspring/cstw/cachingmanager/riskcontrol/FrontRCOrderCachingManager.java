package com.cyanspring.cstw.cachingmanager.riskcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.cstw.cachingmanager.BasicCachingManager;
import com.cyanspring.cstw.localevent.OrderCacheReadyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCParentOrderUpdateCachingLocalEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/24
 *
 */
public final class FrontRCOrderCachingManager extends BasicCachingManager {

	private Map<String, ParentOrder> orderMap;
	
	private static FrontRCOrderCachingManager instance;
	
	public static FrontRCOrderCachingManager getInstance() {
		if (instance == null) {
			instance = new FrontRCOrderCachingManager();
		}
		return instance;
	}
	
	private FrontRCOrderCachingManager() {
		super();
		orderMap = new HashMap<String, ParentOrder>();
		refreshData();		
	}

	private void refreshData() {
		orderMap.clear();
		List<ParentOrder> orders = business.getOrderManager().getAllParentOrderList();
		for(ParentOrder ord : orders) {
			orderMap.put(ord.getId(), ord);
		}		
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(OrderCacheReadyLocalEvent.class);
		list.add(ParentOrderUpdateEvent.class);
		return list;
	}

	@Override
	protected void processAsyncEvent(AsyncEvent event) {
		if (event instanceof OrderCacheReadyLocalEvent) {
			refreshData();
			sendParentOrderUpdateEvent();
		}
		if (event instanceof ParentOrderUpdateEvent) {
			ParentOrder parentOrder = ((ParentOrderUpdateEvent) event)
					.getOrder();
			log.info("ParentOrderCachingManager : Update parent order recieved: "
					+ parentOrder);
			if (business.getUserGroup().isManageeExist(parentOrder.getUser())){
				orderMap.put(parentOrder.getId(), parentOrder);
				sendParentOrderUpdateEvent();
			}			
			
		}

	}

	private void sendParentOrderUpdateEvent() {
		FrontRCParentOrderUpdateCachingLocalEvent event = new FrontRCParentOrderUpdateCachingLocalEvent(
				orderMap);
		business.getEventManager().sendEvent(event);

	}

}
