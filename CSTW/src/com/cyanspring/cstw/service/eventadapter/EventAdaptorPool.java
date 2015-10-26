package com.cyanspring.cstw.service.eventadapter;

import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCTradeEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCOpenPositionEventAdapterImpl;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/26
 *
 */
public final class EventAdaptorPool {

	private static IRCOpenPositionEventAdapter fRCPositionEventAdapter = new FRCOpenPositionEventAdapterImpl();
	private static IRCOpenPositionEventAdapter bRCPositionEventAdapter = new BRCOpenPositionEventAdapterImpl();

	private static IRCTradeEventAdaptor bRCTradeRecordEvent4RCManager = new BRCTradeEventAdaptorImpl();

	public static IRCOpenPositionEventAdapter getfRCPositionEventAdapter() {
		return fRCPositionEventAdapter;
	}

	public static IRCOpenPositionEventAdapter getBackRCOpenPositionEventAdapter() {
		return bRCPositionEventAdapter;
	}

	public static IRCTradeEventAdaptor getBackRCTradeEventAdaptor() {
		return bRCTradeRecordEvent4RCManager;
	}

}
