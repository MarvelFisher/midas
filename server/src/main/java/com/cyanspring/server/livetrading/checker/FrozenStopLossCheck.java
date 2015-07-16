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
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.LiveTradingSetting;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.order.RiskOrderController;
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
	
	@Autowired(required=false)
	RiskOrderController riskOrderController;

    /**
     * need next check return true otherwise return false
     */
	@Override
	public boolean check(Account account, AccountSetting accountSetting) {

		if( !liveTradingSetting.isNeedCheckFreeze()){
			return true;
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
			if(account.getState().equals(AccountState.ACTIVE)) {
				account.setState(AccountState.FROZEN);
				sendUpdateAccountEvent(account);
			}
			TradingUtil.closeAllPositoinAndOrder(account, positionKeeper, eventManager, true, orderReason, riskOrderController);
			return false;
			
		}
		return true;
		
	}
	
	private void sendUpdateAccountEvent(Account account){
		try {
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
			
			AccountUpdateEvent event = new AccountUpdateEvent(account.getId(), null, account);
	    	eventManager.sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public boolean isOverFrozenLoss(Account account, AccountSetting accountSetting){
		
		Double dailyStopLoss = TradingUtil.getMinValue(account.getStartAccountValue() * accountSetting.getFreezePercent()
				, accountSetting.getFreezeValue());
		
		dailyStopLoss = TradingUtil.getMinValue(dailyStopLoss,accountSetting.getDailyStopLoss());	
		if(PriceUtils.isZero(dailyStopLoss)){
				return false;
		}		
		if(PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)){
			return true;			
		}
		
		return false;
	}
	
}
