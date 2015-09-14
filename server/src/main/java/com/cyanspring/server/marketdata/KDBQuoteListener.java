package com.cyanspring.server.marketdata;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.IQuoteListener;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.server.persistence.KDBPersistenceManager;

public class KDBQuoteListener implements IQuoteListener {
	private static final Logger log = LoggerFactory.getLogger(KDBQuoteListener.class);
	@Autowired
	private KDBPersistenceManager kdbPersistenceManager;

	private BlockingQueue<Quote> queue;
	private Thread quoteThread = new Thread(){
		@Override
		public void run(){
			List<Quote> quoteLst = new ArrayList<>();
			TimeThrottler throttler = new TimeThrottler(5000);
			log.info("Start to listen quotes");
			while(true) {
				try {
					Quote quote = queue.take();
					quoteLst.add(quote);
					queue.drainTo(quoteLst);
					kdbPersistenceManager.saveQuotes(quoteLst);
					quoteLst.clear();
					if (throttler.check())
						log.info("Queue size: " + queue.size());
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	;
	
	@Override
	public void init() throws Exception {
		queue = new LinkedBlockingQueue<>();
		quoteThread.start();
	}

	@Override
	public void uninit() throws Exception {
		
	}
	
	@Override
	public void onQuote(InnerQuote InnerQuote) {
		try {
			queue.put(InnerQuote.getQuote());
			if (!quoteThread.isAlive())
				quoteThread.start();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource) {

	}

	@Override
	public void onTrade(Trade trade) {

	}
}
