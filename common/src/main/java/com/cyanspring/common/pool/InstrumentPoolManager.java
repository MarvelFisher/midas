package com.cyanspring.common.pool;

import com.cyanspring.common.IPlugin;

public class InstrumentPoolManager implements IPlugin {

	private InstrumentPoolKeeper keeper = new InstrumentPoolKeeper();
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub

	}

}
