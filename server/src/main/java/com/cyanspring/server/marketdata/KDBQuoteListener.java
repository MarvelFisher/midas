package com.cyanspring.server.marketdata;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.IQuoteListener;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.server.persistence.KDBPersistenceManager;

public class KDBQuoteListener implements IQuoteListener {
	private static final Logger log = LoggerFactory.getLogger(KDBQuoteListener.class);
	@Autowired
	private KDBPersistenceManager kdbPersistenceManager;

	private BlockingQueue<InnerQuote> queue;
	private Thread quoteThread = new Thread(){
		@Override
		public void run(){
			log.info("Start to listen quotes");
			while(true) {
				try {
					InnerQuote quote = queue.take();
					List<InnerQuote> quoteLst = new ArrayList<>();
					queue.drainTo(quoteLst);
					quoteLst.add(quote);
					for (InnerQuote q : quoteLst) {
						kdbPersistenceManager.saveQuote(q);
					}
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
			queue.put(InnerQuote);
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
