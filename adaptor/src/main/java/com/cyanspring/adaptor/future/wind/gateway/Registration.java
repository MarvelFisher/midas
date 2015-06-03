package com.cyanspring.adaptor.future.wind.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.cyanspring.Network.Transport.FDTFields;

public class Registration {

	private ArrayList<String> symbolList;
	private ArrayList<String> marketList;
	private ArrayList<String> transactionList;
	private ArrayList<HashMap<Integer,Object>> mpList = new ArrayList<HashMap<Integer,Object>>();
	
	Registration() {
		symbolList = new ArrayList<String>();
		marketList = new ArrayList<String>();
		transactionList = new ArrayList<String>();
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
		if(hadMarket(market)) {
			return false;
		}
		marketList.add(market);
		return true;
	}
	
	public boolean hadMarket(String market) {
		return marketList.contains(market);
	}
	
	public String getSubscribeMarket(Registration other) {
		if(marketList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Market=");
		int iCount = 0;
		for(String market : marketList) {
			if(other != null && other.hadMarket(market)) {
				continue;
			}
			if(iCount != 0) {
				strb.append(";");
			}
			strb.append(market);
			iCount += 1;
		}
		if(iCount == 0) {
			return null;
		}
		return strb.toString();
	}	
	
	
	public String getSubscribeSymbol(Registration other) {
		if(symbolList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Symbol=");
		int iCount = 0;
		for(String symbol : symbolList) {
			if(other != null && other.hadSymbolMarket(symbol)) {
				continue;
			}
			if(iCount != 0) {
				strb.append(";");
			}
			strb.append(symbol);
			iCount += 1;
		}
		if(iCount == 0) {
			return null;
		}
		return strb.toString();		
	}
	
	public boolean hadSymbol(String symbol) {
		if(hadSymbolMarket(symbol)) {
			return true;
		}
		if(symbolPosition(symbolList,symbol,false) < 0) {
			return false;
		}
		return true;
	}
	
	public boolean hadTransaction(String symbol) {
		if(symbolPosition(transactionList,symbol,false) < 0) {
			return false;
		}
		return true;		
	}
	
	private int symbolPosition(ArrayList<String>lst,String symbol,boolean bAdd) {
		int iPos;
		synchronized(lst) {
			iPos = Collections.binarySearch(lst,symbol);
			if(bAdd && iPos < 0) {
				lst.add(~iPos,symbol);
			}
		}
		return iPos;
	}
	
	
	public boolean addSymbol(String symbol) {
		if(hadSymbolMarket(symbol)) {
			return false;
		}
		int iPos = symbolPosition(symbolList,symbol,true);
		if(iPos >= 0) {
			return false;
		}
		return true;
	}
	
	public boolean addTransaction(String symbol) {
		int iPos = symbolPosition(transactionList,symbol,true);
		if(iPos >= 0) {
			return false;
		}
		return true;
	}
	
	public void clear() {
		symbolList.clear();
		marketList.clear();
		transactionList.clear();
	}
	public void addRegistration(Registration o) {
		this.addSymbols(o.symbolList);
		this.addMarkets(o.marketList);
		this.addTransaction(o.transactionList);
	}	
	private void addSymbols(ArrayList<String> lst) {
		for(String symbol : lst) {
			addSymbol(symbol);
		}
	}	
	private void addMarkets(ArrayList<String> lst) {
		for(String market : lst) {
			addMarket(market);
		}
	}
	private void addTransaction(ArrayList<String>lst) {
		for(String trans : lst) {
			addTransaction(trans);
		}
	}
	
	public int addMsgPack(HashMap<Integer,Object> mp) {
		if(mp != null) {
			synchronized(mpList) {
				mpList.add(mp);
			}
		}
		return mpList.size();
	}
	
	public HashMap<Integer,Object> flushMsgPack() {
		HashMap<Integer,Object> map = new HashMap<Integer, Object>();
		map.put(FDTFields.PacketType,FDTFields.PacketArray);
		synchronized(mpList) {
			if(mpList.size() == 1) {
				map = mpList.get(0);
			} else {
				map.put(FDTFields.ArrayOfPacket,  new ArrayList<HashMap<Integer,Object>>(mpList));
			}
			mpList.clear();
		}
		return map;
	}
	
	public int MsgPackArrayCount() {
		return mpList.size();
	}
	
}
