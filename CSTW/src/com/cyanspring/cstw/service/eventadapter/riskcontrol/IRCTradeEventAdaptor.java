package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.service.localevent.riskmgr.BasicRCParentOrderUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

public interface IRCTradeEventAdaptor {

	List<RCTradeRecordModel> getTradeRecordModelListByOrderList(
			BasicRCParentOrderUpdateLocalEvent event);

}
