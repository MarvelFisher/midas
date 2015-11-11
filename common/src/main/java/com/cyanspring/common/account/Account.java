package com.cyanspring.common.account;

import java.util.List;

import com.cyanspring.common.pool.AccountPool;

public class Account extends BaseAccount implements Cloneable {
	private double urLastPnL;

	// InstrumentPool id
	private List<String> instrumentPools;

	protected Account() {
		super();
	}

	public Account(String id, String userId) {
		super(id, userId);
	}

	@Override
	public synchronized Account clone() throws CloneNotSupportedException {
		return (Account) super.clone();
	}

	public double getUrLastPnL() {
		return urLastPnL;
	}

	public void setUrLastPnL(double urLastPnL) {
		this.urLastPnL = urLastPnL;
	}

	public List<String> getInstrumentPools() {
		return instrumentPools;
	}

	public void setInstrumentPools(List<String> instrumentPools) {
		this.instrumentPools = instrumentPools;
	}

	public void add(AccountPool accountPool) {
		instrumentPools.add(accountPool.getInstrumentPool());
	}

	public void delete(AccountPool accountPool) {
		instrumentPools.remove(accountPool.getInstrumentPool());
	}
}
