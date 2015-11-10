package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCInstrumentModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/02
 *
 */
public final class InstrumentSnapshotReplyLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = -1495213212193955714L;

	private List<RCInstrumentModel> instrumentModelList;

	public InstrumentSnapshotReplyLocalEvent(
			List<RCInstrumentModel> instrumentModelList, String key) {
		super(key);
		this.instrumentModelList = instrumentModelList;
	}

	public List<RCInstrumentModel> getInstrumentModelList() {
		return instrumentModelList;
	}

}