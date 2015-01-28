package com.cyanspring.id;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.xml.xpath.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FileMgr;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Util.StringUtil;
import com.cyanspring.id.Library.Util.TimeSpan;
import com.cyanspring.id.Library.Xml.XmlUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class QuoteMgr implements AutoCloseable, TimerEventHandler {

	private static final Logger log = LoggerFactory
			.getLogger(IdMarketDataAdaptor.class);

	static QuoteMgr instance = new QuoteMgr();
	Object m_lock = new Object();

	public static QuoteMgr instance() {
		return instance;
	}

	public Hashtable<String, SymbolItem> symbolTable = new Hashtable<String, SymbolItem>();

	TimerThread timerThread = new TimerThread();

	public QuoteMgr() {

	}

	public void init() {
		try {
			readFile();
			LoadQuoteFile();
		} catch (Exception e) {
			LogUtil.logException(log, e);
		}
		initTimer();
	}

	public static boolean isSendRefresh = false;
	static boolean bSendPreclose = false;

	// static RefreshTask s_pRefresh = null;
	// static RefreshTask s_pRefreshPreclose = null;
	int m_nStatus = MarketStatus.NONE;
	Date timeLastWrite = DateUtil.now();

	// int status = MarketStatus.NONE;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler#onTimer
	 * (com.cyanspring.id.Library.Threading.TimerThread)
	 */
	@Override
	public void onTimer(TimerThread objSender) {

		Date now = DateUtil.now();
		int nStatus = IdMarketDataAdaptor.instance.getStatus();
		if (m_nStatus != MarketStatus.NONE && m_nStatus != nStatus) {
			m_nStatus = nStatus;
			LogUtil.logInfo(log, "Current Status : %s",
					MarketStatus.toString(nStatus));
			if (m_nStatus == MarketStatus.OPEN) {
				timeLastWrite = now;
			} else if (m_nStatus == MarketStatus.CLOSE) {
				writeFile(true, true);
			} else if (m_nStatus == MarketStatus.PREOPEN) {
				writeFile(false, true);
			}
		} else {
			m_nStatus = nStatus;
		}

		TimeSpan tSpan = TimeSpan.getTimeSpan(now, timeLastWrite);
		if (nStatus == MarketStatus.OPEN && tSpan.getTotalSeconds() >= 60) {
			timeLastWrite = now;
			writeFile(false, true);
		}

		int nSize = Parser.instance().getQueueSize();
		if (nSize > 0) {
			Util.addLog("Queue size : %d", nSize);
		}
	}

	/**
	 * 
	 */
	public void initTimer() {
		timerThread.TimerEvent = this;
		timerThread.setInterval(1000);
		timerThread.start();
	}

	/**
	 * 
	 */
	void uninit() {

		// writeFile(false);

		if (timerThread != null) {
			try {
				timerThread.stop(3000);
				timerThread.close();
			} catch (Exception e) {
				LogUtil.logException(log, e);
			}
			timerThread = null;
		}

		ArrayList<SymbolItem> list = new ArrayList<SymbolItem>(
				symbolTable.values());
		synchronized (m_lock) {

			for (SymbolItem item : list) {
				item.close();
			}
			list.clear();
			symbolTable.clear();
		}
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> SymbolList() {
		return new ArrayList<String>(symbolTable.keySet());
	}

	/**
	 * 
	 * @return
	 */
	public int getSymbolCount() {
		return symbolTable.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		writeFile(false, false);
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	public void addSymbol(String symbol) {
		if (checkSymbol(symbol) == false) {
			SymbolItem item = new SymbolItem(symbol);
			// item
			synchronized (m_lock) {
				symbolTable.put(symbol, item);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public SymbolItem getItem(String key) {
		synchronized (m_lock) {
			return symbolTable.get(key);
		}

	}

	/**
	 * 
	 * @param strSymbol
	 * @return
	 */
	public boolean checkSymbol(String strSymbol) {

		return symbolTable.containsKey(strSymbol);
	}

	/**
	 * 
	 * @param list
	 */
	public void initSymbols(List<String> list) {
		for (String symbol : list) {
			SymbolItem item = new SymbolItem(symbol);
			symbolTable.put(symbol, item);
		}
	}

	/**
	 * 
	 * @param nodeSymbols
	 * @return
	 */
	public boolean parseXml(Node nodeSymbols) {
		try {
			NodeList list = XmlUtil.selectNodes(nodeSymbols, "Symbol");
			for (int i = 0; i < list.getLength(); i++) {
				Node nodeSymbol = list.item(i);
				String s = XmlUtil.getAttribute((Element) nodeSymbol, "ID");
				String[] arrItem = StringUtil.split(s, ',');
				for (String symbol : arrItem) {
					SymbolItem item = new SymbolItem(symbol);
					symbolTable.put(symbol, item);
				}
			}

		} catch (XPathException e) {
			LogUtil.logException(log, e);
			return false;
		}
		return true;
	}

	public void sunrise() {
		Util.addLog("Sunrise");
		LogUtil.logInfo(log, "Sunrise");

		List<SymbolItem> list = new ArrayList<SymbolItem>(symbolTable.values());

		for (SymbolItem item : list) {
			item.sunrise();
		}
		list.clear();
	}

	private XStream xstream = new XStream(new DomDriver());

	private void LoadQuoteFile() {
		String lastPath = IdMarketDataAdaptor.instance.getDataPath("last.xml");
		HashMap<String, Quote> quotes = loadQuotes(lastPath);
		for (Quote quote : quotes.values()) {
			String symbol = quote.getSymbol();
			SymbolItem item = symbolTable.get(symbol);
			if (item == null) {
				item = new SymbolItem(symbol);
				synchronized (symbolTable) {
					symbolTable.put(symbol, item);
				}
			}
			item.updateQuote(quote);
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Quote> loadQuotes(String fileName) {
		File file = new File(fileName);
		HashMap<String, Quote> quotes = new HashMap<>();
		if (file.exists() && quotes.size() <= 0) {
			try {
				ClassLoader save = xstream.getClassLoader();
				ClassLoader cl = HashMap.class.getClassLoader();
				if (cl != null)
					xstream.setClassLoader(cl);
				quotes = (HashMap<String, Quote>) xstream.fromXML(file);
				if (!(quotes instanceof HashMap))
					throw new Exception("Can't xstream load last quote: "
							+ fileName);
				xstream.setClassLoader(save);
			} catch (Exception e) {
				LogUtil.logException(log, e);
				// log.error(e.getMessage(), e);
			}
			for (Quote quote : quotes.values()) {
				quote.setStale(true);
			}
			LogUtil.logInfo(log, "Quotes loaded: %s", fileName);
		}
		return quotes;
	}

	boolean readFile() throws Exception {
		String fileName = IdMarketDataAdaptor.instance.getDataPath("id-forex");

		File file = new File(fileName);
		if (!file.exists()) {
			return false;
		}
		boolean bDone = false;
		try (FileInputStream fs = new FileInputStream(file)) {
			try (Scanner scanner = new Scanner(fs)) {

				while (scanner.hasNextLine()) {

					String sValue = scanner.nextLine();
					sValue.trim();

					int nPos = sValue.indexOf(0x06);
					if (nPos <= 0)
						continue;

					String strID = sValue.substring(0, nPos);
					strID.trim();

					if (strID.isEmpty())
						continue;

					SymbolItem item = symbolTable.get(strID);
					if (null == item) {
						item = new SymbolItem(strID);
						symbolTable.put(strID, item);
					}

					item.loadFromFile(sValue);
				}
			}
		} finally {
		}

		return bDone;
	}

	public void writeFile(boolean closeTime, boolean isAsync) {
		String strPath = IdMarketDataAdaptor.instance.getDataPath("id-forex");
		LogUtil.logInfo(log, "Write File %s", strPath);

		try {
			ArrayList<String> listData = new ArrayList<String>();
			synchronized (m_lock) {

				Set<String> set = symbolTable.keySet();
				Iterator<String> itr = set.iterator();
				while (itr.hasNext()) {
					String strID = itr.next();

					if (strID.isEmpty())
						continue;

					SymbolItem item = symbolTable.get(strID);
					if (closeTime) {
						item.setClose();
					}
					String strData = item.toString();
					listData.add(strData);
				}
			}

			if (isAsync) { // async method
				FileMgr.instance().writeDataToFile(strPath, listData, true);
			} else { // sync method
				FileMgr.writeFile(strPath, listData, true);
			}
			// Thread.sleep(5000);

		} catch (Exception ex) {
			LogUtil.logException(log, ex);
			LogUtil.logError(log, "Fail to write file %s", strPath);
		}
	}
}
