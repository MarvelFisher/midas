package com.cyanspring.server.livetrading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.UserTerminateReplyEvent;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.checker.LiveTradingCheckHandler;
import com.cyanspring.server.livetrading.rule.LiveTradingRuleHandler;
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
	@Autowired(required=false)
	private LiveTradingRuleHandler liveTradingRuleHandler;
	@Autowired(required=false)
	private LiveTradingCheckHandler liveTradingCheckHandler;
	
	private IQuoteChecker quoteChecker = new PriceQuoteChecker();

	public void checkStartAccountValue(Account account){
		if(PriceUtils.isZero(account.getStartAccountValue())){
			account.setStartAccountValue(account.getCashDeposited());
		}
	}
	
	public Double getPositionStopLoss(Account account,AccountSetting accountSetting,Double positionStopLoss){
		
		if(!liveTradingSetting.isNeedCheckPosition() || !accountSetting.checkLiveTrading()){
			return positionStopLoss;
		}	
		if(PriceUtils.isZero(accountSetting.getStopLossPercent())){
			return positionStopLoss;
		}
		checkStartAccountValue(account);
		Double stopLoss= account.getStartAccountValue() * accountSetting.getStopLossPercent();
		if(PriceUtils.isZero(stopLoss)){
			return positionStopLoss;
		} else if(PriceUtils.isZero(positionStopLoss)) {
			return stopLoss;
		}				

		return Math.min(stopLoss, positionStopLoss);
		
	}
	
	private void closeAllPositoinAndOrder(Account account){
		TradingUtil.cancelAllOrders(account, positionKeeper, eventManager);
		TradingUtil.closeOpenPositions(account, positionKeeper, eventManager, true);
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

		if( !liveTradingSetting.isNeedCheckFreeze() || !accountSetting.checkLiveTrading()){
			return false;
		}
		if(PriceUtils.isZero(accountSetting.getFreezePercent())){
			return false;
		}
		
		if(AccountState.FROZEN == account.getState() ){
			closeAllPositoinAndOrder(account);
			return true;
		}
		checkStartAccountValue(account);
		Double dailyStopLoss = account.getStartAccountValue() * accountSetting.getFreezePercent();
		if(PriceUtils.isZero(dailyStopLoss)){		
			return false;
		}
		if(PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)){
			log.info("Account:"+account.getId()+" Daily loss: " + account.getDailyPnL() + " over " + -dailyStopLoss);
			// set it to frozen first
			account.setState(AccountState.FROZEN);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account);
			return true;
			
		}
		return false;
		
	}

	public boolean checkTerminateLoss(Account account,AccountSetting accountSetting){
		if(!liveTradingSetting.isNeedCheckTerminate() || !accountSetting.checkLiveTrading()){
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
			log.info("Account:"+account.getId()+"Terminate loss: " + currentLoss + " over " + -totalLossLimit);
			account.setState(AccountState.TERMINATED);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account);
			return true;
		}
			
		return false;
	}
}
