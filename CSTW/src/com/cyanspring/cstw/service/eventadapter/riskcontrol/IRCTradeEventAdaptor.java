package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCTradeRecordModel;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCParentOrderUpdateCachingLocalEvent;

public interface IRCTradeEventAdaptor {

	List<RCTradeRecordModel> getTradeRecordModelListByOrderList(
			BasicRCParentOrderUpdateCachingLocalEvent event);

}
