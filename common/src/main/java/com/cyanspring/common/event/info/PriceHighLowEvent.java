package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.PriceHighLow;

public class PriceHighLowEvent extends RemoteAsyncEvent {
	private List<PriceHighLow> listHighLow ;
	private boolean ok ;
	private String message ;
	private PriceHighLowType m_type;

	public PriceHighLowEvent(String key, String receiver) {
		super(key, receiver);
	}
	public PriceHighLowEvent(String key, String receiver,
			PriceHighLowType type, List<PriceHighLow> listHighLow) {
		super(key, receiver);
		this.m_type = type;
		this.listHighLow = listHighLow ;
	}
	public PriceHighLowType getType()
	{
		return m_type;
	}
	public void setType(PriceHighLowType type)
	{
		m_type = type;
	}
	public List<PriceHighLow> getListHighLow() {
		return listHighLow;
	}
	public void setListHighLow(List<PriceHighLow> listHighLow) {
		this.listHighLow = listHighLow;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
}
