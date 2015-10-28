package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentStatisticsEventAdaptor;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/26
 *
 */
public class FRCInstrumentStatisticsEventAdaptorImpl implements
		IRCInstrumentStatisticsEventAdaptor {

	@Override
	public List<RCInstrumentModel> getInstrumentModelListByRCEvent(
			BasicRCPositionUpdateCachingLocalEvent event) {
		Map<String, Map<String, OverallPosition>> positionMap = event
				.getAccountPositionMap();
		List<RCInstrumentModel> modelList = new ArrayList<RCInstrumentModel>();
		RCInstrumentModel model = null;
		for (Entry<String, Map<String, OverallPosition>> accountEntry : positionMap
				.entrySet()) {
			for (Entry<String, OverallPosition> symbolEntry : accountEntry
					.getValue().entrySet()) {
				model = ModelTransfer.parseStockStatisticModel(symbolEntry
						.getValue());
				if (model != null) {
					modelList.add(model);
				}
			}
		}
		return modelList;
	}
}
