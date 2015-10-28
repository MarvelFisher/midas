package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/02
 *
 */
public interface IRCInstrumentStatisticsEventAdaptor {
	List<RCInstrumentModel> getInstrumentModelListByRCEvent(
			BasicRCPositionUpdateCachingLocalEvent event);
}
