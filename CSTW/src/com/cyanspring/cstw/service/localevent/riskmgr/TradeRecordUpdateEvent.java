/**
 * 
 */
package com.cyanspring.cstw.service.localevent.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.model.riskmgr.RCTradeRecordModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/08/27
 *
 */
public class TradeRecordUpdateEvent extends AsyncEvent {

	private static final long serialVersionUID = 1L;
	private List<RCTradeRecordModel> tradeRecordModelList;
	
	public TradeRecordUpdateEvent(String key) {
		super(key);
	}

	public List<RCTradeRecordModel> getTradeRecordModelList() {
		return tradeRecordModelList;
	}

	public void setTradeRecordModelList(
			List<RCTradeRecordModel> tradeRecordModelList) {
		this.tradeRecordModelList = tradeRecordModelList;
	}

}
