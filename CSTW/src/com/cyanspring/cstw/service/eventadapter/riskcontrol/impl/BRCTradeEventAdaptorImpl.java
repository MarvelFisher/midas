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
 * @create 14 Sep 2015
 */
public final class BRCTradeEventAdaptorImpl implements IRCTradeEventAdaptor {

	@Override
	public List<RCTradeRecordModel> getTradeRecordModelListByOrderList(
			BasicRCParentOrderUpdateCachingLocalEvent event) {
		List<RCTradeRecordModel> result = new ArrayList<RCTradeRecordModel>();
		Map<String, ParentOrder> orderMap = event.getOrderMap();
		if (orderMap == null) {
			return result;
		}
		//
		// filter no filled order
		for (ParentOrder trade : orderMap.values()) {
			if (trade.getOrdStatus() != OrdStatus.FILLED
					&& trade.getOrdStatus() != OrdStatus.PARTIALLY_FILLED) {
				continue;
			}
			RCTradeRecordModel model = ModelTransfer
					.getRCTradeRecordModel(trade);
			result.add(model);
		}

		return result;
	}
}
