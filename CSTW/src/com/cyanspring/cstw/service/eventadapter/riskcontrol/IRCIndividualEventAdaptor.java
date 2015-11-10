package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCUserStatisticsModel;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCPositionUpdateCachingLocalEvent;

/**
 * @author Yu-Junfeng
 * @create 18 Aug 2015
 */
public interface IRCIndividualEventAdaptor {

	List<RCUserStatisticsModel> getIndividualModelListByEvent(
			FrontRCPositionUpdateCachingLocalEvent event);

}
