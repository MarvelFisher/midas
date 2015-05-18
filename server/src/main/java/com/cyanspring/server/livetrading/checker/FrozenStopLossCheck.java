package com.cyanspring.server.livetrading.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.event.IRemoteEventManager;
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

		if(!accountSetting.checkLiveTrading()){
			return false;
		}
		if( !liveTradingSetting.isNeedCheckFreeze()){
			return true;
		}
		if(PriceUtils.isZero(accountSetting.getFreezePercent())){
			return true;
		}
		if(AccountState.FROZEN == account.getState() ){
			closeAllPositoinAndOrder(account);
			return false;
		}
		
		Double dailyStopLoss = account.getStartAccountValue() * accountSetting.getFreezePercent();
		
		if(PriceUtils.isZero(dailyStopLoss)){
			if(!PriceUtils.isZero(accountSetting.getDailyStopLoss())){
				dailyStopLoss = accountSetting.getDailyStopLoss();
			}else{
				return true;
			}
		}else{
			dailyStopLoss = Math.min(dailyStopLoss, accountSetting.getDailyStopLoss());
		}
				
		if(PriceUtils.EqualLessThan(account.getDailyPnL(), -dailyStopLoss)){
			
			log.info("Account:"+account.getId()+" Daily loss: " + account.getDailyPnL() + " over " + -dailyStopLoss);
			account.setState(AccountState.FROZEN);
			sendUpdateAccountEvent(account);
			closeAllPositoinAndOrder(account);
			return false;
			
		}
		return true;
		
	}
	
	private void closeAllPositoinAndOrder(Account account){
		TradingUtil.cancelAllOrders(account, positionKeeper, eventManager);
		TradingUtil.closeOpenPositions(account, positionKeeper, eventManager, true);
	}

	private void sendUpdateAccountEvent(Account account){
		try {
			eventManager.sendEvent(new PmUpdateAccountEvent(PersistenceManager.ID, null, account));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
}
