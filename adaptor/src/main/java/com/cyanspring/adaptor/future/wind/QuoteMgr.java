package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.FutureData;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.id.Library.Threading.IReqThreadCallback;
import com.cyanspring.id.Library.Threading.RequestThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteMgr implements IReqThreadCallback {

	private static final Logger log = LoggerFactory
			.getLogger(QuoteMgr.class);

	public static QuoteMgr instance = new QuoteMgr();
	private static boolean isModifyTickTime = true;
	Object m_lock = new Object();

	public static QuoteMgr instance() {
		return instance;
	}

	public static boolean isModifyTickTime() {
		return isModifyTickTime;
	}

	public static void setModifyTickTime(boolean isModifyTickTime) {
		QuoteMgr.isModifyTickTime = isModifyTickTime;
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
		case WindDef.MSG_SYS_CODETABLE_RESULT:
			break;
		case WindDef.MSG_SYS_QUOTATIONDATE_CHANGE:
			break;
		case WindDef.MSG_DATA_FUTURE: {
			FutureData futureData = (FutureData) objMsg;
			FutureItem.processFutureData(futureData);
		}
			break;
		case WindDef.MSG_DATA_MARKET: {
			StockData stockData = (StockData) objMsg;
			StockItem.processMarketData(stockData);
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
