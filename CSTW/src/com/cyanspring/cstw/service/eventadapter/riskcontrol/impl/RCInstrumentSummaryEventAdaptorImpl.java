package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentSummaryEventAdaptor;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.BasicRCPositionUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * 
 * @author Guo Wei
 * @create date 2015/08/27
 *
 */
public final class RCInstrumentSummaryEventAdaptorImpl implements
		IRCInstrumentSummaryEventAdaptor {

	@Override
	public List<RCInstrumentModel> getInstrumentSummaryModelListByEvent(
			BasicRCPositionUpdateLocalEvent event) {
		List<OverallPosition> positionList = new ArrayList<OverallPosition>();
		for (Entry<String, Map<String, OverallPosition>> accountSymbolEntry : event
				.getAccountPositionMap().entrySet()) {
			for (Entry<String, OverallPosition> positionEntry : accountSymbolEntry
					.getValue().entrySet()) {
				positionList.add(positionEntry.getValue());
			}
		}
		return ModelTransfer.parseStockSummaryRecordList(positionList);
	}
}