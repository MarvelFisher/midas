package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public final class TradeRecordsSnapshotReplyLocalEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;
	private List<RCTradeRecordModel> tradeRecordModelList;

	public TradeRecordsSnapshotReplyLocalEvent(String key) {
		super(key);
		tradeRecordModelList = new ArrayList<RCTradeRecordModel>();
	}

	public List<RCTradeRecordModel> getTradeRecordModelList() {
		return tradeRecordModelList;
	}

	public void setTradeRecordModelList(
			List<RCTradeRecordModel> tradeRecordModelList) {
		this.tradeRecordModelList = tradeRecordModelList;
	}

}