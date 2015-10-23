/**
 * 
 */
package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.riskmgr.ITradeRecordService;
import com.cyanspring.cstw.service.model.riskmgr.RCTradeRecordModel;

/**
 * @author Yu-Junfeng
 * @create 12 Aug 2015
 */
public final class TradeRecordServiceImpl extends BasicServiceImpl implements
		ITradeRecordService {

	private List<RCTradeRecordModel> tradeRecordList;

	public TradeRecordServiceImpl() {
		tradeRecordList = new ArrayList<RCTradeRecordModel>();
	}

	@Override
	public void queryTradeRecord() {
		//
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		
		return list;
	}

	@Override
	public List<RCTradeRecordModel> getTradeRecordModelList() {
		return tradeRecordList;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		
		return RefreshEventType.Default;
	}

}