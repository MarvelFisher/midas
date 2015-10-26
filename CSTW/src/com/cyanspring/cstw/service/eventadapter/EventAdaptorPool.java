package com.cyanspring.cstw.service.eventadapter;

import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCOpenPositionEventAdapterImpl;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/26
 *
 */
public class EventAdaptorPool {

	private static IRCOpenPositionEventAdapter fRCPositionEventAdapter = new FRCOpenPositionEventAdapterImpl();
	private static IRCOpenPositionEventAdapter bRCPositionEventAdapter = new BRCOpenPositionEventAdapterImpl();

	public static IRCOpenPositionEventAdapter getfRCPositionEventAdapter() {
		return fRCPositionEventAdapter;
	}

	public static IRCOpenPositionEventAdapter getBackRCOpenPositionEventAdapter() {
		return bRCPositionEventAdapter;
	}

}
