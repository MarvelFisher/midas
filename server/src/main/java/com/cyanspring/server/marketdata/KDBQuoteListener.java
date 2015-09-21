package com.cyanspring.server.marketdata;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.statistic.AccountStatisticReplyEvent;
import com.cyanspring.common.marketdata.IKDBThrottling;
import com.cyanspring.common.marketdata.IQuoteListener;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.util.TimeThrottler;
import com.cyanspring.server.persistence.KDBPersistenceManager;

public class KDBQuoteListener implements IQuoteListener, IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(KDBQuoteListener.class);
	@Autowired
	private KDBPersistenceManager kdbPersistenceManager;
	private ScheduleManager scheduleManager = new ScheduleManager();
	
	private AsyncTimerEvent timerThrottlingEvent = new AsyncTimerEvent();
	private long quoteThrottlingInterval = 1000;
	private IKDBThrottling KdbThrottling = null;
	HashMap<String, Quote> quoteHold = new HashMap<String, Quote>();

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
					SaveQuotes(quoteLst);
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
	public void onEvent(AsyncEvent event) {
		if(event instanceof AsyncTimerEvent) {
			synchronized(quoteHold)
			{
				if(quoteHold.size() > 0)
				{
					kdbPersistenceManager.saveQuotes(quoteHold);
					//TODO saveQuotes
					quoteHold.clear();
				}
			}
		}else{
			log.error("Unhandle Event:{}",event.getClass().getSimpleName());
		}
	}
	
	private void SaveQuotes(List<Quote> Quotes)
	{
		
		if(null == KdbThrottling)
		{
			kdbPersistenceManager.saveQuotes(Quotes);
			return;
		}
		
		ArrayList<Quote> priorityQuotes = new ArrayList<Quote>();
		for(Quote q : Quotes)
		{
			synchronized(quoteHold)
			{
				if(quoteHold.containsKey(q.getSymbol()))
				{
					if(KdbThrottling.isNewQuote(q, quoteHold.get(q.getSymbol())))
					{
						priorityQuotes.add(quoteHold.get(q.getSymbol()));
					}
				}
				quoteHold.put(q.getSymbol(), q);
			}
		}
		
		if(priorityQuotes.size() > 0)
			kdbPersistenceManager.saveQuotes(priorityQuotes);
	}
	
	@Override
	public void init() throws Exception {
		queue = new LinkedBlockingQueue<>();
		quoteThread.start();
		if(null == KdbThrottling)
		{
			scheduleManager.scheduleRepeatTimerEvent(quoteThrottlingInterval,
					KDBQuoteListener.this, timerThrottlingEvent);
		}
	}

	@Override
	public void uninit() throws Exception {
		scheduleManager.uninit();
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

	public long getQuoteThrottlingInterval() {
		return quoteThrottlingInterval;
	}

	public void setQuoteThrottlingInterval(long quoteThrottlingInterval) {
		this.quoteThrottlingInterval = quoteThrottlingInterval;
	}
}
