package com.cyanspring.common.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.util.DualKeyMap;
import com.cyanspring.common.util.IdGenerator;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class InstrumentPoolKeeper implements IInstrumentPoolKeeper {

	// k=ExchangeAccount id; v=ExchangeAccount
	private Map<String, ExchangeAccount> exchAccMap = new HashMap<String, ExchangeAccount>();

	// k1=ExchangeSubAccount Id; k2=ExchangeAccount Id; v=ExchangeSubAccount
	private DualKeyMap<String, String, ExchangeSubAccount> subAccountMap = new DualKeyMap<String, String, ExchangeSubAccount>();

	// k1=InstrumentPool id; k2=ExchangeSubAccount Id; v=InstrumentPool
	private DualKeyMap<String, String, InstrumentPool> poolSubAccountMap = new DualKeyMap<String, String, InstrumentPool>();

	// k1=InstrumentPool id; k2=symbol; v=InstrumentPoolRecord
	private Map<String, Map<String, InstrumentPoolRecord>> instrumentPoolRecordMap = new ConcurrentHashMap<String, Map<String, InstrumentPoolRecord>>();

	// k1=User id; k2=ExchangeSubAccount id; v=ExchangeSubAccount
	private Map<String, Map<String, ExchangeSubAccount>> userSubAccounMap = new ConcurrentHashMap<String, Map<String, ExchangeSubAccount>>();

	/**
	 * 根据交易员账号和输入的股票，返回其对应的ExchangeSubAccountId和股票池信息List<InstrumentPoolRecord>
	 * 
	 * @param account
	 * @param symbol
	 * @return Map -> k=ExchangeSubAccount id
	 */
	@Override
	public Map<String, List<InstrumentPoolRecord>> getSubAccountInstrumentPoolRecordMap(
			Account account, String symbol) {
		InstrumentPool instrumentPool = null;
		Map<String, InstrumentPoolRecord> tempInstrumentPoolRecordMap = null;
		Map<String, List<InstrumentPoolRecord>> subAccountInstrumentPoolRecordMap = new HashMap<String, List<InstrumentPoolRecord>>();
		for (String instrumentPoolId : account.getInstrumentPools()) {
			instrumentPool = poolSubAccountMap.get(instrumentPoolId);
			tempInstrumentPoolRecordMap = instrumentPoolRecordMap
					.get(instrumentPool.getId());
			if (!tempInstrumentPoolRecordMap.isEmpty()
					&& tempInstrumentPoolRecordMap.containsKey(symbol)) {
				if (!subAccountInstrumentPoolRecordMap
						.containsKey(instrumentPool.getExchangeSubAccount())) {
					subAccountInstrumentPoolRecordMap.put(
							instrumentPool.getExchangeSubAccount(),
							new ArrayList<InstrumentPoolRecord>());
				}
				subAccountInstrumentPoolRecordMap.get(
						instrumentPool.getExchangeSubAccount()).add(
						tempInstrumentPoolRecordMap.get(symbol));
			}
		}
		return subAccountInstrumentPoolRecordMap;
	}

	@Override
	public ExchangeAccount getExchangeAccount(String exchangeAccountId) {
		return exchAccMap.get(exchangeAccountId);
	}

	@Override
	public List<ExchangeAccount> getExchangeAccountList() {
		return new ArrayList<ExchangeAccount>(exchAccMap.values());
	}

	@Override
	public List<ExchangeSubAccount> getExchangeSubAccountList(
			String exchangeAccount) {
		return new ArrayList<ExchangeSubAccount>(subAccountMap.getMap(
				exchangeAccount).values());
	}

	@Override
	public List<ExchangeSubAccount> getAllSubAccountList() {
		return new ArrayList<ExchangeSubAccount>(subAccountMap.values());
	}

	@Override
	public List<InstrumentPool> getInstrumentPoolList(String exchangeSubAccount) {
		return new ArrayList<InstrumentPool>(poolSubAccountMap.getMap(
				exchangeSubAccount).values());
	}

	public InstrumentPool getInstrumentPool(String instrumentPool) {
		return poolSubAccountMap.get(instrumentPool);
	}

	@Override
	public List<String> getAssignedAdminsBySubAccount(String subAccount) {
		List<String> users = new ArrayList<String>();
		for (Entry<String, Map<String, ExchangeSubAccount>> entry : userSubAccounMap
				.entrySet()) {
			if (entry.getValue().containsKey(subAccount)
					&& !users.contains(entry.getKey())) {
				users.add(entry.getKey());
			}
		}
		return users;
	}

	@Override
	public List<ExchangeSubAccount> getAssignedSubAccounts(String user) {
		List<ExchangeSubAccount> subAccounts = new ArrayList<ExchangeSubAccount>();
		if (userSubAccounMap.containsKey(user)) {
			subAccounts.addAll(userSubAccounMap.get(user).values());
		}
		return subAccounts;
	}

	@Override
	public List<InstrumentPoolRecord> getInstrumentPoolRecordList(String id,
			ModelType type) {
		List<InstrumentPoolRecord> instrumentPoolRecords = new ArrayList<InstrumentPoolRecord>();
		switch (type) {
		case EXCHANGE_ACCOUNT:
			Map<String, ExchangeSubAccount> tempSubAccountMap = subAccountMap
					.getMap(id);
			if (tempSubAccountMap != null && !tempSubAccountMap.isEmpty()) {
				for (ExchangeSubAccount subAccount : tempSubAccountMap.values()) {
					Map<String, InstrumentPool> tempInstrumentPoolMap = poolSubAccountMap
							.getMap(subAccount.getId());
					if (tempInstrumentPoolMap != null
							&& !tempInstrumentPoolMap.isEmpty()) {
						for (InstrumentPool instrumentPool : tempInstrumentPoolMap
								.values()) {
							if (instrumentPoolRecordMap
									.containsKey(instrumentPool.getId())) {
								instrumentPoolRecords
										.addAll(instrumentPoolRecordMap.get(
												instrumentPool.getId())
												.values());
							}
						}
					}
				}
			}
			break;
		case EXCHANGE_SUB_ACCOUNT:
			for (InstrumentPool instrumentPool : poolSubAccountMap.getMap(id)
					.values()) {
				instrumentPoolRecords.addAll(instrumentPoolRecordMap.get(
						instrumentPool.getId()).values());
			}
			break;
		case INSTRUMENT_POOL:
			instrumentPoolRecords.addAll(instrumentPoolRecordMap.get(id)
					.values());
			break;
		}
		return instrumentPoolRecords;
	}

	/**
	 * 根据InstrumentPoolId和Symbol获取对应股票数量的InstrumentPoolRecord
	 * 
	 * @param instrumentPoolId
	 * @param symbol
	 * @return InstrumentPoolRecord
	 */
	@Override
	public InstrumentPoolRecord getInstrumentPoolRecord(
			String instrumentPoolId, String symbol) {
		return instrumentPoolRecordMap.get(instrumentPoolId).get(symbol);
	}

	/**
	 * 更新股票池数量InstrumentPoolRecord
	 * 
	 * @param instrumentPoolRecord
	 */
	@Override
	public void update(InstrumentPoolRecord instrumentPoolRecord) {
		instrumentPoolRecordMap.get(instrumentPoolRecord.getInstrumentPoolId())
				.put(instrumentPoolRecord.getSymbol(), instrumentPoolRecord);
	}

	public boolean ifIdExists(ExchangeAccount exchangeAccount) {
		return exchAccMap.containsKey(exchangeAccount.getId());
	}

	public boolean ifNameExists(ExchangeAccount exchangeAccount) {
		for (ExchangeAccount tempExchangeAccount : exchAccMap.values()) {
			if (tempExchangeAccount.getName().equals(
					exchangeAccount.getName().trim())) {
				return true;
			}
		}
		return false;
	}

	public void add(ExchangeAccount exchangeAccount) {
		exchAccMap.put(exchangeAccount.getId(), exchangeAccount);
	}

	public void delete(ExchangeAccount exchangeAccount) {
		exchAccMap.remove(exchangeAccount.getId());
	}

	public void update(ExchangeAccount exchangeAccount) {
		exchAccMap.put(exchangeAccount.getId(), exchangeAccount);
	}

	public boolean ifIdExists(ExchangeSubAccount exchangeSubAccount) {
		return subAccountMap.containsKey(exchangeSubAccount.getId());
	}

	public boolean ifNameExists(ExchangeSubAccount exchangeSubAccount) {
		for (ExchangeSubAccount tempSubAccount : subAccountMap.values()) {
			if (tempSubAccount.getName().equals(
					exchangeSubAccount.getName().trim())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ExchangeSubAccount getSubAccountById(String subAccountId) {
		return subAccountMap.get(subAccountId);
	}

	public void add(ExchangeSubAccount exchangeSubAccount) {
		subAccountMap.put(exchangeSubAccount.getId(),
				exchangeSubAccount.getExchangeAccount(), exchangeSubAccount);
	}

	public void update(ExchangeSubAccount exchangeSubAccount) {
		subAccountMap.put(exchangeSubAccount.getId(),
				exchangeSubAccount.getExchangeAccount(), exchangeSubAccount);
	}

	public void delete(ExchangeSubAccount exchangeSubAccount) {
		subAccountMap.remove(exchangeSubAccount.getId(),
				exchangeSubAccount.getExchangeAccount());
	}

	public boolean ifIdExists(InstrumentPool instrumentPool) {
		return poolSubAccountMap.containsKey(instrumentPool.getId());
	}

	public boolean ifNameExists(InstrumentPool instrumentPool) {
		for (InstrumentPool tempInstrumentPool : poolSubAccountMap.getMap(
				instrumentPool.getExchangeSubAccount()).values()) {
			if (tempInstrumentPool.getName().equals(instrumentPool.getName())) {
				return true;
			}
		}
		return false;
	}

	public void add(InstrumentPool instrumentPool) {
		poolSubAccountMap.put(instrumentPool.getId(),
				instrumentPool.getExchangeSubAccount(), instrumentPool);
	}

	public void delete(InstrumentPool instrumentPool) {
		poolSubAccountMap.remove(instrumentPool.getId(),
				instrumentPool.getExchangeSubAccount());
		if (instrumentPoolRecordMap.containsKey(instrumentPool.getId())) {
			instrumentPoolRecordMap.remove(instrumentPool.getId());
		}
	}

	public void add(List<InstrumentPoolRecord> instrumentPoolRecords) {
		for (InstrumentPoolRecord record : instrumentPoolRecords) {
			if (!instrumentPoolRecordMap.containsKey(record
					.getInstrumentPoolId())) {
				instrumentPoolRecordMap.put(record.getInstrumentPoolId(),
						new ConcurrentHashMap<String, InstrumentPoolRecord>());
			}
			instrumentPoolRecordMap.get(record.getInstrumentPoolId()).put(
					record.getSymbol(), record);
		}
	}

	public void delete(List<InstrumentPoolRecord> instrumentPoolRecords) {
		for (InstrumentPoolRecord record : instrumentPoolRecords) {
			instrumentPoolRecordMap.get(record.getInstrumentPoolId()).remove(
					record.getSymbol());
		}
		if (!instrumentPoolRecords.isEmpty()
				&& instrumentPoolRecordMap.get(
						instrumentPoolRecords.get(0).getInstrumentPoolId())
						.isEmpty()) {
			instrumentPoolRecordMap.remove(instrumentPoolRecords.get(0)
					.getInstrumentPoolId());
		}
	}

	public boolean ifExists(InstrumentPoolRecord record) {
		if (instrumentPoolRecordMap.containsKey(record.getInstrumentPoolId())
				&& instrumentPoolRecordMap.get(record.getInstrumentPoolId())
						.containsKey(record.getSymbol())) {
			return true;
		}
		return false;
	}

	public boolean ifExists(UserExchangeSubAccount userSubAccount) {
		if (userSubAccounMap.containsKey(userSubAccount.getUser())
				&& userSubAccounMap.get(userSubAccount.getUser()).containsKey(
						userSubAccount.getExchangeSubAccount())) {
			return true;
		}
		return false;
	}

	public void add(UserExchangeSubAccount userExchangeSubAccount) {
		if (!userSubAccounMap.containsKey(userExchangeSubAccount.getUser())) {
			userSubAccounMap.put(userExchangeSubAccount.getUser(),
					new ConcurrentHashMap<String, ExchangeSubAccount>());
		}
		userSubAccounMap.get(userExchangeSubAccount.getUser()).put(
				userExchangeSubAccount.getExchangeSubAccount(),
				subAccountMap.get(userExchangeSubAccount
						.getExchangeSubAccount()));
	}

	public void delete(UserExchangeSubAccount userExchangeSubAccount) {
		Map<String, ExchangeSubAccount> tempSubAccountMap = userSubAccounMap
				.get(userExchangeSubAccount.getUser());
		tempSubAccountMap
				.remove(userExchangeSubAccount.getExchangeSubAccount());
		if (tempSubAccountMap.isEmpty()) {
			userSubAccounMap.remove(userExchangeSubAccount.getUser());
		}
	}

	public void injectExchangeAccounts(List<ExchangeAccount> exchangeAccounts) {
		for (ExchangeAccount exchangeAccount : exchangeAccounts) {
			exchAccMap.put(exchangeAccount.getId(), exchangeAccount);
		}
	}

	public void injectExchangeSubAccounts(
			List<ExchangeSubAccount> exchangeSubAccounts) {
		for (ExchangeSubAccount exchangeSubAccount : exchangeSubAccounts) {
			subAccountMap
					.put(exchangeSubAccount.getId(),
							exchangeSubAccount.getExchangeAccount(),
							exchangeSubAccount);
		}
	}

	public void injectInstrumentPools(List<InstrumentPool> instrumentPools) {
		for (InstrumentPool instrumentPool : instrumentPools) {
			poolSubAccountMap.put(instrumentPool.getId(),
					instrumentPool.getExchangeSubAccount(), instrumentPool);
		}
	}

	public void injectAccountPools(List<AccountPool> accountPools) {
		for (AccountPool accountPool : accountPools) {
			poolSubAccountMap.get(accountPool.getInstrumentPool()).add(
					accountPool);
		}
	}

	public void injectInstrumentPoolRecords(
			List<InstrumentPoolRecord> instrumentPoolRecords) {
		for (InstrumentPoolRecord record : instrumentPoolRecords) {
			if (!instrumentPoolRecordMap.containsKey(record
					.getInstrumentPoolId())) {
				instrumentPoolRecordMap.put(record.getInstrumentPoolId(),
						new ConcurrentHashMap<String, InstrumentPoolRecord>());
			}
			instrumentPoolRecordMap.get(record.getInstrumentPoolId()).put(
					record.getSymbol(), record);
		}
	}

	public void injectUserExchangeSubAccounts(
			List<UserExchangeSubAccount> userExchangeSubAccounts) {
		for (UserExchangeSubAccount subAccount : userExchangeSubAccounts) {
			if (!userSubAccounMap.containsKey(subAccount.getUser())) {
				userSubAccounMap.put(subAccount.getUser(),
						new ConcurrentHashMap<String, ExchangeSubAccount>());
			}
			userSubAccounMap.get(subAccount.getUser()).put(
					subAccount.getExchangeSubAccount(),
					subAccountMap.get(subAccount.getExchangeSubAccount()));
		}
	}

	public String genNextExchangeAccountId() {
		return "EX" + IdGenerator.getInstance().getNextID();
	}

	public String genNextExchangeSubAccountId() {
		return "SA" + IdGenerator.getInstance().getNextID();
	}

	public String genNextInstrumentPoolId() {
		return "PO" + IdGenerator.getInstance().getNextID();
	}

}
