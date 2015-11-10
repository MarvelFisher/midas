package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOrderEventAdapter;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCParentOrderUpdateCachingLocalEvent;

/**
 * @author GuoWei
 * @since 08/28/2015
 */
public final class RCOrderEventAdapterImpl implements IRCOrderEventAdapter {

	@Override
	public List<RCOrderRecordModel> getOrderModelListByUpdateEvent(
			FrontRCParentOrderUpdateCachingLocalEvent event) {
		Map<String, ParentOrder> orderMap = event.getOrderMap();
		if (orderMap == null || orderMap.isEmpty()) {
			return new ArrayList<RCOrderRecordModel>();
		}
		List<RCOrderRecordModel> orderRecordList = ModelTransfer
				.parseOrderRecordList(new ArrayList<ParentOrder>(orderMap
						.values()));
		return orderRecordList;
	}
}