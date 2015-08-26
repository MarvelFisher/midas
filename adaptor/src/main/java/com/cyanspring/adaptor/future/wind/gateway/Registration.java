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
	
	public String getSubscribeMarket() {
		if(marketList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Market=");
		int iCount = 0;
		for(String market : marketList) {
			//if(other != null && other.hadMarket(market)) {
			//	continue;
			//}
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
	
    private boolean IsMFSymbol(String str) {
        int dp = str.indexOf(".");
        if(dp == -1 || str.length()- dp > 3) {
            return true;
        }
        return false;
    }
    private String ConvertMFSymbol(String str) {
        int dp = str.indexOf(".SHF");
        if (dp < 0)
        {
            dp = str.indexOf(".DCE");
        }
        if (dp > 0)
        {
            String code = str.substring(0, dp);
            String market = str.substring(dp);
            return code.toLowerCase() + market;
        }
        return str;
    }
	
    // 避免一個 Subscribe 太長,所以用 ArrayList 回
	public ArrayList<String> getSubscribeSymbol() {
		if(symbolList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBE|Symbol=");
		ArrayList<String>lst = new ArrayList<String>();
		int iCount = 0;
		synchronized(symbolList) {
			for(String symbol : symbolList) {
				if(iCount != 0) {
					strb.append(";");
				}
				if(IsMFSymbol(symbol))
				{
					continue;
				}
				strb.append(symbol);
				iCount += 1;
				if(iCount >= 512) {
					lst.add(strb.toString());
					strb = new StringBuilder("API=SUBSCRIBE|Symbol=");
					iCount = 0;
				}
			}
		}
		if(iCount > 0) {
			lst.add(strb.toString());
		}
		if(lst.size() == 0) {
			return null;
		}
		return lst;	
	}
	
	public ArrayList<String> getSubscribeSymbolMF() {
		if(symbolList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SUBSCRIBEMF|Symbol=");
		ArrayList<String> lst = new ArrayList<String>();
		int iCount = 0;
		for(String symbol : symbolList) {
			if(iCount != 0) {
				strb.append(";");
			}
			if(IsMFSymbol(symbol) == false)
			{
				continue;
			}								
			strb.append(ConvertMFSymbol(symbol)); // 因為 SHF , DCE 註冊時,商品代碼要用小寫,但是傳回來的商品代碼是大寫 !!!	
			iCount += 1;
			if(iCount >= 512) {
				lst.add(strb.toString());
				strb = new StringBuilder("API=SUBSCRIBEMF|Symbol=");
				iCount = 0;
			}			
		}
		if(iCount > 0) {
			lst.add(strb.toString());
		}		
		if(lst.size() == 0) {
			return null;
		}
		return lst;	
	}	
	
	public String getSubscribeTransaction() {
		if(transactionList.size() <= 0) {
			return null;
		}
		StringBuilder strb = new StringBuilder("API=SubsTrans|Symbol=");
		int iCount = 0;
		for(String symbol : transactionList) {
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
	

	
	private int symbolPosition(ArrayList<String>lst,String symbol,boolean bAdd) {
		int iPos;
		symbol = symbol.toUpperCase();  // 因為 SHF , DCE 註冊時,商品代碼要用小寫,但是傳回來的商品代碼是大寫 !!!
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
	
	public boolean hadTransaction(String symbol) {
		if(symbolPosition(transactionList,symbol,false) < 0) {
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
	
	public boolean removeTransaction(String symbol) {
		int iPos;
		synchronized(transactionList) {
			iPos = Collections.binarySearch(transactionList,symbol);
			if(iPos >= 0) {
				transactionList.remove(iPos);
				return true;
			}
		}
		return false;
	}
	
	public void clear() {
		synchronized(symbolList) {
			symbolList.clear();
		}
		marketList.clear();
		transactionList.clear();
	}
	public void addRegistration(Registration o) {
		synchronized(symbolList) {
			this.addSymbols(o.symbolList);
		}
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
				if(mpList.size() == 0) {
					return null;
				}
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
