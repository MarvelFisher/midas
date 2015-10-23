/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCInstrumentModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/09/02
 *
 */
public final class InstrumentSummaryUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = -1093065513947000897L;
	
	private List<RCInstrumentModel> instrumentModelList;

	public InstrumentSummaryUpdateLocalEvent(
			List<RCInstrumentModel> instrumentModelList) {
		this.instrumentModelList = instrumentModelList;
	}

	public List<RCInstrumentModel> getInstrumentModelList() {
		return instrumentModelList;
	}

}
