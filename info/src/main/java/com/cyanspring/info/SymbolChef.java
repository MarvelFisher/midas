package com.cyanspring.info;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.info.PriceHighLowType;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.PriceHighLow;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefData;

public class SymbolChef implements Runnable{
	private static final Logger log = LoggerFactory
			.getLogger(SymbolChef.class);
    private LinkedBlockingQueue<Quote> m_q = new LinkedBlockingQueue<Quote>();
    private ConcurrentHashMap<String, SymbolData> mapSymboldata = new ConcurrentHashMap<String, SymbolData>();
    private int m_queueMaxSize;
    private String strChefName = null;
    private Thread m_Thread = new Thread(this);
    private boolean suspended = true;
    
    public SymbolChef(String strChefName)
    {
    	this.strChefName = strChefName;
    	setSuspended(true);
    	m_Thread.start();
    }
    
    public void setSuspended(boolean suspended)
    {
    	this.suspended = suspended;
    }
    public boolean isSuspended()
    {
    	return this.suspended;
    }
    public void chefStart()
    {
    	setSuspended(false);
    }
    public void chefStop()
    {
    	setSuspended(true);
    }
    
    public void createSymbol(RefData refdata, CentralDbProcessor centraldb)
    {
    	SymbolData symbolData = mapSymboldata.get(refdata.getSymbol());
    	if (symbolData == null)
    	{
    		mapSymboldata.put(refdata.getSymbol(),
    				new SymbolData(refdata.getSymbol(), refdata.getExchange(), centraldb)) ;
    	}
    }
    public void clearSymbol()
    {
    	mapSymboldata.clear();
    }
    
    public List<HistoricalPrice> retrieveHistoricalPrice(
    		String type, String symbol, int dataCount)
    {
    	SymbolData symboldata = mapSymboldata.get(symbol);
    	if (symboldata == null)
    	{
    		return null;
    	}
		return symboldata.getHistoricalPrice(type, symbol, dataCount) ;
    }
    
    public PriceHighLow retrievePriceHighLow(String symbol, PriceHighLowType type)
    {
    	SymbolData symboldata = mapSymboldata.get(symbol);
    	if (symboldata == null)
    	{
    		return null;
    	}
    	return symboldata.getPriceHighLow(type);
    }
    
    public void onQuote(Quote quote)
    {
    	try 
    	{
    		if (quote == null)
    		{
    			return;
    		}
			m_q.put(quote);
		} 
    	catch (InterruptedException e) 
    	{
            log.error(strChefName, e);
		}
    }
    public void executeQuote(Quote quote)
    {
    	if (quote == null)
    	{
    		return;
    	}
    	SymbolData symboldata = mapSymboldata.get(quote.getSymbol());
    	if (symboldata == null)
    	{
    		return;
    	}
    	else
    	{
    		symboldata.parseQuote(quote);
    	}
		log.debug("Quote: " + quote);
    }

	public void getAllChartPrice()
	{
		for (Entry<String, SymbolData> entry : mapSymboldata.entrySet())
		{
			entry.getValue().resetMapHistorical();
			entry.getValue().getAllChartPrice();
		}
	}
	
	public SymbolData getSymbolData(String symbol)
	{
		return mapSymboldata.get(symbol);
	}

	@Override
	public void run() 
	{
		long lTimeOut = 50;
		Quote quote;
		while (true)
		{
			if (isSuspended())
			{
				try 
				{
					Thread.sleep(lTimeOut);
					continue;
				} 
				catch (InterruptedException e) 
				{
					log.error(strChefName, e);
				}
			}
			try 
			{
				quote = m_q.poll(lTimeOut, TimeUnit.MILLISECONDS);
			} 
			catch (InterruptedException e) 
			{
				quote = null;
				log.error(strChefName, e);
			}
            // 記錄queue的最高個數
            if (m_queueMaxSize < m_q.size())
            {
                m_queueMaxSize = m_q.size();
                log.info(strChefName + " QueueMaxSize=[" + m_queueMaxSize + "]");
            }
            if (quote != null)
            {
            	executeQuote(quote);
            }
		}
	}
	
	public void resetSymbolDataStat()
	{
		Iterator<Entry<String, SymbolData>> itr = mapSymboldata.entrySet().iterator();
		SymbolData symboldata;
		while (itr.hasNext())
		{
			ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry)itr.next();
			symboldata = (SymbolData)pair.getValue();
			symboldata.setWriteMin(true);
		}
	}
	
	public void insertSQL()
	{
		Iterator<Entry<String, SymbolData>> itr = mapSymboldata.entrySet().iterator();
		SymbolData symboldata;
		while (itr.hasNext())
		{
			ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry)itr.next();
			symboldata = (SymbolData)pair.getValue();
			if (symboldata.getMarket() == null)
			{
				log.warn(symboldata.getStrSymbol() + " get Market NULL");
				continue;
			}
			symboldata.insertSQLDate("D");
			symboldata.insertSQLDate("W");
			symboldata.insertSQLDate("M");
			symboldata.insertSQLTick("1");
			symboldata.insertSQLTick("R");
			symboldata.insertSQLTick("A");
			symboldata.insertSQLTick("Q");
			symboldata.insertSQLTick("H");
			symboldata.insertSQLTick("6");
			symboldata.insertSQLTick("T");
		}
	}

}
