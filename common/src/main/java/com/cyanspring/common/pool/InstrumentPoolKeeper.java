package com.cyanspring.common.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.util.DualKeyMap;

/**
 * @author GuoWei
 * @since 09/11/2015
 */
public class InstrumentPoolKeeper implements IInstrumentPoolKeeper {

	// k=ExchangeAccount id; v=ExchangeAccount
	private Map<String, ExchangeAccount> exchAccMap = new HashMap<String, ExchangeAccount>();

	// k1=ExchangeSubAccount Id; k2=ExchangeAccount Id; v=ExchangeSubAccount
	private DualKeyMap<String, String, ExchangeSubAccount> subAccountMap = new DualKeyMap<String, String, ExchangeSubAccount>();

	// k1=InstrumentPool id; k2=ExchangeSubAccount Id; v=InstrumentPool
	private DualKeyMap<String, String, InstrumentPool> poolSubAccountMap = new DualKeyMap<String, String, InstrumentPool>();

	// k1=InstrumentPoolRecord id; k2=InstrumentPool id; v=InstrumentPoolRecord
	private DualKeyMap<String, String, InstrumentPoolRecord> instrumentPoolRecordMap = new DualKeyMap<String, String, InstrumentPoolRecord>();
	
	@Autowired
	private AccountKeeper accountKeeper;

	public List<ExchangeAccount> getExchangeAccountList() {
		return new ArrayList<ExchangeAccount>(exchAccMap.values());
	}

	public List<ExchangeSubAccount> getExchangeSubAccountList(
			ExchangeAccount exchangeAccount) {
		return new ArrayList<ExchangeSubAccount>(subAccountMap.getMap(
				exchangeAccount.getId()).values());
	}

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
		Map<String, InstrumentPoolRecord> instrumentPoolRecordMap = null;
		Map<String, List<InstrumentPoolRecord>> subAccountInstrumentPoolRecordMap = new HashMap<String, List<InstrumentPoolRecord>>();
		for (String instrumentPoolId : account.getInstrumentPools()) {
			instrumentPool = poolSubAccountMap.get(instrumentPoolId);
			instrumentPoolRecordMap = instrumentPool.getInstrumentPoolRecords();
			if (!instrumentPoolRecordMap.isEmpty()
					&& instrumentPoolRecordMap.containsKey(symbol)) {
				if (!subAccountInstrumentPoolRecordMap
						.containsKey(instrumentPool.getExchangeSubAccount())) {
					subAccountInstrumentPoolRecordMap.put(
							instrumentPool.getExchangeSubAccount(),
							new ArrayList<InstrumentPoolRecord>());
				}
				subAccountInstrumentPoolRecordMap.get(
						instrumentPool.getExchangeSubAccount()).add(
						instrumentPoolRecordMap.get(symbol));
			}
		}
		return subAccountInstrumentPoolRecordMap;
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
		return poolSubAccountMap.get(instrumentPoolId).getInstrumentPoolRecord(
				symbol);
	}

	/**
	 * 更新股票池数量InstrumentPoolRecord
	 * 
	 * @param instrumentPoolRecord
	 */
	@Override
	public void update(InstrumentPoolRecord instrumentPoolRecord) {
		poolSubAccountMap.get(instrumentPoolRecord.getInstrumentPoolId())
				.update(instrumentPoolRecord);
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
			if (poolSubAccountMap.containsKey(record.getInstrumentPoolId())) {
				poolSubAccountMap.get(record.getInstrumentPoolId()).add(record);
			}
		}
	}

}
