package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.RemoteAsyncEvent;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;

public class MultiQuoteExtendEvent extends RemoteAsyncEvent {

	private HashMap<String,DataObject> data;
	private Date tradeDate;
	private int offSet = -1;
	private int totalDataCount = -1;

	public MultiQuoteExtendEvent(String key, String receiver, HashMap data, Date tradeDate) {
		super(key, receiver);
		this.data = data;
		this.tradeDate = tradeDate;
	}

	public Date getTradeDate() {
		return tradeDate;
	}

	public HashMap<String,DataObject> getMutilQuoteExtend(){
		return data;
	}

	public int getOffSet() {
		return offSet;
	}

	public void setOffSet(int offSet) {
		this.offSet = offSet;
	}

	public int getTotalDataCount() {
		return totalDataCount;
	}

	public void setTotalDataCount(int totalDataCount) {
		this.totalDataCount = totalDataCount;
	}
}
