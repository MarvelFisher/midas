package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;

/**
 * @author GuoWei
 * @since 08/13/2015
 */
public class InstrumentSummarySnapshotReplyLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = -3452293957081261616L;

	private List<RCInstrumentModel> instrumentModelList;

	public InstrumentSummarySnapshotReplyLocalEvent(
			List<RCInstrumentModel> instrumentModelList) {
		this.instrumentModelList = instrumentModelList;
	}

	public List<RCInstrumentModel> getInstrumentModelList() {
		return instrumentModelList;
	}

}