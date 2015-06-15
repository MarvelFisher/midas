package com.cyanspring.server.statistic;

import com.cyanspring.common.account.Account;

public class AccountStatisticCollector {
	private double totalUrPnL;
	private double totalAllTimePnL;
	private double totalValue;
	private double totalDailyPnL;
	private double totalAccountValue;
	private double totalCashDeposited;
	
	public void calculate(Account account){
		totalUrPnL += account.getUrPnL();
		totalAllTimePnL += account.getAllTimePnL();
		totalValue += account.getValue();
		totalDailyPnL += account.getDailyPnL();
		totalAccountValue += account.getStartAccountValue();
		totalCashDeposited += account.getCashDeposited();
	}
	
	public double getTotalUrPnL() {
		return totalUrPnL;
	}

	public double getTotalAllTimePnl() {
		return totalAllTimePnL;
	}

	public double getTotalValue() {
		return totalValue;
	}

	public double getTotalDailyPnL() {
		return totalDailyPnL;
	}

	public double getTotalAccountValue() {
		return totalAccountValue;
	}

	public double getTotalCashDeposited() {
		return totalCashDeposited;
	}

}
