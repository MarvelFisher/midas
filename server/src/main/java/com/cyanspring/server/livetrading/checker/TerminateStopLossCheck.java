package com.cyanspring.server.livetrading.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.AccountStateReplyEvent;
import com.cyanspring.common.event.account.PmUpdateAccountEvent;
import com.cyanspring.common.event.account.UserTerminateReplyEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.LiveTradingSetting;
import com.cyanspring.server.livetrading.TradingUtil;
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
		
		if(AccountState.TERMINATED == account.getState() ){
			closeAllPositoinAndOrder(account);
			return false;
		}
		
		double totalLossLimit = TradingUtil.getMinValue(account.getCashDeposited() * accountSetting.getTerminatePercent()
				, accountSetting.getTerminateValue());
		double currentLoss = account.getValue() - account.getCashDeposited();
		if(PriceUtils.EqualLessThan(currentLoss, -totalLossLimit)){
			log.info("Account:"+account.getId()+"Terminate loss: " + currentLoss + " over " + -totalLossLimit);
			account.setState(AccountState.TERMINATED);
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
			
			String message = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_TERMINATED, "Live Trading - Account:"+account.getId()+" is terminated due to exceeding loss limit");
			
			eventManager.sendRemoteEvent(new UserTerminateReplyEvent(null, null ,true, message, account.getId(), TerminationStatus.TERMINATED));
	    	
			AccountStateReplyEvent accountStateEvent = new AccountStateReplyEvent(null
	    			,null,true,"",account.getId(),account.getUserId(),account.getState());
	    	
			eventManager.sendRemoteEvent(accountStateEvent);
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
}
