package com.cyanspring.common.event.alert;

import com.cyanspring.common.alert.BasePriceAlert;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class SetPercentagePriceAlertRequestEvent extends RemoteAsyncEvent {
	private BasePriceAlert percentagePriceAlert;
	private AlertType type;
	private String txId;
	
//	PERCENTAGE_SET_NEW(7),
//	PERCENTAGE_SET_MODIFY(8),
//	PERCENTAGE_SET_CANCEL(9)
	
	public SetPercentagePriceAlertRequestEvent(String key, String receiver,
			BasePriceAlert percentagePriceAlert, String txId, AlertType type) {
		super(key, receiver);
		// TODO Auto-generated constructor stub
		this.setPercentagePriceAlert(percentagePriceAlert);
		this.txId = txId ;
		this.type = type;
	}

	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public BasePriceAlert getPercentagePriceAlert() {
		return percentagePriceAlert;
	}

	public void setPercentagePriceAlert(BasePriceAlert percentagePriceAlert) {
		this.percentagePriceAlert = percentagePriceAlert;
	}
	
	@Override
	public String toString()
	{
		return "Type : " + type.toString() + "," + percentagePriceAlert.toString() ;
	}

}
