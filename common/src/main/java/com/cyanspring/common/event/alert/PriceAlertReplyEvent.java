package com.cyanspring.common.event.alert;

import java.util.List;

import com.cyanspring.common.alert.PriceAlert;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PriceAlertReplyEvent extends RemoteAsyncEvent {
	private List<PriceAlert> PriceAlertList;
	private String id;
	private String txId;
	private String userId;
	private AlertType type;
	private boolean ok;
	private String message;
	/*AlertType :
	 * 	PRICE_SET_NEW(1),
	 * 	PRICE_SET_MODIFY(2),
	 * 	PRICE_SET_CANCEL(3),
	 *  PRICE_QUERY_CUR(4),
	 *  PRICE_QUERY_PAST(5),
	 * */
	/*
	 * if Success , ok = true ,message = "";
	 * if reject , ok = false ,message = error msg ;
	 * */
	public PriceAlertReplyEvent(String key, String receiver,
			String id, String txId,String userId, AlertType type, List<PriceAlert> PriceAlertList, boolean ok, String message) {
		super(key, receiver);
		this.setId(id);
		this.setTxId(txId);
		this.setUserId(userId);
		this.setType(type);
		this.setPriceAlert(PriceAlertList);
		this.setOk(ok);
		this.setMessage(message);
	}
	public String getId(){
		return id ;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<PriceAlert> getPriceAlert() {
		return PriceAlertList;
	}
	public void setPriceAlert(List<PriceAlert> priceAlert) {
		PriceAlertList = priceAlert;
	}
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public AlertType getType() {
		return type;
	}
	public void setType(AlertType type) {
		this.type = type;
	}
}
