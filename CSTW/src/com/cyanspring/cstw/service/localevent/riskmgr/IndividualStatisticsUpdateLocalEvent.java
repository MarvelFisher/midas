/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCUserStatisticsModel;

/**
 * @author Yu-Junfeng
 * @create 27 Aug 2015
 */
public class IndividualStatisticsUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;
	private List<RCUserStatisticsModel> individualModelList;
	
	public IndividualStatisticsUpdateLocalEvent(List<RCUserStatisticsModel> modelList) {
		individualModelList = modelList;
	}

	public List<RCUserStatisticsModel> getIndividualModelList() {
		return individualModelList;
	}
	
}
