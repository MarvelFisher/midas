package com.cyanspring.common.pool;

import java.util.HashMap;
import java.util.List;

import com.cyanspring.common.util.DualKeyMap;

public class InstrumentPoolKeeper {
	// k1 = ExchangeSubAccount Id; k2 = ExchangeAccount Id
	private DualKeyMap<String, String, ExchangeSubAccount> subAccounts = new DualKeyMap<String, String, ExchangeSubAccount>();
	// k1 = Instrument Pool id; k2 = ExchangeSubAccount Id
	private DualKeyMap<String, String, InstrumentPool> pools = new DualKeyMap<String, String, InstrumentPool>();
	
	// key is user account, NOT sub account
	private HashMap<String, List<InstrumentPool>> accountPools = new HashMap<String, List<InstrumentPool>>();
	
	
}
