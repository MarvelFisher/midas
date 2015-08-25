package com.cyanspring.info.cdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBInsertProc implements Runnable 
{
	private static final Logger log = LoggerFactory
			.getLogger(DBInsertProc.class);
    private LinkedBlockingQueue<String> m_q = new LinkedBlockingQueue<String>();
    private static final int periodTime = 1000;
    private Thread m_Thread;
    
    private CentralDbProcessor centralDB = null;
	private boolean suspended = true;
	
	public DBInsertProc(CentralDbProcessor centralDB)
	{
		this.centralDB = centralDB;
		m_Thread = new Thread(this);
		m_Thread.setName("CDP_Insert_SQL");
		setSuspended(true);
		m_Thread.start();
	}
    
    public void put(String symbol)
    {
    	try 
    	{
			m_q.put(symbol);
			log.info("set Suspended false");
			setSuspended(false);
		} 
    	catch (InterruptedException e) 
		{
			log.error(e.getMessage(), e);
		}
    }
    
    public void clear()
    {
    	m_q.clear();
    }

	@Override
	public void run() 
	{
		String symbol = null;
		SymbolChef chef;
		SymbolData data;
		ArrayList<String> marketList = new ArrayList<String>();
		int index;
		while (true)
		{
			try
			{
				if (isSuspended() == false)
				{
					marketList.clear();
					do
					{
						symbol = m_q.poll();
						if (symbol != null)
						{
							chef = centralDB.getChefBySymbol(symbol);
							if (chef != null)
							{
								chef.insertSQL(symbol);
								data = chef.getSymbolData(symbol);
								if (data != null)
								{
									index = Collections.binarySearch(
											marketList, data.getMarket());
									if (index < 0)
									{
										marketList
												.add(~index, data.getMarket());
									}
								}
							}
							else
							{
								log.warn("null chef " + symbol);
								continue;
							}
						}
					} while (symbol != null);
					for (String market : marketList)
					{
						centralDB.getChartPriceByMarket(market);
					}
					log.info("set Suspended true");
					setSuspended(true);
				}
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
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

	public boolean isSuspended()
	{
		return suspended;
	}

	public void setSuspended(boolean suspended)
	{
		this.suspended = suspended;
	}
}
