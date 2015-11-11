package com.cyanspring.common.pool;

import java.util.List;
import java.util.Map;

public class InstrumentPool {
	String id;
	String exchangeSubAccount;
	// k=Account id
	List<String> accounts;
	// k=Symbol; v=InstrumentPoolRecord
	Map<String, InstrumentPoolRecord> instrumentPoolRecords;

	public InstrumentPool(String id, String exchangeSubAccount) {
		super();
		this.id = id;
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public String getId() {
		return id;
	}

	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setExchangeSubAccount(String exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public Map<String, InstrumentPoolRecord> getInstrumentPoolRecords() {
		return instrumentPoolRecords;
	}

	public void setInstrumentPoolRecords(
			Map<String, InstrumentPoolRecord> instrumentPoolRecords) {
		this.instrumentPoolRecords = instrumentPoolRecords;
	}

	public InstrumentPoolRecord getInstrumentPoolRecord(String symbol) {
		return instrumentPoolRecords.get(symbol);
	}

	public void update(InstrumentPoolRecord instrumentPoolRecord) {
		instrumentPoolRecords.put(instrumentPoolRecord.getSymbol(),
				instrumentPoolRecord);
	}

	public void add(InstrumentPoolRecord instrumentPoolRecord) {
		instrumentPoolRecords.put(instrumentPoolRecord.getSymbol(),
				instrumentPoolRecord);
	}

	public void delete(InstrumentPoolRecord instrumentPoolRecord) {
		instrumentPoolRecords.remove(instrumentPoolRecord.getSymbol());
	}

	public List<String> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}

	public void add(AccountPool accountPool) {
		accounts.add(accountPool.getAccount());
	}

	public void delete(AccountPool accountPool) {
		accounts.remove(accountPool.getAccount());
	}
}
