package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.List;

import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.localevent.riskmgr.BasicRCPositionUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;

public final class FRCOpenPositionEventAdapterImpl implements
		IRCOpenPositionEventAdapter {

	@Override
	public List<RCOpenPositionModel> getOpenPositionModelListByEvent(
			BasicRCPositionUpdateLocalEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
