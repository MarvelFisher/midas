/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.IInstrumentSummaryService;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummarySnapshotReplyLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummarySnapshotRequestLocalEvent;
import com.cyanspring.cstw.service.localevent.riskmgr.InstrumentSummaryUpdateLocalEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * @author Yu-Junfeng
 * @create 20 Aug 2015
 */
public final class InstrumentSummaryServiceImpl extends BasicServiceImpl
		implements IInstrumentSummaryService {

	private List<RCInstrumentModel> modelList;

	public InstrumentSummaryServiceImpl() {
		modelList = new ArrayList<RCInstrumentModel>();
	}

	@Override
	public void queryInstrument() {
		InstrumentSummarySnapshotRequestLocalEvent event = new InstrumentSummarySnapshotRequestLocalEvent();
		sendEvent(event);
	}

	@Override
	public List<RCInstrumentModel> getInstrumentModelList() {
		if (modelList == null) {
			modelList = new ArrayList<RCInstrumentModel>();
		}
		return modelList;
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(InstrumentSummarySnapshotReplyLocalEvent.class);
		list.add(InstrumentSummaryUpdateLocalEvent.class);
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		if (event instanceof InstrumentSummarySnapshotReplyLocalEvent) {
			InstrumentSummarySnapshotReplyLocalEvent replyEvent = (InstrumentSummarySnapshotReplyLocalEvent) event;
			modelList = replyEvent.getInstrumentModelList();
			return RefreshEventType.RWInstrumentSummary;
		} else if (event instanceof InstrumentSummaryUpdateLocalEvent) {
			InstrumentSummaryUpdateLocalEvent updateEvent = (InstrumentSummaryUpdateLocalEvent) event;
			modelList = updateEvent.getInstrumentModelList();
			return RefreshEventType.RWInstrumentSummary;
		}
		return RefreshEventType.Default;
	}

}
