package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCIndividualEventAdaptor;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;

/**
 * @author GuoWei
 * @since 09/08/2015
 */
public final class RCIndividualEventAdaptorImpl implements
		IRCIndividualEventAdaptor {

	@Override
	public List<RCUserStatisticsModel> getIndividualModelListByEvent(
			FrontRCPositionUpdateCachingLocalEvent event) {
		Map<String, Map<String, OverallPosition>> accountPositionMap = event
				.getAccountPositionMap();
		List<RCUserStatisticsModel> individualModelList = new ArrayList<RCUserStatisticsModel>();
		RCUserStatisticsModel model = null;
		if (accountPositionMap != null && !accountPositionMap.isEmpty()) {
			for (Entry<String, Map<String, OverallPosition>> accountEntry : accountPositionMap
					.entrySet()) {
				model = ModelTransfer
						.parseRCIndividualStatisticsModel(accountEntry
								.getValue());
				if (model != null) {
					individualModelList.add(model);
				}
			}
		}
		return individualModelList;
	}
}
