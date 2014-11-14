package com.cyanspring.server.alert;

import com.cyanspring.common.business.Execution;

public interface ITradeAlertSender {
	void sendTradeAlert(Execution execution);
}
