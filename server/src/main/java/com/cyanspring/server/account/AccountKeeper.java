package com.cyanspring.server.account;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountSettingType;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.server.livetrading.TradingUtil;
import com.cyanspring.server.livetrading.rule.LiveTradingRuleHandler;

public class AccountKeeper {
	private static final Logger log = LoggerFactory
			.getLogger(AccountKeeper.class);
	
	private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<String, Account>();
	private ConcurrentHashMap<String, List<Account>> userAccounts = new ConcurrentHashMap<String, List<Account>>();
	private List<Account> jobs = new ArrayList<Account>();
	private ConcurrentHashMap<String, AccountSetting> accountSettings = 
				new ConcurrentHashMap<String, AccountSetting>();
	private int dynamicJobBatch = 100;
	private int rmJobBatch = 2000;
	private AccountJobs dynamicJobs;
	private AccountJobs rmJobs;
	
	@Autowired
	ILeverageManager leverageManager;
	
    @Autowired(required = false)
    LiveTradingRuleHandler liveTradingRuleHandler;
	
	public void init() {
		dynamicJobs = new AccountJobs(dynamicJobBatch);
		rmJobs = new AccountJobs(rmJobBatch);
	}
	
	public class AccountJobs {
		private int jobIndex = 0;
		private int jobBatch = 50;
		
		public AccountJobs(int jobBatch) {
			this.jobBatch = jobBatch;
		}
		
		public List<Account> getJobs() {
			List<Account> result = new ArrayList<Account>();
			int i = 0;
			synchronized(jobs) {
				while(i < this.jobBatch && i < jobs.size()) {
					result.add(jobs.get(this.jobIndex++));
					if(this.jobIndex>=jobs.size())
						this.jobIndex = 0;
					i++;
				}
			}
			return result;
		}
	}
	
	public AccountSetting getAccountSetting(String account) throws AccountException {
		if(!accounts.containsKey(account))
			throw new AccountException("Account id doesn't exist: " + account,ErrorMessage.ACCOUNT_NOT_EXIST);
		
		AccountSetting setting = accountSettings.get(account);
		AccountSetting empty = AccountSetting.createEmptySettings(account);
		if(null == setting)
			return empty;
		
		synchronized(setting) {
			empty.update(setting);
			return empty;
		}
	}
	
	private void checkLiveTradingStopLossValue(AccountSetting oldSetting,AccountSetting newSetting,Account account)throws AccountException{
			
		double comDailyStopLoss = TradingUtil.getMinValue(account.getStartAccountValue() * oldSetting.getFreezePercent()
                , oldSetting.getFreezeValue());
		
		double comPositionStopLoss = TradingUtil.getMinValue(account.getStartAccountValue() * oldSetting.getStopLossPercent()
                , oldSetting.getCompanySLValue());
		
		if(!PriceUtils.isZero(comDailyStopLoss) 
				&& PriceUtils.GreaterThan(newSetting.getDailyStopLoss(), comDailyStopLoss)){
			
			throw new AccountException("The value you set exceeds max. daily stop loss!",ErrorMessage.USER_DAILY_STOP_LOSS_VALUE_EXCEEDS_COMPANY_SETTING);

		}
		
		if(!PriceUtils.isZero(comPositionStopLoss) 
				&& PriceUtils.GreaterThan(newSetting.getStopLossValue(), comPositionStopLoss)){

			throw new AccountException("The value you set exceeds max. position stop loss!",ErrorMessage.USER_POSITION_STOP_LOSS_VALUE_EXCEEDS_COMPANY_SETTING);
		}

	}
	
	private void setLiveTradingStopLossValue(AccountSetting oldSetting,AccountSetting newSetting,Account account)throws AccountException{
		
		double comDailyStopLoss = TradingUtil.getMinValue(account.getStartAccountValue()*oldSetting.getFreezePercent()
				, oldSetting.getFreezeValue());
		
		double comPositionStopLoss = TradingUtil.getMinValue(account.getStartAccountValue()*oldSetting.getStopLossPercent()
				, oldSetting.getCompanySLValue());
		log.info("comDailyStopLoss:{} , comPositionStopLoss:{}",comDailyStopLoss,comPositionStopLoss);
		log.info("oldSetting.getDailyStopLoss():{} , oldSetting.getStopLossValue():{}",oldSetting.getDailyStopLoss(),oldSetting.getStopLossValue());

		if(!PriceUtils.isZero(comDailyStopLoss) 
				&& PriceUtils.GreaterThan(oldSetting.getDailyStopLoss(), comDailyStopLoss)){
			
			oldSetting.setDailyStopLoss(comDailyStopLoss);		
		}
		
		if(!PriceUtils.isZero(comPositionStopLoss) 
				&& PriceUtils.GreaterThan(oldSetting.getStopLossValue(), comPositionStopLoss)){

			oldSetting.setStopLossValue(comPositionStopLoss);		
		}

	}
	
	private boolean enterLiveTrading(AccountSetting setting){
		
		if(setting.fieldExists(AccountSettingType.USER_LIVE_TRADING.value())
				&& setting.isUserLiveTrading()){
			return true;
		}
		
		return false;
	}
	
	public AccountSetting setAccountSetting(AccountSetting setting) throws AccountException {
		if(!accounts.containsKey(setting.getId()))
			throw new AccountException("Account id doesn't exist: " + setting.getId(),ErrorMessage.ACCOUNT_NOT_EXIST);
		
		AccountSetting existing = accountSettings.get(setting.getId());
		Account account = accounts.get(setting.getId());			

		if(null == existing) {
			existing = AccountSetting.createEmptySettings(setting.getId());
			accountSettings.put(setting.getId(), existing);
		}
		
		if(existing.isUserLiveTrading() && null != account){					
		   checkLiveTradingStopLossValue(existing,setting,account);		 		
		}
		
		if(enterLiveTrading(setting) && null != account){
			setLiveTradingStopLossValue(existing,setting,account);
		}
		
		synchronized(existing) {
			for(Entry<String, Object> entry: setting.getFields().entrySet()) {
				existing.getFields().put(entry.getKey(), entry.getValue());
			}
		}
		return existing;
	}
	
	void addAccount(Account account) {
		accounts.put(account.getId(), account);
		List<Account> list = userAccounts.get(account.getUserId());
		if(null == list) {
			list = new ArrayList<Account>();
			List<Account> existing = userAccounts.putIfAbsent(account.getUserId(), list);
			list = existing == null?list:existing;
		}
		synchronized(list) {
			list.add(account);
		}
		synchronized(jobs) {
			jobs.add(account);
		}
	}
	
	public void createAccount(Account account) throws AccountException {
		setupAccount(account);
		addAccount(account);
	}
	
	public void setupAccount(Account account) throws AccountException {
		if(accounts.containsKey(account.getId()))
			throw new AccountException("Account already exists: " + account.getId(),ErrorMessage.USER_ALREADY_EXIST);
		if(null == account.getCurrency())
			account.setCurrency(Default.getCurrency());
		if(PriceUtils.isZero(account.getCash()))
				account.addCash(Default.getAccountCash());
		if(account.getMarket() == null)
			account.setMarket(Default.getMarket());
		account.addMargin(account.getCash() * leverageManager.getLeverage(null, null));
		account.setActive(true);
		account.setState(AccountState.ACTIVE);
		
		if(PriceUtils.isZero(account.getValue()))
			account.setStartAccountValue(Default.getAccountCash());
		else
			account.setStartAccountValue(account.getValue());
	}
	
	public boolean accountExists(String id) {
		return accounts.containsKey(id);
	}
	
	public Account getAccount(String id) {
		return accounts.get(id);
	}
	
	public List<Account> getAccounts(String userId) {
		List<Account> result = new ArrayList<Account>();
		List<Account> list = userAccounts.get(userId);
		if(null != list) {
			synchronized(list) {
				result.addAll(list);
			}
		}
		return result;
	}
	
	public List<Account> getAllAccounts() {
		List<Account> result = new ArrayList<Account>();
		int i=0;
		synchronized(jobs) {
			while(i<jobs.size()) {
				result.add(jobs.get(i));
				i++;
			}
		}
		return result;
	}
	
	public Account tryCreateDefaultAccount() {
		if(!accountExists(Default.getAccount())) {
			Account account = null;
			try {
				account = new Account(Default.getAccount(), Default.getUser());
				createAccount(account);
			} catch (AccountException e) {
				log.error(e.getMessage(), e);
			}
			return account;
		}
		return null;
	}
	
	public void injectAccounts(List<Account> accounts) {
		for(Account account: accounts) {
			addAccount(account);
		}
	}
	
	public void injectAccountSettings(List<AccountSetting> accountSettings) {
		for(AccountSetting accountSetting: accountSettings) {
			this.accountSettings.put(accountSetting.getId(), accountSetting);
		}
	}

	public AccountJobs getDynamicJobs() {
		return dynamicJobs;
	}

	public AccountJobs getRmJobs() {
		return rmJobs;
	}

	public int getDynamicJobBatch() {
		return dynamicJobBatch;
	}

	public void setDynamicJobBatch(int dynamicJobBatch) {
		this.dynamicJobBatch = dynamicJobBatch;
	}

	public int getRmJobBatch() {
		return rmJobBatch;
	}

	public void setRmJobBatch(int rmJobBatch) {
		this.rmJobBatch = rmJobBatch;
	}
	
	public List<AccountSetting> getTrailingStopSettings() {
		List<AccountSetting> result = new LinkedList<AccountSetting>();
		Iterator<AccountSetting> it = accountSettings.values().iterator();
		while(it.hasNext()) {
			AccountSetting setting = it.next();
			double trailingStop = setting.getTrailingStop();
			if(PriceUtils.isZero(trailingStop))
				continue;
			result.add(setting);
		}
		return result;
	}

    public List<String> getAllRouters(){
        List<String> list = new ArrayList<>();
        for (AccountSetting s : accountSettings.values()){
            String router = s.getRoute();
            if (router == null || router.equals(""))
                continue;
            if (!list.contains(router))
                list.add(router);
        }
        return list;
    }

    public List<Account> getAccountsByRouter(String router) throws AccountException {
        List<Account> list = new ArrayList<>();
        List<Account> accounts = getAllAccounts();
        for (Account account : accounts) {
            AccountSetting setting = getAccountSetting(account.getId());
            if (setting.getRoute().equals(router))
                list.add(account);
        }

        return list;
    }
}
