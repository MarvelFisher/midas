package com.cyanspring.server.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;

public class TotalPnLCalculator {
	private double totalPnL;
	private double totalAccountValue;
	private double liveTradingPnL;
	private double liveTradingAccountValue;
	private double cumTotalPnL;
	private double cumLiveTradingPnL;
	private double cumTotalAccountValue;
	private double cumLiveTradingAccountValue;
	private String startAccount;
	private String startLiveAccount;
	
	public void calculate(Account account, AccountSetting setting) {

		if(null == startAccount || startAccount.equals(account.getId())) {
			startAccount = account.getId();
			totalPnL = cumTotalPnL;
			totalAccountValue = cumTotalAccountValue;
			cumTotalPnL = account.getDailyPnL();
			cumTotalAccountValue = account.getStartAccountValue();
		} else {
			cumTotalPnL += account.getDailyPnL();
			cumTotalAccountValue += account.getStartAccountValue();
		}
		
		if(setting.checkLiveTrading()) {
			if(null == startLiveAccount || startLiveAccount.equals(account.getId())) {
				startLiveAccount = account.getId();
				liveTradingPnL = cumLiveTradingPnL;
				liveTradingAccountValue = cumLiveTradingAccountValue;
				cumLiveTradingPnL = account.getDailyPnL();
				cumLiveTradingAccountValue = account.getStartAccountValue();
			} else {
				cumLiveTradingPnL += account.getDailyPnL();
				cumLiveTradingAccountValue += account.getStartAccountValue();
			}
		}
			
	}

	public double getLiveTradingPnL() {
		return liveTradingPnL;
	}

	public double getTotalPnL() {
		return totalPnL;
	}

	public double getTotalAccountValue() {
		return totalAccountValue;
	}

	public double getLiveTradingAccountValue() {
		return liveTradingAccountValue;
	}
	
}
