package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;

public interface IChainListener {
	void onState(boolean on);
	// 不需要修改order 状态的回报
	void onOrder(String orderId, ExecType type, String message);
	// 需要修改 order 状态的回报
	void onOrder(String orderId, ExecType type , OrdStatus status, String message);
	// 部分成交
	void onOrder(String orderId, ExecType type , OrdStatus status, int volumeTraded, String message);
	void onError(String orderId, String message);
	void onError(String message);
}
