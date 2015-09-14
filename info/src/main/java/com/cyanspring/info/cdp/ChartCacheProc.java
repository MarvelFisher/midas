package com.cyanspring.info.cdp;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartCacheProc implements Runnable {
	private static final Logger log = LoggerFactory
			.getLogger(ChartCacheProc.class);
	private LinkedBlockingQueue<SymbolData> m_q = new LinkedBlockingQueue<SymbolData>();
	private static final int periodTime = 1000;
	private Thread m_Thread = new Thread(this);
	boolean threadSentinel = false;

	public ChartCacheProc() {
		threadSentinel = true;
		m_Thread.start();
	}

	public void put(SymbolData symboldata) {
		try {
			m_q.put(symboldata);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void clear() {
		m_q.clear();
	}

	public void exit() {
		threadSentinel = false;
	}

	@Override
	public void run() {
		SymbolData symboldata = null;
		while (threadSentinel) {
			do {
				symboldata = m_q.poll();
				if (symboldata != null) {
					symboldata.writeToMin();
				}
			} while (symboldata != null && threadSentinel);

			try {
				Thread.sleep(periodTime);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

	}

}
