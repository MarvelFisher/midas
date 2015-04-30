package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.message.ErrorMessage;

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
	public AccountSetting setAccountSetting(AccountSetting setting) throws AccountException {
		if(!accounts.containsKey(setting.getId()))
			throw new AccountException("Account id doesn't exist: " + setting.getId(),ErrorMessage.ACCOUNT_NOT_EXIST);
		
		AccountSetting existing = accountSettings.get(setting.getId());
		if(null == existing) {
			existing = AccountSetting.createEmptySettings(setting.getId());
			accountSettings.put(setting.getId(), existing);
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
}
