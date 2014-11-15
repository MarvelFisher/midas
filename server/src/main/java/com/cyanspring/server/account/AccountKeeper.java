package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;

public class AccountKeeper {
	private static final Logger log = LoggerFactory
			.getLogger(AccountKeeper.class);
	
	private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<String, Account>();
	private ConcurrentHashMap<String, List<Account>> userAccounts = new ConcurrentHashMap<String, List<Account>>();
	private List<Account> jobs = new ArrayList<Account>();
	private ConcurrentHashMap<String, AccountSetting> accountSettings = 
				new ConcurrentHashMap<String, AccountSetting>();
	private int jobIndex = 0;
	private int jobBatch = 50;
	
	public AccountSetting getAccountSetting(String account) throws AccountException {
		if(!accounts.containsKey(account))
			throw new AccountException("Account id doesn't exist: " + account);
		
		AccountSetting setting = accountSettings.get(account);
		if(null == setting)
			return null;
		
		synchronized(setting) {
			return setting.clone();
		}
	}
	
	public AccountSetting setAccountSetting(AccountSetting setting) throws AccountException {
		if(!accounts.containsKey(setting.getId()))
			throw new AccountException("Account id doesn't exist: " + setting.getId());
		
		AccountSetting existing = accountSettings.putIfAbsent(setting.getId(), setting);
		if(null != existing) {
			synchronized(existing) {
				for(Entry<String, Object> entry: setting.getFields().entrySet()) {
					existing.getFields().put(entry.getKey(), entry.getValue());
				}
			}
			return existing;
		}
		
		return setting;
	}
	
	private void addAccount(Account account) {
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
		jobs.add(account);
	}
	
	public void createAccount(Account account) throws AccountException {
		if(accounts.containsKey(account.getId()))
			throw new AccountException("Account already exists: " + account.getId());
		if(null == account.getCurrency())
			account.setCurrency(Default.getCurrency());
		if(PriceUtils.isZero(account.getCash()))
				account.addCash(Default.getAccountCash());
		account.addMargin(account.getCash() * Default.getMarginTimes());
		account.setActive(true);
		addAccount(account);
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
		while(i<jobs.size()) { //account should never be removed in real time so thread safe here
			result.add(jobs.get(i));
			i++;
		}
		return result;
	}
	
	public int getJobBatch() {
		return jobBatch;
	}

	public void setJobBatch(int jobBatch) {
		this.jobBatch = jobBatch;
	}

	public List<Account> getJobs() {
		List<Account> result = new ArrayList<Account>();
		int i = 0;
		while(i < this.jobBatch && i < jobs.size()) {
			result.add(jobs.get(this.jobIndex++));
			if(this.jobIndex>=jobs.size())
				this.jobIndex = 0;
			i++;
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
}
