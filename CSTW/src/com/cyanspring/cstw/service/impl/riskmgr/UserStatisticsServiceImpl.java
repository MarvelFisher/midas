/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.IUserStatisticsService;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsSnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsSnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.IndividualStatisticsUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;

/**
 * @author Yu-Junfeng
 * @create 18 Aug 2015
 */
public class UserStatisticsServiceImpl extends BasicServiceImpl implements
		IUserStatisticsService {

	private List<RCUserStatisticsModel> individualModelList;

	public UserStatisticsServiceImpl() {
		individualModelList = new ArrayList<RCUserStatisticsModel>();
	}

	@Override
	public void queryIndividualRecord() {
		IndividualStatisticsSnapshotRequestLocalEvent event = new IndividualStatisticsSnapshotRequestLocalEvent();
		sendEvent(event);
	}

	@Override
	public List<RCUserStatisticsModel> getIndividualRecordModelList() {
		return individualModelList;
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(IndividualStatisticsSnapshotReplyLocalEvent.class);
		list.add(IndividualStatisticsUpdateLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof IndividualStatisticsSnapshotReplyLocalEvent) {
			IndividualStatisticsSnapshotReplyLocalEvent replyEvent = (IndividualStatisticsSnapshotReplyLocalEvent) event;
			individualModelList = replyEvent
					.getIndividualStatisticsRecordList();
			return RefreshEventType.RWUserStatistics;
		}
		else if (event instanceof IndividualStatisticsUpdateLocalEvent) {
			IndividualStatisticsUpdateLocalEvent replyEvent = (IndividualStatisticsUpdateLocalEvent) event;
			individualModelList = replyEvent.getIndividualModelList();
			return RefreshEventType.RWUserStatistics;
		}
		return RefreshEventType.Default;
	}

}
