package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import cn.com.wind.td.tdf.TDF_MARKET_DATA;
import cn.com.wind.td.tdf.TDF_MSG_ID;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteMgr implements IReqThreadCallback {

	private static final Logger log = LoggerFactory
			.getLogger(QuoteMgr.class);

	public static QuoteMgr instance = new QuoteMgr();
	Object m_lock = new Object();

	public static QuoteMgr instance() {
		return instance;
	}

	RequestThread thread = null;

	public void init() {
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

	public void AddRequest(Object reqObj) {
		thread.addRequest(reqObj);
	}

	public boolean checkFutureSymbol(String strSymbol) {
		return FutureItem.futureItemBySymbolMap.containsKey(strSymbol); //Future
	}

	public void addFutureSymbol(String symbol, String exchange) {
		if (checkFutureSymbol(symbol) == false) {
			FutureItem item = new FutureItem(symbol);
			item.setMarket(exchange);
			// item
			synchronized (m_lock) {
				FutureItem.futureItemBySymbolMap.put(symbol, item);
			}
		}
	}
	
	public boolean checkStockSymbol(String strSymbol) {
		return StockItem.stockItemBySymbolMap.containsKey(strSymbol); //Stock
	}

	public void addStockSymbol(String symbol, String exchange) {
		if (checkStockSymbol(symbol) == false) {
			StockItem item = new StockItem(symbol);
			item.setMarket(exchange);
			// item
			synchronized (m_lock) {
				StockItem.stockItemBySymbolMap.put(symbol, item);
			}
		}
	}	
	

	void process(int type, Object objMsg) {
		switch (type) {
		case TDF_MSG_ID.MSG_SYS_CODETABLE_RESULT:
			break;
		case TDF_MSG_ID.MSG_SYS_QUOTATIONDATE_CHANGE:
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
	}

	@Override
	public void onStopEvent(RequestThread sender) {

	}

}
