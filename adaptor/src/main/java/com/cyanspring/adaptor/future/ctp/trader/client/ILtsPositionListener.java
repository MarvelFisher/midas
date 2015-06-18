package com.cyanspring.adaptor.future.ctp.trader.client;

import com.cyanspring.adaptor.future.ctp.trader.generated.CThostFtdcInvestorPositionField;

public interface ILtsPositionListener {
	void onQryPosition(CThostFtdcInvestorPositionField field, boolean isLast);
}
