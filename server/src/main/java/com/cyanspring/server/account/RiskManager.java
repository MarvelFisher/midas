package com.cyanspring.server.account;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;

public class RiskManager implements IPlugin{
	
	@Autowired(required=false)
	private LiveTradingChecker liveTradingChecker;
	

	@Override
	public void init() throws Exception {
		
	}

	@Override
	public void uninit() {
		
	}

	public LiveTradingChecker getLiveTradingChecker() {
		return liveTradingChecker;
	}

	private void setLiveTradingChecker(LiveTradingChecker liveTradingChecker) {
		this.liveTradingChecker = liveTradingChecker;
	}
	
}
