package com.cyanspring.cstw.service.eventadapter.riskcontrol;

import java.util.List;

import com.cyanspring.cstw.model.riskmgr.RCOrderRecordModel;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.FrontRCParentOrderUpdateCachingLocalEvent;

/**
 * 
 * @author GuoWei
 * @create date 2015/08/28
 *
 */
public interface IRCOrderEventAdapter {

	List<RCOrderRecordModel> getOrderModelListByUpdateEvent(
			FrontRCParentOrderUpdateCachingLocalEvent event);
}