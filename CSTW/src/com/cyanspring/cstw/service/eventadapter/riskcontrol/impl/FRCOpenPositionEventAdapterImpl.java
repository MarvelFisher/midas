package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;

/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public final class FRCOpenPositionEventAdapterImpl implements
		IRCOpenPositionEventAdapter {

	@Override
	public List<RCOpenPositionModel> getOpenPositionModelListByEvent(
			BasicRCPositionUpdateCachingLocalEvent event) {
		Map<String, Map<String, OverallPosition>> accountPositionMap = event
				.getAccountPositionMap();
		List<RCOpenPositionModel> openPositionModelList = new ArrayList<RCOpenPositionModel>();
		RCOpenPositionModel positionModel = null;
		if (accountPositionMap == null) {
			return openPositionModelList;
		}
		for (Entry<String, Map<String, OverallPosition>> accountEntry : accountPositionMap
				.entrySet()) {
			for (Entry<String, OverallPosition> positionEntry : accountEntry
					.getValue().entrySet()) {
				positionModel = ModelTransfer
						.parseCurrentPositionRecord(positionEntry.getValue());
				if (positionModel != null) {
					openPositionModelList.add(positionModel);
				}
			}
		}
		return openPositionModelList;
	}

}
