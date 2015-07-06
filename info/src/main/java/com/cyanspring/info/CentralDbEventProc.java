package com.cyanspring.info;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.info.HistoricalPriceEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestDateEvent;
import com.cyanspring.common.event.info.HistoricalPriceRequestEvent;
import com.cyanspring.common.event.info.PriceHighLowEvent;
import com.cyanspring.common.event.info.PriceHighLowRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeRequestEvent;
import com.cyanspring.common.event.info.SymbolListSubscribeType;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.marketdata.HistoricalPrice;
import com.cyanspring.common.marketdata.PriceHighLow;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;

public class CentralDbEventProc implements Runnable 
{
	private static final Logger log = LoggerFactory
			.getLogger(CentralDbProcessor.class);
    private Thread m_Thread;
    private CentralDbProcessor centraldb = null;
    private LinkedBlockingQueue<RemoteAsyncEvent> m_q = new LinkedBlockingQueue<RemoteAsyncEvent>();
    private String strName = "";
    
    public CentralDbEventProc(CentralDbProcessor centraldb, String strName)
    {
    	this.strName = strName;
    	this.centraldb = centraldb;
    	m_Thread = new Thread(this);
    	m_Thread.setName(strName);
    	m_Thread.start();
    }
    public void onEvent(RemoteAsyncEvent event)
    {
    	try 
    	{
    		if (event == null)
    		{
    			return;
    		}
			m_q.put(event);
		} 
    	catch (InterruptedException e) 
    	{
            log.error(strName, e);
		}
    }
    
    public void processHistoricalPriceRequestEvent(HistoricalPriceRequestEvent event)
	{
		String symbol = event.getSymbol() ;
		HistoricalPriceEvent retEvent = new HistoricalPriceEvent(null, event.getSender());
		retEvent.setSymbol(symbol);
		log.info("Process Historical Price Request");
		String type   = event.getHistoryType() ;
		int dataCount = event.getDataCount(); 
		List<HistoricalPrice> listPrice = null;
		log.debug("Process Historical Price Request Symbol: " + symbol + " Type: " + type + " DataCounts: " + dataCount);
		
		listPrice = centraldb.getChefBySymbol(symbol).retrieveHistoricalPrice(type, symbol, dataCount);
		if (listPrice == null)
		{
			retEvent.setOk(false) ;
//			retEvent.setMessage("Get price list fail");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.DATA_NOT_FOUND, "Get price list fail"));
			centraldb.sendEvent(retEvent) ;
			log.debug("Process Historical Price Request fail: Get price list fail");
			return ;
		}
		else
		{
			retEvent.setOk(true) ;
			retEvent.setHistoryType(event.getHistoryType());
			retEvent.setDataCount(listPrice.size());
			retEvent.setPriceList(listPrice);
			centraldb.sendEvent(retEvent) ;
			log.info("Process Historical Price Request success Symbol: " + listPrice.size());
			return ;
		}
	}
    
    public void processHistoricalPriceRequestDateEvent(HistoricalPriceRequestDateEvent event)
    {
		String symbol = event.getSymbol() ;
		HistoricalPriceEvent retEvent = new HistoricalPriceEvent(null, event.getSender());
		retEvent.setSymbol(symbol);
		log.info("Process Historical Price Request");
		String type   = event.getHistoryType() ;
		String startDate = event.getStartDate();
		String endDate = event.getEndDate();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<HistoricalPrice> listPrice = null;
		SymbolData symboldata = centraldb.getChefBySymbol(symbol).getSymbolData(symbol);
		log.debug("Process Historical Price Request Symbol by Date: " + symbol + " Type: " + type + " Start: " + startDate + " End: " + endDate);
		
		try 
		{
			listPrice = centraldb.getDbhnd().getPeriodStartEndValue(centraldb.getServerMarket(), type, symbol, sdf.parse(startDate), sdf.parse(endDate));
		} 
		catch (ParseException e) 
		{
			log.warn("Input date format is wrong");
		}
		if (listPrice == null || symboldata == null)
		{
			retEvent.setOk(false) ;
//			retEvent.setMessage("Get price list fail");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.DATA_NOT_FOUND, "Get price list fail"));
			centraldb.sendEvent(retEvent) ;
			log.debug("Process Historical Price Request fail: Get price list fail");
			return ;
		}
		else
		{
			retEvent.setOk(true) ;
			retEvent.setHistoryType(event.getHistoryType());
			retEvent.setDataCount(listPrice.size());
			retEvent.setPriceList(listPrice);
			retEvent.setCurrentTotalVolume((long) symboldata.getdCurTotalVolume());
			retEvent.setCurrentTurnover((long) symboldata.getdCurTurnover());
			centraldb.sendEvent(retEvent) ;
			log.info("Process Historical Price Request success Symbol: " + listPrice.size());
			return ;
		}
    }
    
	public void processPriceHighLowRequestEvent(PriceHighLowRequestEvent event)
	{
		log.info("Process Price High Low Request");
		String sender = event.getSender() ;
		List<String> symbolList = event.getSymbolList() ;
		PriceHighLowEvent retEvent = new PriceHighLowEvent(null, sender) ;
		retEvent.setType(event.getType());
		if (symbolList == null)
		{
			retEvent.setOk(false);
//			retEvent.setMessage("No requested symbol is find");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.REF_SYMBOL_NOT_FOUND, "No requested symbol is find"));
			log.debug("Process Price High Low Request fail: No requested symbol is find");
			centraldb.sendEvent(retEvent) ;
			return;
		}

		ArrayList<PriceHighLow> phlList = new ArrayList<PriceHighLow>() ;

		for (String symbol : symbolList)
		{
			phlList.add(centraldb.getChefBySymbol(symbol).retrievePriceHighLow(symbol, event.getType()));
		}
		if (phlList.isEmpty())
		{
			retEvent.setOk(false);
//			retEvent.setMessage("No requested symbol is find");
			retEvent.setMessage(MessageLookup.buildEventMessage(ErrorMessage.REF_SYMBOL_NOT_FOUND, "No requested symbol is find"));
			log.debug("Process Price High Low Request fail: No requested symbol is find");
		}
		else
		{
			retEvent.setOk(true);
			retEvent.setListHighLow(phlList);
			log.info("Process Price High Low Request success Symbol: " + phlList.size());
		}
		centraldb.sendEvent(retEvent) ;
	}	
	public void processSymbolListSubscribeRequestEvent(SymbolListSubscribeRequestEvent event)
	{
		SymbolListSubscribeEvent retEvent = new SymbolListSubscribeEvent(null, event.getSender());
		SymbolListSubscribeType type = event.getType() ;
		retEvent.setUserID(event.getUserID());
		retEvent.setGroup(event.getGroup());
		retEvent.setMarket(event.getMarket());
		retEvent.setTxId(event.getTxId());
		retEvent.setType(event.getType());
		retEvent.setQueryType(event.getQueryType());
		String user = event.getUserID();
		String market = event.getMarket();
		String group = event.getGroup();
		ArrayList<String> symbols = (ArrayList<String>) event.getSymbolList();
		switch(type)
		{
		case DEFAULT:
			log.info("Process Symbol List Subscribe Type: DEFAULT Market: " + market);
			centraldb.requestDefaultSymbol(retEvent, market);
			break;
		case ALLSYMBOL:
			log.info("Process Symbol List Subscribe Type: ALLSYMBOL Market: " + market);
			centraldb.userRequestAllSymbol(retEvent, market);
			break;
		case GROUPSYMBOL:
			log.info("Process Symbol List Subscribe Type: GROUPSYMBOL User: " + user + " Market: " + market + " Group: " + group);
			centraldb.userRequestGroupSymbol(retEvent, user, market, group);
			break;
		case SET:
			log.info("Process Symbol List Subscribe Type: SET User: " + user + " Market: " + market + " Group: " + group);
			centraldb.userSetGroupSymbol(retEvent, user, market, group, symbols);
			break;
		default:
			break;
		}
	}
	
	public void processMarketSessionEvent(MarketSessionEvent event)
	{
		log.info("Process MarketSession: " + event.getTradeDate() + " " + event.getSession()
				+ " Start: " + event.getStart() + " End: " + event.getEnd());
		centraldb.setTradedate(event.getTradeDate());
		centraldb.setSessionType(event.getSession(), event.getMarket()) ;
		centraldb.setSessionEnd(event.getEnd());
	}
	public void parseEvent(RemoteAsyncEvent event) throws Exception
	{
		if (event instanceof HistoricalPriceRequestEvent)
		{
			processHistoricalPriceRequestEvent((HistoricalPriceRequestEvent)event);
		}
		else if (event instanceof MarketSessionEvent)
		{
			processMarketSessionEvent((MarketSessionEvent)event);
		}
		else if (event instanceof PriceHighLowRequestEvent)
		{
			processPriceHighLowRequestEvent((PriceHighLowRequestEvent)event);
		}
		else if (event instanceof SymbolListSubscribeRequestEvent)
		{
			processSymbolListSubscribeRequestEvent((SymbolListSubscribeRequestEvent)event);
		}
		else if (event instanceof HistoricalPriceRequestDateEvent)
		{
			processHistoricalPriceRequestDateEvent((HistoricalPriceRequestDateEvent)event);
		}
	}

	@Override
	public void run() 
	{
		long lTimeOut = 50;
		RemoteAsyncEvent event;
		while (true)
		{
			try 
			{
				event = m_q.poll(lTimeOut, TimeUnit.MILLISECONDS);
			} 
			catch (InterruptedException e) 
			{
				event = null;
				log.error(strName, e);
			}
            if (event != null)
            {
            	try 
            	{
					parseEvent(event);
				} 
            	catch (Exception e) 
				{
					log.error(strName, e);
				}
            }
		}
	}

}
