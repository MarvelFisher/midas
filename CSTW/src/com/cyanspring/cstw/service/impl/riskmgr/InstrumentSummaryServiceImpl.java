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
		//
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
		
		return list;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		
		return RefreshEventType.Default;
	}

}
