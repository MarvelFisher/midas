package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;

/**
 * 股票汇总
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public interface IRCInstrumentSummaryEventAdaptor {

	List<RCInstrumentModel> getInstrumentSummaryModelListByEvent(
			BasicRCPositionUpdateCachingLocalEvent event);

}