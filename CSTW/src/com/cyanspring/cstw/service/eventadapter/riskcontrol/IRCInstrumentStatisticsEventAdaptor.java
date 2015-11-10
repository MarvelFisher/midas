package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;

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
