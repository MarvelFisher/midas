package com.cyanspring.cstw.cachingmanager.quote;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.cstw.cachingmanager.BasicCachingManager;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/16
 *
 */
public final class QuoteCachingManager extends BasicCachingManager {

	private static QuoteCachingManager instance;

	private List<IQuoteChangeListener> listenerList;

	private QuoteCachingManager() {
		listenerList = new ArrayList<IQuoteChangeListener>();
	}

	public static QuoteCachingManager getInstance() {
		if (instance == null) {
			instance = new QuoteCachingManager();
		}
		return instance;
	}

	public void addIQuoteChangeListener(IQuoteChangeListener listener) {
		listenerList.add(listener);
	}

	public void removeIQuoteChangeListener(IQuoteChangeListener listener) {
		listenerList.remove(listener);
	}

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		List<Class<? extends AsyncEvent>> list = new ArrayList<Class<? extends AsyncEvent>>();
		list.add(QuoteEvent.class);
		return list;
	}

	@Override
	protected void processAsyncEvent(AsyncEvent event) {
		if (event instanceof QuoteEvent) {
			QuoteEvent quoteEvent = (QuoteEvent) event;
			for (IQuoteChangeListener listener : listenerList) {
				Quote quote = quoteEvent.getQuote();
				if (listener.getQuoteSymbolSet().contains(quote.getSymbol())) {
					listener.refreshByQuote(quote);
				}
			}
		}
	}

}
