package com.cyanspring.info;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartCacheProc implements Runnable 
{
	private static final Logger log = LoggerFactory
			.getLogger(ChartCacheProc.class);
    private LinkedBlockingQueue<SymbolData> m_q = new LinkedBlockingQueue<SymbolData>();
    private static final int periodTime = 1000;
    private Thread m_Thread = new Thread(this);
    
    public ChartCacheProc()
    {
    	m_Thread.start();
    }
    
    public void put(SymbolData symboldata)
    {
    	try 
    	{
			m_q.put(symboldata);
		} 
    	catch (InterruptedException e) 
		{
			log.error(e.getMessage(), e);
		}
    }

	@Override
	public void run() 
	{
		while (true)
		{
			if (!m_q.isEmpty())
			{
				SymbolData symboldata = m_q.poll();
				symboldata.writeToMin();
			}
			try 
			{
				Thread.sleep(periodTime);
			} 
			catch (InterruptedException e) 
			{
				log.error(e.getMessage(), e);
			}
		}

	}

}
