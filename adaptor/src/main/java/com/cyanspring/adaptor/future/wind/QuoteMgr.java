package com.cyanspring.adaptor.future.wind;

import java.util.List;

import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_MSG_ID;
import cn.com.wind.td.tdf.TDF_QUOTATIONDATE_CHANGE;

import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;

public class QuoteMgr implements IReqThreadCallback{

	public static QuoteMgr instance = new QuoteMgr();
	
	RequestThread thread = null;
	
	public void init(){
		if (thread == null) {
			thread = new RequestThread(this, "QuoteMgr");
			thread.start();
		}		
	}
	
	public void uninit() {
		if (thread != null) {
			thread.close();
			thread = null;
		}
	}
	public void AddRequest(Object  reqObj) {
		thread.addRequest(reqObj);
	}
	
	void process(int type, Object objMsg) {
		WindFutureDataAdaptor adaptor = WindFutureDataAdaptor.instance;
		switch (type) {
		case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT: {

			List<SymbolInfo> list = adaptor.updateCodeTable((String) objMsg);
			adaptor.sendSymbolInfo(list);
		}
			break;
		/*
		 * case TDF_MSG_ID.MSG_SYS_DISCONNECT_NETWORK: { try {
		 * Thread.sleep(1000); } catch (InterruptedException e) { }
		 * 
		 * if (!isClosed) { connect(); } } break;
		 */
		case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE: {
			TDF_QUOTATIONDATE_CHANGE change = (TDF_QUOTATIONDATE_CHANGE) objMsg;
			WindFutureDataAdaptor.info("%s, quotation change from %d to %d", change.getMarket(),
					change.getOldDate(), change.getNewDate());

			adaptor.updateCodeTable(change.getMarket());
		}
			break;
		case TDF_MSG_ID.MSG_DATA_FUTURE: {
			TDF_FUTURE_DATA future = (TDF_FUTURE_DATA) objMsg;
			FutureItem.processFutureData(future);
		}
			break;
		case TDF_MSG_ID.MSG_DATA_MARKET: {
			TDF_MARKET_DATA market = (TDF_MARKET_DATA) objMsg;
			StockItem.processMarketData(market);
		}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onStartEvent(RequestThread sender) {
		
	}

	@Override
	public void onRequestEvent(RequestThread sender, Object reqObj) {
		Object[] arr = (Object[]) reqObj;
		if (arr == null || arr.length != 2) {
			return;
		}
		int type = (int) arr[0];
		process(type, arr[1]);
		
		arr = null;
		reqObj = null;	
	}

	@Override
	public void onStopEvent(RequestThread sender) {

		
	}

}
