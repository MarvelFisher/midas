package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcOrderField;

public interface ILtsQryOrderListener {
	void onQryOrder(CThostFtdcOrderField field, boolean isLast);
}
