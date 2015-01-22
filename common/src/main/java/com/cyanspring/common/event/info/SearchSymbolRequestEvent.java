package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SearchSymbolRequestEvent extends RemoteAsyncEvent {

	public SearchSymbolRequestEvent(String key, String receiver) {
		super(key, receiver);
		// TODO Auto-generated constructor stub
	}

	private String type;
	private String keyword;
	private String txId;
	private int page;
	private int symbolperpage;

	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}

	public int getSymbolPerPage() {
		return symbolperpage;
	}

	public void setSymbolPerPage(int symbolperpage) {
		this.symbolperpage = symbolperpage;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
