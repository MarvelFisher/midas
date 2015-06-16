package com.cyanspring.server.livetrading.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.AccountStateReplyEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.LiveTradingSetting;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.persistence.PersistenceManager;

public class FrozenStopLossCheck implements ILiveTradingChecker {
	
	private static final Logger log = LoggerFactory
			.getLogger(FrozenStopLossCheck.class);
	
	@Autowired
	private LiveTradingSetting liveTradingSetting;
	
    @Autowired
    PositionKeeper positionKeeper;
    
    @Autowired
    private IRemoteEventManager eventManager;
	
    /**
     * need next check return true otherwise return false
     */
	@Override
	public boolean check(Account account, AccountSetting accountSetting) {

		if(!accountSetting.isUserLiveTrading()){
			return false;
		}
		if( !liveTradingSetting.isNeedCheckFreeze()){
			return true;
		}

		if(AccountState.FROZEN == account.getState() ){
			closeAllPositoinAndOrder(account,OrderReason.CompanyDailyStopLoss);
			return false;
		}
		
		Double dailyStopLoss = TradingUtil.getMinValue(account.getStartAccountValue() * accountSetting.getFreezePercent()
				, accountSetting.getFreezeValue());
		
		dailyStopLoss = TradingUtil.getMinValue(dailyStopLoss,accountSetting.getDailyStopLoss());

		
		if(PriceUtils.isZero(dailyStopLoss)){
				return true;
		}
		
		OrderReason orderReason= OrderReason.CompanyDailyStopLoss;
		
		if(PriceUtils.Equal(dailyStopLoss, accountSetting.getDailyStopLoss()))
			orderReason = OrderReason.DailyStopLoss;
				
		if(PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)){
			
			log.info("Account:"+account.getId()+" Daily loss: " + account.getDailyPnL() + " over " + -dailyStopLoss+" reason:"+orderReason);
			account.setState(AccountState.FROZEN);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account,orderReason);
			return false;
			
		}
		return true;
		
	}
	
	private void closeAllPositoinAndOrder(Account account,OrderReason orderReason){
		TradingUtil.cancelAllOrders(account, positionKeeper, eventManager,orderReason);
		TradingUtil.closeOpenPositions(account, positionKeeper, eventManager, true,orderReason);
	}

	private void sendUpdateAccountEvent(Account account){
		try {
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
	    	AccountStateReplyEvent accountStateEvent = new AccountStateReplyEvent(null
	    			,null,true,"",account.getId(),account.getUserId(),account.getState());
	    	eventManager.sendRemoteEvent(accountStateEvent);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
}
