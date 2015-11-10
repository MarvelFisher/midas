package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.cstw.model.riskmgr.RCTradeRecordModel;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCParentOrderUpdateCachingLocalEvent;

/**
 * @author Yu-Junfeng
 * @create 12 Aug 2015
 */
public final class FRCTradeEventAdaptorImpl implements IRCTradeEventAdaptor {

	@Override
	public List<RCTradeRecordModel> getTradeRecordModelListByOrderList(
			BasicRCParentOrderUpdateCachingLocalEvent event) {
		List<RCTradeRecordModel> result = new ArrayList<RCTradeRecordModel>();
		Map<String, ParentOrder> orderMap = event.getOrderMap();
		if (orderMap == null || orderMap.isEmpty()) {
			return result;
		}

		for (ParentOrder order : orderMap.values()) {
			if (order.getOrdStatus() != OrdStatus.FILLED
					&& order.getOrdStatus() != OrdStatus.PARTIALLY_FILLED) {
				continue;
			}
			result.add(ModelTransfer.getRCTradeRecordModel(order));
		}
		return result;
	}
}
