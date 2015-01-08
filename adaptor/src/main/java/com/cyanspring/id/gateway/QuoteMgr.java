package com.cyanspring.id.gateway; 

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.FinalizeHelper;

public class QuoteMgr implements AutoCloseable, TimerEventHandler {
	static QuoteMgr _Instance = new QuoteMgr();

	public static QuoteMgr Instance() {
		return _Instance;
	}

	static final String strXml = "<Symbols>\n"
			+ "	<Symbol ID=\"USDCAD,USDCHF,USDCNH,USDCZK,USDDKK,USDHKD,USDHUF,USDILS,USDJPY,USDMXN,USDNOK,USDPLN,USDRUB,USDSEK,USDSGD,NZDUSD,EURUSD,GBPUSD,AUDUSD\" />\n"
			+ "	<Symbol ID=\"EURAUD,EURCAD,EURCHF,EURCNH,EURCZK,EURDKK,EURGBP,EURHKD,EURILS,EURJPY,EURMXN,EURNOK,EURNZD,EURRUB,EURSEK,EURSGD\"/>\n"
			+ "	<Symbol ID=\"GBPAUD,GBPCAD,GBPCHF,GBPCNH,GBPDKK,GBPHKD,GBPJPY,GBPNOK,GBPNZD,GBPSEK\"/>\n"
			+ "	<Symbol ID=\"AUDCHF,AUDHKD,AUDJPY,AUDCAD,AUDCNH,AUDNZD,AUDSGD\"/>\n"
			+ "	<Symbol ID=\"CHFJPY,CHFCNH,CHFDKK,CHFNOK,CHFSEK\"/>\n"
			+ "	<Symbol ID=\"CNHJPY,DKKJPY,DKKNOK,DKKSEK,HKDJPY,MXNJPY,NOKJPY,NOKSEK,SEKJPY,SGDCNH,SGDJPY,NZDCHF,NZDJPY\"/>\n"
			+ "</Symbols>";

	public ArrayList<String> symbolList = new ArrayList<String>();

	TimerThread timer = new TimerThread();
	public QuoteMgr() {
		timer.TimerEvent = this;
		timer.setInterval(5000);
		timer.start();		
	}

	void fini() {
		synchronized (m_lock) {
			symbolList.clear();
		}		
		
		try {
			timer.close();
		} catch (Exception e) {
		}
	}

	public ArrayList<String> getSymbolList() {
		return new ArrayList<String>(symbolList);
	}

	public void close() {
		fini();
		FinalizeHelper.suppressFinalize(this);
	}

	Object m_lock = new Object();

	public boolean checkSymbol(String symbol) {
		
		if (symbolList.size() == 0)
			return true;
		
		synchronized(m_lock) {
			return symbolList.contains(symbol);
		}
	}
	
	public void addSymbols(List<String> list) {
		symbolList.addAll(list);
	}

	@Override
	public void onTimer(TimerThread objSender) {
		System.gc();		
	}

}
