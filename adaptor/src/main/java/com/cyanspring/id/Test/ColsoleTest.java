package com.cyanspring.id.Test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import io.netty.channel.ChannelFuture;

import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.id.IdMarketDataAdaptor;
import com.cyanspring.id.Library.Util.LogUtil;

public class ColsoleTest {

	private static final Logger log = LoggerFactory.getLogger(ColsoleTest.class);

	static ColsoleTest instance = new ColsoleTest();

	public static ColsoleTest instance() {
		return instance;
	}

	List<ForexClient> clients = new ArrayList<ForexClient>();

	public static boolean isConnected = false;
	public static final String version = "1.00R01";
	public static final String timeStamp = "2014-12-15";

	public static void setStatus(boolean connected) {
		if (isConnected != connected) {
			isConnected = connected;
			adapter.updateState(connected);
		}
	}

	//public static IDForexFeedDialog mainFrame = null;

	boolean isClose = false;
	// static ChannelFuture fClient = null;
	static ChannelFuture fServer = null;

	public static IdMarketDataAdaptor adapter = null; // new IDForexAdapter();

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		DOMConfigurator.configure("conf/idlog4j.xml");

		String configFile = "conf/idtest.xml";
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		IdMarketDataAdaptor bean = (IdMarketDataAdaptor) context
				.getBean("IdMarketDataAdaptor");

		adapter = bean;

		adapter.init();

		LogUtil.logInfo(log, "Program Start");
	}


	public void onCloseAction() {

		adapter.uninit();
		System.exit(0);
	}

	public void reconClient() {
		adapter.reconClient();
	}

	public void addClient(ForexClient client) {
		clients.add(client);
	}

	public void onClientClose(ForexClient client) throws Exception {
		if (clients.contains(client) == true) {
			clients.remove(client);
			client.close();
		}
	}

	public void addState(ForexClient client) {
		adapter.subscribeMarketDataState(client);
	}

	public void removeState(ForexClient client) {
		adapter.unsubscribeMarketDataState(client);
	}

	public void onAddSymbol(String Symbol, ForexClient client)
			throws MarketDataException {
		adapter.subscribeMarketData(Symbol, client);
	}

	public void onRemoveSymbol(String Symbol, ForexClient client)
			throws MarketDataException {
		adapter.unsubscribeMarketData(Symbol, client);
	}
}
