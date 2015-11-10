/**
 * 
 */
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
public final class InstrumentStatisticsUpdateLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 772025443465466917L;

	private List<RCInstrumentModel> instrumentModeList;

	public InstrumentStatisticsUpdateLocalEvent(
			List<RCInstrumentModel> instrumentModeList, String key) {
		super(key);
		this.instrumentModeList = instrumentModeList;
	}

	public List<RCInstrumentModel> getInstrumentModeList() {
		return instrumentModeList;
	}

}
