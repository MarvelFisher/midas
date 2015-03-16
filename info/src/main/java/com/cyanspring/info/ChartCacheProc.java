package com.cyanspring.info;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartCacheProc implements Runnable 
{
	private static final Logger log = LoggerFactory
			.getLogger(ChartCacheProc.class);
    private LinkedBlockingQueue<SymbolData> m_q = new LinkedBlockingQueue<SymbolData>();
    private static final int periodTime = 500;
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
		SymbolData symboldata = null;
		while (true)
		{
			symboldata = m_q.poll();
			if (symboldata != null)
			{
				symboldata.writeToMin();
			}
			else
			{
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

}
