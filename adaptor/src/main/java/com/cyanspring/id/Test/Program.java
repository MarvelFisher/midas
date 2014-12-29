package com.cyanspring.id.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import io.netty.channel.ChannelFuture;

import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.id.IdMarketDataAdaptor;
import com.cyanspring.id.QuoteMgr;
import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Frame.InfoString;

public class Program implements IFrameClose {

	private static final Logger log = LoggerFactory.getLogger(Program.class);

	static Program instance = new Program();

	public static Program instance() {
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

	public static IDForexFeedDialog mainFrame = null;

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

		mainFrame = IDForexFeedDialog.Instance(Program.instance(),
				String.format("%s:%s", "IDForexFeed", Program.version), true);
		Program.instance().addRefreshButton();

		DOMConfigurator.configure("conf/log4j.xml");

		String configFile = "conf/idtest.xml";
		ApplicationContext context = new FileSystemXmlApplicationContext(
				configFile);

		// start server
		IdMarketDataAdaptor bean = (IdMarketDataAdaptor) context
				.getBean("IdMarketDataAdaptor");

		adapter = bean;

		adapter.init();

		log.info("Program Start");
		mainFrame.addLog("Program Start");
	}

	ForexClient _client= null;
	
	public void addRefreshButton() {

		JButton reConnect = new JButton("reconnect");
		reConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				adapter.closeClient();
				log.info("reconnect ....");				
				mainFrame.addLog(InfoString.Info, "reconnect ....");
			}
		});
		mainFrame.addButton(reConnect);

		JButton refresh = new JButton("Add all");
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final String[] arrSymbol = new String[] {
						"USDCAD","USDCHF","USDCNH","USDCZK","USDDKK","USDHKD","USDHUF","USDILS","USDJPY","USDMXN","USDNOK","USDPLN","USDRUB","USDSEK","USDSGD","NZDUSD","EURUSD","GBPUSD","AUDUSD",
						"EURAUD","EURCAD","EURCHF","EURCNH","EURCZK","EURDKK","EURGBP","EURHKD","EURILS","EURJPY","EURMXN","EURNOK","EURNZD","EURRUB","EURSEK","EURSGD",
						"GBPAUD","GBPCAD","GBPCHF","GBPCNH","GBPDKK","GBPHKD","GBPJPY","GBPNOK","GBPNZD","GBPSEK",
						"AUDCHF","AUDHKD","AUDJPY","AUDCAD","AUDCNH","AUDNZD","AUDSGD",
						"CHFJPY","CHFCNH","CHFDKK","CHFNOK","CHFSEK",
						"CNHJPY","DKKJPY","DKKNOK","DKKSEK","HKDJPY","MXNJPY","NOKJPY","NOKSEK","SEKJPY","SGDCNH","SGDJPY","NZDCHF","NZDJPY"};
				if (_client != null) {
					for (String symbol : arrSymbol) {
						try {
							adapter.subscribeMarketData(symbol, _client);
						} catch (MarketDataException e) {
							e.printStackTrace();
						};
					}	
				}
				//log.info("refresh ....");
				//mainFrame.addLog(InfoString.Info, "refresh ....");
				//QuoteMgr.instance().refresh();
			}
		});
		mainFrame.addButton(refresh);

		mainFrame.addSpace();
		JButton writrFile = new JButton("write");
		writrFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("writrFile ....");
				mainFrame.addLog(InfoString.Info, "writrFile ....");
				QuoteMgr.instance().writeFile(false);
			}
		});
		mainFrame.addButton(writrFile);		
		
		final JButton newClient = new JButton("New Client");
		newClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				// QuoteMgr.Instance().refresh();
				if (_client == null) {
					log.info("add new client ....");
					mainFrame.addLog(InfoString.Info, "add new client .....");
					_client = new ForexClient(Program.instance());
					Program.instance().addClient(_client);
					newClient.setEnabled(false);
				}
			}
		});
		mainFrame.addButton(newClient);
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
	
	public void onClientclose( ForexClient client) {
		
	}
}
