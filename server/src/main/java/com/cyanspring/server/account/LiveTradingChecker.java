package com.cyanspring.server.account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.UserTerminateReplyEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.persistence.PersistenceManager;

public class LiveTradingChecker{
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingChecker.class);
	@Autowired
	private LiveTradingSetting liveTradingSetting;
	@Autowired
	private PositionKeeper positionKeeper;
	@Autowired
	private AccountKeeper accountKeeper;
	@Autowired
	private IRemoteEventManager eventManager;
	
	private IQuoteChecker quoteChecker = new PriceQuoteChecker();
	
	public Double getPositionStopLoss(Account account,AccountSetting accountSetting,Double positionStopLoss){
		if(!liveTradingSetting.isNeedCheckPosition()){
			return positionStopLoss;
		}	
		
		Double stopLoss= account.getStartAccountValue() * accountSetting.getStopLossPercent();
		
		if(PriceUtils.isZero(stopLoss)){
			return positionStopLoss;
		} else if(PriceUtils.isZero(positionStopLoss)) {
			return stopLoss;
		}				

		return Math.min(stopLoss, positionStopLoss);
	}
		
	private boolean isValidQuote(Quote quote){
		if(null != quoteChecker && !quoteChecker.check(quote))
			return false;
		return !quote.isStale();
	}
	
	private void closeAllPositoinAndOrder(Account account){
		//close all order
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
		
		//close all position
		List <OpenPosition> positionList = positionKeeper.getOverallPosition(account);
		if(positionList.size() > 0)
			log.info("Live trading closing of positions: ", positionList.size());
		if(positionList.size() >0){
			for(OpenPosition position : positionList){
				Quote quote = positionKeeper.getQuote(position.getSymbol());
				if(!this.isValidQuote(quote))
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
	}
	
	private void sendUpdateAccountEvent(Account account){
		try {
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));

			if(AccountState.TERMINATED == account.getState()){
				String message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_TERMINATED, "Live Trading - Account:"+account.getId()+" is terminated due to exceeding loss limit");
				eventManager.sendRemoteEvent(new UserTerminateReplyEvent(null, null ,true, message, account.getId(), TerminationStatus.TERMINATED));
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public boolean checkFreezeLoss(Account account,AccountSetting accountSetting){
		if( !liveTradingSetting.isNeedCheckFreeze()){
			return false;
		}
		if(PriceUtils.isZero(accountSetting.getFreezePercent())){
			return false;
		}
		
		if(AccountState.FROZEN == account.getState() ){
			closeAllPositoinAndOrder(account);
			return true;
		}
		
		Double dailyStopLoss = account.getStartAccountValue() * accountSetting.getFreezePercent();
		
		if(PriceUtils.isZero(dailyStopLoss)){		
			return false;
		}
		
		if(PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)){
			log.info("Daily loss: " + account.getDailyPnL() + " over " + dailyStopLoss);
			// set it to frozen first
			account.setState(AccountState.FROZEN);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account);
			return true;
			
		}
		return false;
		
	}

	public boolean checkTerminateLoss(Account account,AccountSetting accountSetting){
		if(!liveTradingSetting.isNeedCheckTerminate()){
			return false;
		}
		
		if(PriceUtils.isZero(accountSetting.getTerminatePercent())){
			return false;
		}
		
		if(AccountState.TERMINATED == account.getState() ){
			closeAllPositoinAndOrder(account);
			return true;
		}
		
		double totalLossLimit = account.getCashDeposited() * accountSetting.getTerminatePercent();
		double currentLoss = account.getValue() - account.getCashDeposited();
		if(PriceUtils.EqualLessThan(currentLoss, -totalLossLimit)){
			log.info("Terminate loss: " + currentLoss + " over " + -totalLossLimit);
			account.setState(AccountState.TERMINATED);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account);
			return true;
		}
			
		return false;
	}
}
