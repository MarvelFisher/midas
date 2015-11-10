package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCUserStatisticsModel;

/**
 * @author GuoWei
 * @since 08/13/2015
 */
public final class IndividualStatisticsSnapshotReplyLocalEvent extends
		AsyncEvent {

	private static final long serialVersionUID = -4210136368162584793L;

	private List<RCUserStatisticsModel> individualStatisticsRecordList;

	public IndividualStatisticsSnapshotReplyLocalEvent(List<RCUserStatisticsModel> modelList) {
		individualStatisticsRecordList = modelList;
	}

	public List<RCUserStatisticsModel> getIndividualStatisticsRecordList() {
		return individualStatisticsRecordList;
	}
}