package com.cyanspring.common.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.util.DualKeyMap;

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

	@Autowired
	private AccountKeeper accountKeeper;

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

	public List<ExchangeAccount> getExchangeAccountList() {
		return new ArrayList<ExchangeAccount>(exchAccMap.values());
	}

	public List<ExchangeSubAccount> getExchangeSubAccountList(
			String exchangeAccount) {
		return new ArrayList<ExchangeSubAccount>(subAccountMap.getMap(
				exchangeAccount).values());
	}

	public List<InstrumentPool> getInstrumentPoolList(String exchangeSubAccount) {
		return new ArrayList<InstrumentPool>(poolSubAccountMap.getMap(
				exchangeSubAccount).values());
	}

	public InstrumentPool getInstrumentPool(String instrumentPool) {
		return poolSubAccountMap.get(instrumentPool);
	}

	public List<InstrumentPoolRecord> getInstrumentPoolRecordList(
			String instrumentPool) {
		return new ArrayList<InstrumentPoolRecord>(instrumentPoolRecordMap.get(
				instrumentPool).values());
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

	public boolean ifExists(ExchangeAccount exchangeAccount) {
		return exchAccMap.containsKey(exchangeAccount.getId());
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

	public boolean ifExists(ExchangeSubAccount exchangeSubAccount) {
		return subAccountMap.containsKey(exchangeSubAccount.getId());
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

	public boolean ifExists(InstrumentPool instrumentPool) {
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
}
