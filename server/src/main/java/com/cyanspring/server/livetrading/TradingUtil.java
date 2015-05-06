package com.cyanspring.server.livetrading;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.account.PositionKeeper;

public class TradingUtil {
	
	private static final Logger log = LoggerFactory
			.getLogger(TradingUtil.class);
	private static Lock lock = new ReentrantLock();
	private static IQuoteChecker quoteChecker = new PriceQuoteChecker();
	
	private static boolean isValidQuote(Quote quote){
		if(null != quoteChecker && !quoteChecker.check(quote))
			return false;
		return !quote.isStale();
	}
	
	public static void cancelAllOrders(Account account,PositionKeeper positionKeeper,IRemoteEventManager eventManager){
		
		lock.lock();
		try{
			
			List <ParentOrder> orderList = positionKeeper.getParentOrders(account.getId());
			if(orderList.size() > 0)
				log.info("Live trading cancelling of orders: ", orderList.size());
			for(ParentOrder order : orderList){
				if(order.getOrdStatus().isReady()){
					String source = order.get(String.class, OrderField.SOURCE.value());
					String txId = order.get(String.class, OrderField.CLORDERID.value());
					CancelStrategyOrderEvent cancel = 
							new CancelStrategyOrderEvent(order.getId(), order.getSender(), txId, source, OrderReason.CompanyStopLoss, false);
					eventManager.sendEvent(cancel);
				}
			}
			
		}finally{
			lock.unlock();
		}
		
	}
	
	public static void closeOpenPositions(Account account,PositionKeeper positionKeeper,IRemoteEventManager eventManager,boolean checkValidQuote){
		
		lock.lock();
		try{
			
			List <OpenPosition> positionList = positionKeeper.getOverallPosition(account);
			if(positionList.size() > 0)
				log.info("Live trading closing of positions: ", positionList.size());
			if(positionList.size() >0){
				for(OpenPosition position : positionList){
					Quote quote = positionKeeper.getQuote(position.getSymbol());
					if(checkValidQuote && !isValidQuote(quote))
						continue;
					
	                if (positionKeeper.checkAccountPositionLock(account.getId(), position.getSymbol())) {
	                    log.debug("Position stop loss over threshold but account is locked: " +
	                            account.getId() + ", " + position.getSymbol());
	                    continue;
	                }
	                
	                log.info("Position loss over threshold, cutting loss: " + position.getAccount() + ", " +
	                        position.getSymbol() + ", " + position.getAcPnL() + ", " +
	                        quote);
					ClosePositionRequestEvent event = new ClosePositionRequestEvent(position.getAccount(), 
							null, position.getAccount(), position.getSymbol(), 0.0, OrderReason.CompanyStopLoss,
							IdGenerator.getInstance().getNextID());
					
					eventManager.sendEvent(event);
				}
			}
				
		}finally{
			lock.unlock();
		}
		
	}
}
