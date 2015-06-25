package com.cyanspring.server.statistic;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.statistic.AccountStatistic;

public class AccountStatisticCollector {

	private AccountStatisticBean defaultAccount;
	private AccountStatisticBean liveTradingAccount;
	
	public AccountStatisticCollector() {
		defaultAccount = new AccountStatisticBean();
		liveTradingAccount = new AccountStatisticBean();
	}
	
	public void calculate(Account account,AccountSetting setting){	
		
		if(null != account && null != setting && setting.isUserLiveTrading()){
			liveTradingAccount.setTotalUrPnL( liveTradingAccount.getTotalUrPnL() + account.getUrPnL()) ;
			liveTradingAccount.setTotalAllTimePnL(liveTradingAccount.getTotalAllTimePnL()+ account.getAllTimePnL());
			liveTradingAccount.setTotalValue( liveTradingAccount.getTotalValue() + account.getValue());
			liveTradingAccount.setTotalDailyPnL(liveTradingAccount.getTotalDailyPnL()+account.getDailyPnL());
			liveTradingAccount.setTotalAccountValue(liveTradingAccount.getTotalAccountValue()+account.getStartAccountValue());
			liveTradingAccount.setTotalCashDeposited(liveTradingAccount.getTotalCashDeposited()+account.getCashDeposited());
		}
		if( null != account){
			defaultAccount.setTotalUrPnL( defaultAccount.getTotalUrPnL() + account.getUrPnL()) ;
			defaultAccount.setTotalAllTimePnL(defaultAccount.getTotalAllTimePnL()+ account.getAllTimePnL());
			defaultAccount.setTotalValue( defaultAccount.getTotalValue() + account.getValue());
			defaultAccount.setTotalDailyPnL(defaultAccount.getTotalDailyPnL()+account.getDailyPnL());
			defaultAccount.setTotalAccountValue(defaultAccount.getTotalAccountValue()+account.getStartAccountValue());
			defaultAccount.setTotalCashDeposited(defaultAccount.getTotalCashDeposited()+account.getCashDeposited());
		}
	
	}
	
	public Map<String,Object> toDefaultCaculatedMap(){
		Map <String,Object>map = new LinkedHashMap<>();
		map.put(AccountStatistic.CASH_DEPOSITED.value(), defaultAccount.getTotalCashDeposited());
		map.put(AccountStatistic.ACCOUNT_VALUE.value(), defaultAccount.getTotalAccountValue());
		map.put(AccountStatistic.VALUE.value(), defaultAccount.getTotalValue());		
		map.put(AccountStatistic.DAILY_PNL.value(), defaultAccount.getTotalDailyPnL());
		map.put(AccountStatistic.ALL_TIME_PNL.value(), defaultAccount.getTotalAllTimePnL());	
		map.put(AccountStatistic.UR_PNL.value(), defaultAccount.getTotalUrPnL());
		return map;
	}
	
	public Map<String,Object> toLiveTradingCaculatedMap(){
		Map <String,Object>map = new LinkedHashMap<>();
		map.put(AccountStatistic.CASH_DEPOSITED.value(), liveTradingAccount.getTotalCashDeposited());
		map.put(AccountStatistic.ACCOUNT_VALUE.value(), liveTradingAccount.getTotalAccountValue());
		map.put(AccountStatistic.VALUE.value(), liveTradingAccount.getTotalValue());		
		map.put(AccountStatistic.DAILY_PNL.value(), liveTradingAccount.getTotalDailyPnL());
		map.put(AccountStatistic.ALL_TIME_PNL.value(), liveTradingAccount.getTotalAllTimePnL());	
		map.put(AccountStatistic.UR_PNL.value(), liveTradingAccount.getTotalUrPnL());
		return map;
	}

}
