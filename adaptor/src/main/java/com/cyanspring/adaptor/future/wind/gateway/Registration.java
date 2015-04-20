package com.cyanspring.adaptor.future.wind.gateway;

import java.util.ArrayList;
import java.util.Collections;

public class Registration {

	private ArrayList<String> symbolList;
	private ArrayList<String> marketList;
	
	Registration() {
		symbolList = new ArrayList<String>();
		marketList = new ArrayList<String>();
	}
	
	public boolean hadSymbolMarket(String symbol) {
		if(marketList.size() <= 0) {
			return false;
		}
		String[] symArray = symbol.split(".");
		if(symArray.length == 2 && marketList.contains(symArray[1])) {
			return true;
		}
		return false;
	}
	
	public boolean addMarket(String market) {
		if(marketList.contains(market)) {
			return false;
		}
		marketList.add(market);
		return true;
	}
	
	public String getSubscribeMarket() {
		if(marketList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Market=");
		int iCount = 0;
		for(String market : marketList) {
			if(iCount != 0) {
				strb.append(";");
			}
			strb.append(market);
			iCount += 1;
		}
		return strb.toString();
	}
	
	public String getSubscribeSymbol() {
		if(symbolList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Symbol=");
		int iCount = 0;
		for(String symbol : symbolList) {
			if(iCount != 0) {
				strb.append(";");
			}
			strb.append(symbol);
			iCount += 1;
		}
		return strb.toString();		
	}
	
	public boolean hadSymbol(String symbol) {
		if(hadSymbolMarket(symbol)) {
			return true;
		}
		if(symbolPosition(symbol) < 0) {
			return false;
		}
		return true;
	}
	
	private int symbolPosition(String symbol) {
		int iPos;
		synchronized(symbolList) {
			iPos = Collections.binarySearch(symbolList,symbol);
		}
		return iPos;
	}
	
	public boolean addSymbol(String symbol) {
		if(hadSymbolMarket(symbol)) {
			return false;
		}
		int iPos = symbolPosition(symbol);
		if(iPos >= 0) {
			return false;
		}

		synchronized(symbolList) {
			symbolList.add(~iPos,symbol);
		}
		return true;
	}
	public void clear() {
		symbolList.clear();
		marketList.clear();
	}
	public void addRegistration(Registration o) {
		this.addSymbols(o.symbolList);
		this.addMarkets(o.marketList);
	}	
	private void addSymbols(ArrayList<String> symbolList) {
		for(String symbol : symbolList) {
			addSymbol(symbol);
		}
	}	
	private void addMarkets(ArrayList<String> marketlList) {
		for(String market : marketList) {
			addMarket(market);
		}
	}
}
