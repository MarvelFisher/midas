package com.cyanspring.server.livetrading.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.AccountStateReplyEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.UserTerminateReplyEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.LiveTradingSetting;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.order.RiskOrderController;
import com.cyanspring.server.persistence.PersistenceManager;

public class TerminateStopLossCheck implements ILiveTradingChecker {
	
	private static final Logger log = LoggerFactory
			.getLogger(TerminateStopLossCheck.class);
	
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
				
		if(!accountSetting.isUserLiveTrading()){
			return false;
		}
		
		if(!liveTradingSetting.isNeedCheckTerminate()){
			return true;
		}
		
		double totalLossLimit = TradingUtil.getMinValue(account.getCashDeposited() * accountSetting.getTerminatePercent()
				, accountSetting.getTerminateValue());
		
		if( PriceUtils.isZero(totalLossLimit) ){
			return true;
		}
		
		double currentLoss = account.getValue() - account.getCashDeposited();
		
		if(PriceUtils.EqualLessThan(currentLoss, -totalLossLimit)){
			log.info("Account:"+account.getId()+"Terminate loss: " + currentLoss + " over " + -totalLossLimit);
			if(!account.getState().equals(AccountState.TERMINATED)) {
				account.setState(AccountState.TERMINATED);
				sendUpdateAccountEvent(account);
			}
			TradingUtil.closeAllPositoinAndOrder(account, positionKeeper, eventManager, true, 
					OrderReason.AccountStopLoss, riskOrderController);
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
	
	public boolean isOverTerminateLoss(Account account, AccountSetting accountSetting){
		double totalLossLimit = TradingUtil.getMinValue(account.getCashDeposited() * accountSetting.getTerminatePercent()
				, accountSetting.getTerminateValue());
		double currentLoss = account.getValue() - account.getCashDeposited();
		
		if( PriceUtils.isZero(totalLossLimit) ){
			return false;
		}
		
		if(PriceUtils.EqualLessThan(currentLoss, -totalLossLimit)){
			return true;
		}
		return false;
	}
	
}
