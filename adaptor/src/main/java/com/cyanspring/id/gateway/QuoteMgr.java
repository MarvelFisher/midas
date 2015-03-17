package com.cyanspring.id.gateway; 

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.FileMgr;
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
	
	public ArrayList<String> allSymbol = new ArrayList<String>();
	
	TimerThread timer = new TimerThread();
	public QuoteMgr() {
		timer.setName("QuoteMgr.Timer");
		timer.TimerEvent = this;
		timer.setInterval(300000);
		timer.start();		
	}

	void uninit() {
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

	public void updateAllSymbol(String symbol) {
		int iPos;
		synchronized (allSymbol) {		
			iPos = Collections.binarySearch(allSymbol,symbol);
			if ( iPos >= 0)
				return;					
			allSymbol.add(~iPos,symbol);
		}
	}
	
	public void dumpSymbols() {
		ArrayList<String> list = new ArrayList<String>();		
		String[] arrSymbol = new String[allSymbol.size()];
		
		synchronized (allSymbol) {
			allSymbol.toArray(arrSymbol);
			//list.addAll(allSymbol);
		}	
		
		for (String symbol : arrSymbol ) {
			if(symbol.contains("X:F") || symbol.contains("X:O"))
				continue;
			
			list.add(String.format("%s%n", symbol));
		}
		Collections.sort(list);
		String path = getPath("symbol.txt");
		IdGateway.instance.addLog(InfoString.Info, "%s", path);	
		FileMgr.writeFile(path, list, true);
		list.clear();
	}
	
	public static String getPath(String strName) {
		Path path = Paths.get("");
		return String.format("%s/%s", path.toAbsolutePath().toString(), strName);		
	}
	
	public void close() {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	Object m_lock = new Object();

	public boolean checkSymbol(String symbol) {
		
		if (symbolList.size() == 0)
			return true;
		
		synchronized(m_lock) {
			if(Collections.binarySearch(symbolList, symbol) >= 0)
				return true;
			else
				return false;
			//return symbolList.contains(symbol);
		}
	}
	
	public void addSymbols(List<String> list) {
		int iPos;
		for(String symbol : list)
		{			
			iPos = Collections.binarySearch(symbolList, symbol);
			if(iPos < 0)
				symbolList.add(~iPos,symbol);
		}
	}

	@Override
	public void onTimer(TimerThread objSender) {
		System.gc();		
	}

}
