package com.cyanspring.id.gateway; 

import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;
import com.cyanspring.id.Library.Xml.XmlUtil;

public class QuoteMgr implements AutoCloseable {
	private static final Logger log = LoggerFactory
			.getLogger(IdGateway.class);
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

	public QuoteMgr() {
	}

	void fini() {
		synchronized (m_lock) {
			symbolList.clear();
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
	
	public boolean parseXml(Node nodeSymbols) {
		try {
			NodeList list = XmlUtil.selectNodes(nodeSymbols, "Symbol");
			for (int i = 0; i < list.getLength(); i++) {
				Node nodeSymbol = list.item(i);
				String s = XmlUtil.getAttribute((Element) nodeSymbol, "ID");
				String[] arrItem = StringUtil.split(s, ',');
				for (String symbol : arrItem) {
					symbolList.add(symbol);
				}
			}

		} catch (XPathException e) {
			LogUtil.logError(log, e.getMessage());
			LogUtil.logException(log, e);
			return false;
		}

		return true;

	}
}
