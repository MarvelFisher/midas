package com.cyanspring.cstw.service.localevent.riskmgr;

import com.cyanspring.common.event.AsyncEvent;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class TradeRecordsSnapshotRequestLocalEvent extends AsyncEvent {
	
	private static final long serialVersionUID = 1L;

	public TradeRecordsSnapshotRequestLocalEvent(String key) {
		super(key);
	}
		
}