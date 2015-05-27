package com.cyanspring.server.livetrading.checker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.ClosePositionRequestEvent;
import com.cyanspring.common.marketdata.IQuoteChecker;
import com.cyanspring.common.marketdata.PriceQuoteChecker;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.account.PositionKeeper;
import com.cyanspring.server.livetrading.LiveTradingSetting;

public class PositionStopLossCheck implements ILiveTradingChecker {
	
    private static final Logger log = LoggerFactory
            .getLogger(PositionStopLossCheck.class);
    
	@Autowired
	private LiveTradingSetting liveTradingSetting;
    
    @Autowired
    PositionKeeper positionKeeper;
    
    @Autowired
    private IRemoteEventManager eventManager;
    
    private IQuoteChecker quoteChecker = new PriceQuoteChecker();
    
    /**
     * need next check return true otherwise return false
     */
	@Override
	public boolean check(Account account, AccountSetting accountSetting) {

		if(!accountSetting.isUserLiveTrading()){
			return false;
		}
		
		Double positionStopLoss = Default.getPositionStopLoss();

		if (null != accountSetting) {
			
			positionStopLoss = accountSetting.getStopLossValue();
			Double companyStopLoss = accountSetting.getCompanySLValue();
			if (null == positionStopLoss){
				positionStopLoss = Default.getPositionStopLoss();
			}
			if (null != companyStopLoss && !PriceUtils.isZero(companyStopLoss)) {
				if (PriceUtils.isZero(positionStopLoss))
					positionStopLoss = companyStopLoss;
				else
					positionStopLoss = Math.min(positionStopLoss,
							companyStopLoss);
			}

			positionStopLoss = getPositionStopLoss(account, accountSetting,
					positionStopLoss);

		}

		if (PriceUtils.isZero(positionStopLoss)){
			return true;
		}
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		
		for (OpenPosition position : positions) {
			Quote quote = positionKeeper.getQuote(position.getSymbol());
			if (PriceUtils
					.EqualLessThan(position.getAcPnL(), -positionStopLoss)
					&& null != quote && quoteIsValid(quote)) {
				if (positionKeeper.checkAccountPositionLock(account.getId(),
						position.getSymbol())) {
					log.debug("Live trading Position stop loss over threshold but account is locked: "
							+ account.getId() + ", " + position.getSymbol());
					continue;
				}
				log.info("Live trading Position loss over threshold, cutting loss: "
						+ position.getAccount() + ", " + position.getSymbol()
						+ ", " + position.getAcPnL() + ", " + positionStopLoss
						+ ", " + quote);
				ClosePositionRequestEvent event = new ClosePositionRequestEvent(
						position.getAccount(), null, position.getAccount(),
						position.getSymbol(), 0.0, OrderReason.StopLoss,
						IdGenerator.getInstance().getNextID());

				eventManager.sendEvent(event);
			}
		}

		return true;
	}

	private double getPositionStopLoss(Account account,
			AccountSetting accountSetting, Double positionStopLoss) {

		if(!liveTradingSetting.isNeedCheckPosition() || !accountSetting.checkLiveTrading()){
			return positionStopLoss;
		}
		
		if (PriceUtils.isZero(accountSetting.getStopLossPercent())) {
			return positionStopLoss;
		}
				
		Double stopLoss = account.getStartAccountValue()
				* accountSetting.getStopLossPercent();
		
		if (PriceUtils.isZero(stopLoss)) {
			return positionStopLoss;
		} else if (PriceUtils.isZero(positionStopLoss)) {
			return stopLoss;
		}

		return Math.min(stopLoss, positionStopLoss);		
	}
	
    private boolean quoteIsValid(Quote quote) {
    	
        if (null != quoteChecker && !quoteChecker.check(quote))
            return false;

        return !quote.isStale();
    }
    
}
