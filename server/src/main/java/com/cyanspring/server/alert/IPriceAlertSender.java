package com.cyanspring.server.alert;

import com.cyanspring.common.alert.PriceAlert;

public interface IPriceAlertSender {
	void sendPriceAlert(PriceAlert alert);
}
