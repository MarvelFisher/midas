package com.cyanspring.cstw.service.eventadapter;

import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCIndividualEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentStatisticsEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCInstrumentSummaryEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOrderEventAdapter;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCTradeEventAdaptor;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.BRCTradeEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCInstrumentStatisticsEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCOpenPositionEventAdapterImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.FRCTradeEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.RCIndividualEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.RCInstrumentSummaryEventAdaptorImpl;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.impl.RCOrderEventAdapterImpl;

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
	private static IRCOrderEventAdapter fRCOrderEventAdapter = new RCOrderEventAdapterImpl();

	private static IRCInstrumentSummaryEventAdaptor rCinstrumentSummaryEventAdaptor = new RCInstrumentSummaryEventAdaptorImpl();
	private static IRCInstrumentStatisticsEventAdaptor fRCInstrumentEventAdaptor = new FRCInstrumentStatisticsEventAdaptorImpl();

	public static IRCOpenPositionEventAdapter getFrontRCOpenPositionEventAdapter() {
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

	public static IRCOrderEventAdapter getRCOrderEventAdapter() {
		return fRCOrderEventAdapter;
	}

	public static IRCInstrumentSummaryEventAdaptor getRCinstrumentSummaryEventAdaptor() {
		return rCinstrumentSummaryEventAdaptor;
	}

	public static IRCInstrumentStatisticsEventAdaptor getFrontRCInstrumentStatisticsEventAdaptor() {
		return fRCInstrumentEventAdaptor;
	}

}
