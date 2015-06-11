package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;

public interface IChainListener {
	void onState(boolean on);
	void onOrder(String orderId, ExecType type , OrdStatus status, String message);
	void onError(String orderId, String message);
	void onError(String message);
}
