package com.cyanspring.adaptor.future.wind;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.id.Library.Util.FinalizeHelper;

public class UserClient implements AutoCloseable {

	List<String> list = new ArrayList<String>();

	public List<String> getList() {
		return list;
	}

	public IMarketDataListener listener = null;

	/**
	 * 
	 * @param listener
	 */
	public UserClient(IMarketDataListener mdListener) {
		listener = mdListener;
	}

	/**
	 * add symbol to list
	 * 
	 * @param symbol
	 *            e.g. USDJPY
	 */
	public void addSymbol(String symbol) {
		synchronized (list) {
			if (!list.contains(symbol)) {
				list.add(symbol);
			}
		}
	}

	/**
	 * remove symbol from list
	 * 
	 * @param symbol
	 */
	public void removeSymbol(String symbol) {
		synchronized (list) {
			if (list.contains(symbol)) {
				list.remove(symbol);
			}
		}
	}

	/**
	 * check if list contains symbol
	 * 
	 * @param symbol
	 * @return
	 */
	public boolean isMySymbol(String symbol) {
		synchronized (list) {
			return list.contains(symbol);
		}
	}

	/**
	 * send quote info to listener
	 * 
	 * @param quote
	 */
	public void sendQuote(Quote quote,DataObject quoteExt) {

		if (isMySymbol(quote.getSymbol())) {
			//use in MDM proceessInnerQuoteEvent , Wind Future Adapter sourceid=101
			listener.onQuote(quote, 101); 
			listener.onQuoteExt(quoteExt, 101); 
		}
	}

	/**
	 * 
	 */
	void uninit() {

		if (list != null) {
			list.clear();
			list = null;
		}
		listener = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

}
