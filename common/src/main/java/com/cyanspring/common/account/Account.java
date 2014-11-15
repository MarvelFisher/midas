package com.cyanspring.common.account;


public class Account extends BaseAccount implements Cloneable {
	
	protected Account() {
		super();
	}
	
	public Account(String id, String userId) {
		super(id, userId);
	}

	@Override
	public synchronized Account clone() throws CloneNotSupportedException {
		return (Account)super.clone();
	}
	

}
