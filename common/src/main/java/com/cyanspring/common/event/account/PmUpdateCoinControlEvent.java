package com.cyanspring.common.event.account;

import com.cyanspring.common.business.CoinControl;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmUpdateCoinControlEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private CoinControl coinControl;

	public PmUpdateCoinControlEvent(String key, String receiver,CoinControl coinControl) {
		super(key, receiver);
		this.coinControl = coinControl;
	}

	public CoinControl getCoinControl() {
		return coinControl;
	}
}
