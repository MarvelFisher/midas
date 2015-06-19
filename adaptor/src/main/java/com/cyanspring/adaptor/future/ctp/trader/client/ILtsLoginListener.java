package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcRspUserLoginField;

public interface ILtsLoginListener {
	void onLogin(CThostFtdcRspUserLoginField field);
	void onDisconnect();
}
