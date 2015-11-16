package com.cyanspring.cstw.keepermanager;

import com.cyanspring.common.client.IInstrumentPoolKeeper;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.16
 *
 */
public final class InstrumentPoolKeeperManager {

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	public IInstrumentPoolKeeper getInstrumentPoolKeeper() {
		return instrumentPoolKeeper;
	}

	public void setInstrumentPoolKeeper(
			IInstrumentPoolKeeper instrumentPoolKeeper) {
		this.instrumentPoolKeeper = instrumentPoolKeeper;
	}

}
