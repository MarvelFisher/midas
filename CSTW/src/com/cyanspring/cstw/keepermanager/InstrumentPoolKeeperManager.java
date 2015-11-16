package com.cyanspring.cstw.keepermanager;

import com.cyanspring.common.client.IInstrumentPoolKeeper;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.16
 *
 */
public final class InstrumentPoolKeeperManager {

	private static InstrumentPoolKeeperManager instance;

	private IInstrumentPoolKeeper instrumentPoolKeeper;

	private IAsyncEventListener listener;

	public InstrumentPoolKeeperManager getInstance() {
		if (instance == null) {
			instance = new InstrumentPoolKeeperManager();
		}

		return instance;
	}

	public void init() {
		listener = new IAsyncEventListener() {
			@Override
			public void onEvent(AsyncEvent arg0) {
				// TODO updateEvent;
			}
		};

		// CSTWEventManager.subscribe(event, listener);
	}

	public IInstrumentPoolKeeper getInstrumentPoolKeeper() {
		return instrumentPoolKeeper;
	}

	public void setInstrumentPoolKeeper(
			IInstrumentPoolKeeper instrumentPoolKeeper) {
		this.instrumentPoolKeeper = instrumentPoolKeeper;
	}

}
