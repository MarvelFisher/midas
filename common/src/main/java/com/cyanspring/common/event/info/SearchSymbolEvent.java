package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.SymbolInfo;

public class SearchSymbolEvent extends RemoteAsyncEvent {

	public SearchSymbolEvent(String key, String receiver) {
		super(key, receiver);
		// TODO Auto-generated constructor stub
	}
	public SearchSymbolEvent(String key, String receiver,
			String type, String txId, int page, int symbolperpage, int totalpage) {
		super(key, receiver);
	}
	public SearchSymbolEvent(String key, String receiver, SearchSymbolRequestEvent event) {
		super(key, receiver);
		this.setType(event.getType());
		this.setKeyword(event.getKeyword());
		this.setMarket(event.getMarket());
		this.setTxId(event.getTxId());
		this.setPage(event.getPage());
		this.setSymbolPerPage(event.getSymbolPerPage());
	}

	private SearchSymbolType type;
	private String keyword;
	private String market;
	private String txId;
	private int page;
	private int symbolperpage;
	private int totalpage;
	private List<SymbolInfo> symbolinfo;
	private boolean ok;
	private String message;

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

	public SearchSymbolType getType() {
		return type;
	}

	public void setType(SearchSymbolType type) {
		this.type = type;
	}
	public List<SymbolInfo> getSymbolinfo() {
		return symbolinfo;
	}
	public void setSymbolinfo(List<SymbolInfo> symbolinfo) {
		this.symbolinfo = symbolinfo;
	}
	public int getTotalpage() {
		return totalpage;
	}
	public void setTotalpage(int totalpage) {
		this.totalpage = totalpage;
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
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
}
