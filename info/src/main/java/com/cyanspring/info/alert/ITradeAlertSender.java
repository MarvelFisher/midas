package com.cyanspring.info.alert;

import com.cyanspring.common.business.Execution;

public interface ITradeAlertSender {
	void sendTradeAlert(Execution execution);
}
