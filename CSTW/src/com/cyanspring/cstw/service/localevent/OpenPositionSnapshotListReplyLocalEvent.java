package com.cyanspring.cstw.service.localevent;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCOpenPositionModel;

/**
 * @author GuoWei
 * @since 08/03/2015
 */
public final class OpenPositionSnapshotListReplyLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 5874149809735860626L;

	private List<RCOpenPositionModel> positionList;	
	
	public OpenPositionSnapshotListReplyLocalEvent(
			List<RCOpenPositionModel> positionList, String key) {
		super(key);
		this.positionList = positionList;
	}

	public List<RCOpenPositionModel> getPositionList() {
		return positionList;
	}

}