package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCParentOrderUpdateCachingLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

public interface IRCTradeEventAdaptor {

	List<RCTradeRecordModel> getTradeRecordModelListByOrderList(
			BasicRCParentOrderUpdateCachingLocalEvent event);

}
