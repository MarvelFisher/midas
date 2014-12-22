package com.cyanspring.info.alert;

import com.cyanspring.common.alert.PriceAlert;

public interface IPriceAlertSender {
	void sendPriceAlert(PriceAlert alert);
}
