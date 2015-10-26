package com.cyanspring.cstw.service.eventadapter;

import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCIndividualEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCTradeEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCTradeEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.RCIndividualEventAdaptorImpl;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/26
 *
 */
public final class EventAdaptorPool {

	private static IRCOpenPositionEventAdapter fRCPositionEventAdapter = new FRCOpenPositionEventAdapterImpl();
	private static IRCOpenPositionEventAdapter bRCPositionEventAdapter = new BRCOpenPositionEventAdapterImpl();
	private static IRCIndividualEventAdaptor rcIndividualEventAdaptor = new RCIndividualEventAdaptorImpl();
	private static IRCTradeEventAdaptor bRCTradeRecordEvent4RCManager = new BRCTradeEventAdaptorImpl();
	private static IRCTradeEventAdaptor fRCTradeRecordEvent4RCManager = new FRCTradeEventAdaptorImpl();

	public static IRCOpenPositionEventAdapter getfRCPositionEventAdapter() {
		return fRCPositionEventAdapter;
	}

	public static IRCOpenPositionEventAdapter getBackRCOpenPositionEventAdapter() {
		return bRCPositionEventAdapter;
	}

	public static IRCTradeEventAdaptor getFrontRCTradeEventAdaptor() {
		return fRCTradeRecordEvent4RCManager;
	}

	public static IRCTradeEventAdaptor getBackRCTradeEventAdaptor() {
		return bRCTradeRecordEvent4RCManager;
	}

	public static IRCIndividualEventAdaptor getRCIndividualEventAdaptor() {
		return rcIndividualEventAdaptor;
	}

}
