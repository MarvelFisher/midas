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
import com.cyanspring.id.Library.Threading.TimerThread;
import com.cyanspring.id.Library.Threading.TimerThread.TimerEventHandler;
import com.cyanspring.id.Library.Util.LogUtil;

public class Program implements IFrameClose, TimerEventHandler {

	private static final Logger log = LoggerFactory.getLogger(Program.class);

	static Program instance = new Program();

	TimerThread timer = new TimerThread();
	public static Program instance() {
		return instance;
	}

	List<ForexClient> clients = new ArrayList<ForexClient>();

	public static boolean isConnected = false;
	public static final String version = "1.00R02";
	public static final String timeStamp = "2015-04-23";
	
	private JButton btnNewClient;

	public Program() {
		timer.TimerEvent = this;
		timer.setInterval(5000);
		timer.start();
	}
	
	public static void setStatus(boolean connected) {
		if (isConnected != connected) {
			isConnected = connected;
			adapter.updateState();
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

		DOMConfigurator.configure("conf/idlog4j.xml");

		String configFile = "conf/idtest.xml";
		try {
			ApplicationContext context = new FileSystemXmlApplicationContext(
					configFile);

			// start server
			IdMarketDataAdaptor bean = (IdMarketDataAdaptor) context
					.getBean("idMarketDataAdaptor");

			adapter = bean;

			adapter.init();

			LogUtil.logInfo(log, "Program Start");
			mainFrame.addLog("Program Start");
		} catch (Exception e) {
			mainFrame.addLog(InfoString.Error, "init fail reason : [%s]", e.getMessage());
		}

	}

	ForexClient _client= null;
	
	public void addRefreshButton() {

		JButton reConnect = new JButton("reconnect");
		reConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				adapter.closeClient();
				LogUtil.logInfo(log, "reconnect ....");				
				mainFrame.addLog(InfoString.Info, "reconnect ....");
			}
		});
		mainFrame.addButton(reConnect);

		JButton refresh = new JButton("Add all");
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final String[] arrSymbol = new String[] 
				//{
				//		"USDCAD","USDCHF","USDCNH","USDCZK","USDDKK","USDHKD","USDHUF","USDILS","USDJPY","USDMXN","USDNOK","USDPLN","USDRUB","USDSEK","USDSGD","NZDUSD","EURUSD","GBPUSD","AUDUSD",
				//		"EURAUD","EURCAD","EURCHF","EURCNH","EURCZK","EURDKK","EURGBP","EURHKD","EURILS","EURJPY","EURMXN","EURNOK","EURNZD","EURRUB","EURSEK","EURSGD",
				//		"GBPAUD","GBPCAD","GBPCHF","GBPCNH","GBPDKK","GBPHKD","GBPJPY","GBPNOK","GBPNZD","GBPSEK",
				//		"AUDCHF","AUDHKD","AUDJPY","AUDCAD","AUDCNH","AUDNZD","AUDSGD",
				//		"CHFJPY","CHFCNH","CHFDKK","CHFNOK","CHFSEK",
				//		"CNHJPY","DKKJPY","DKKNOK","DKKSEK","HKDJPY","MXNJPY","NOKJPY","NOKSEK","SEKJPY","SGDCNH","SGDJPY","NZDCHF","NZDJPY"};
				{
					"AUDCAD","AUDCHF","AUDCNH","AUDCNY","AUDHKD","AUDJPY","AUDNZD","AUDSGD","AUDUSD","CADCHF",
					"CADCNH","CADCNY","CADHKD","CADJPY","CHFCNH","CHFCNY","CHFDKK","CHFJPY","CHFNOK","CHFSEK",
					"CNHHKD","CNHIDR","CNHINR","CNHJPY","CNHKRW","CNHPHP","CNHTHB","CNHTWD","CNYBRL","CNYIDR",
					"CNYINR","CNYJPY","CNYKRW","CNYMYR","CNYPHP","CNYPLN","CNYRUB","CNYTHB","CNYTWD","CNYXCU",
					"CNYZAR","DKKJPY","DKKNOK","DKKSEK","EURAUD","EURCAD","EURCHF","EURCNH","EURCNY","EURCZK",
					"EURDKK","EURGBP","EURHKD","EURHUF","EURILS","EURJPY","EURMXN","EURNOK","EURNZD","EURPLN",
					"EURRUB","EURSEK","EURSGD","EURTRY","EURUSD","GBPAUD","GBPCAD","GBPCHF","GBPCNH","GBPCNY",
					"GBPDKK","GBPHKD","GBPJPY","GBPNOK","GBPNZD","GBPSEK","GBPUSD","HKDCNY","HKDJPY","KRWAUD",
					"KRWCAD","KRWCHF","KRWEUR","KRWGBP","KRWHKD","KRWJPY","KRWUSD","MXNJPY","NOKJPY","NOKSEK",
					"NZDCAD","NZDCHF","NZDCNH","NZDCNY","NZDJPY","NZDUSD","SEKCNY","SEKJPY","SGDCNH","SGDCNY",
					"SGDJPY","USDAED","USDAFN","USDALL","USDAMD","USDANG","USDAOA","USDARS","USDAWG","USDAZN",
					"USDBAM","USDBBD","USDBDT","USDBGN","USDBHD","USDBIF","USDBMD","USDBND","USDBOB","USDBRL",
					"USDBSD","USDBTN","USDBWP","USDBYR","USDBZD","USDCAD","USDCDF","USDCHF","USDCLF","USDCLP",
					"USDCNH","USDCNY","USDCOP","USDCRC","USDCUC","USDCUP","USDCVE","USDCZK","USDDJF","USDDKK",
					"USDDOP","USDDZD","USDEGP","USDERN","USDETB","USDFJD","USDFKP","USDGEL","USDGHS","USDGIP",
					"USDGMD","USDGNF","USDGTQ","USDGYD","USDHKD","USDHNL","USDHRK","USDHTG","USDHUF","USDIDR",
					"USDILA","USDILS","USDINR","USDIQD","USDIRR","USDISK","USDJMD","USDJOD","USDJPY","USDKES",
					"USDKGS","USDKHR","USDKMF","USDKPW","USDKRW","USDKWD","USDKYD","USDKZT","USDLAK","USDLBP",
					"USDLKR","USDLRD","USDLSL","USDLTL","USDLVL","USDLYD","USDMAD","USDMDL","USDMGA","USDMKD",
					"USDMMK","USDMNT","USDMOP","USDMRO","USDMUR","USDMVR","USDMWK","USDMXN","USDMYR","USDMZN",
					"USDNAD","USDNGN","USDNIO","USDNOK","USDNPR","USDOMR","USDPAB","USDPEN","USDPGK","USDPHP",
					"USDPKR","USDPLN","USDPYG","USDQAR","USDRON","USDRSD","USDRUB","USDRWF","USDSAR","USDSBD",
					"USDSCR","USDSDG","USDSEK","USDSGD","USDSHP","USDSLL","USDSOS","USDSRD","USDSTD","USDSVC",
					"USDSYP","USDSZL","USDTHB","USDTJS","USDTMT","USDTND","USDTOP","USDTRY","USDTTD","USDTWD",
					"USDTZS","USDUAH","USDUGX","USDUYU","USDUZS","USDVEF","USDVND","USDVUV","USDWST","USDXAF",
					"USDXCD","USDXCU","USDXDR","USDXOF","USDXPF","USDYER","USDZAC","USDZAR","USDZMW","USDZWL",
					"ZARJPY","XAUUSD","XAGUSD"};
				if (_client != null) {
					for (String symbol : arrSymbol) {
						try {
							adapter.subscribeMarketData(symbol, _client);
						} catch (MarketDataException e) {
							e.printStackTrace();
						};
					}	
				}
				//LogUtil.logInfo(log, "refresh ....");
				//mainFrame.addLog(InfoString.Info, "refresh ....");
				//QuoteMgr.instance().refresh();
			}
		});
		mainFrame.addButton(refresh);

		mainFrame.addSpace();
		JButton writrFile = new JButton("write");
		writrFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				LogUtil.logInfo(log, "writrFile ....");
				mainFrame.addLog(InfoString.Info, "writrFile ....");
				QuoteMgr.instance().writeFile(false, true);
			}
		});
		mainFrame.addButton(writrFile);		
		
		final JButton newClient = new JButton("New Client");
		btnNewClient = newClient;
		newClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				// QuoteMgr.Instance().refresh();
				if (_client == null) {
					LogUtil.logInfo(log, "add new client ....");
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

		if (adapter != null) {
			adapter.uninit();
		}
		try {
			timer.close();
		} catch (Exception e) {
		}
		System.exit(0);
	}

	public void reconClient() {
		if (adapter != null) {
//			adapter.reconClient();
		}
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
		final String[] arrSymbol = new String[] 
			//{
			//		"USDCAD","USDCHF","USDCNH","USDCZK","USDDKK","USDHKD","USDHUF","USDILS","USDJPY","USDMXN","USDNOK","USDPLN","USDRUB","USDSEK","USDSGD","NZDUSD","EURUSD","GBPUSD","AUDUSD",
			//		"EURAUD","EURCAD","EURCHF","EURCNH","EURCZK","EURDKK","EURGBP","EURHKD","EURILS","EURJPY","EURMXN","EURNOK","EURNZD","EURRUB","EURSEK","EURSGD",
			//		"GBPAUD","GBPCAD","GBPCHF","GBPCNH","GBPDKK","GBPHKD","GBPJPY","GBPNOK","GBPNZD","GBPSEK",
			//		"AUDCHF","AUDHKD","AUDJPY","AUDCAD","AUDCNH","AUDNZD","AUDSGD",
			//		"CHFJPY","CHFCNH","CHFDKK","CHFNOK","CHFSEK",
			//		"CNHJPY","DKKJPY","DKKNOK","DKKSEK","HKDJPY","MXNJPY","NOKJPY","NOKSEK","SEKJPY","SGDCNH","SGDJPY","NZDCHF","NZDJPY"};
			{
				"AUDCAD","AUDCHF","AUDCNH","AUDCNY","AUDHKD","AUDJPY","AUDNZD","AUDSGD","AUDUSD","CADCHF",
				"CADCNH","CADCNY","CADHKD","CADJPY","CHFCNH","CHFCNY","CHFDKK","CHFJPY","CHFNOK","CHFSEK",
				"CNHHKD","CNHIDR","CNHINR","CNHJPY","CNHKRW","CNHPHP","CNHTHB","CNHTWD","CNYBRL","CNYIDR",
				"CNYINR","CNYJPY","CNYKRW","CNYMYR","CNYPHP","CNYPLN","CNYRUB","CNYTHB","CNYTWD","CNYXCU",
				"CNYZAR","DKKJPY","DKKNOK","DKKSEK","EURAUD","EURCAD","EURCHF","EURCNH","EURCNY","EURCZK",
				"EURDKK","EURGBP","EURHKD","EURHUF","EURILS","EURJPY","EURMXN","EURNOK","EURNZD","EURPLN",
				"EURRUB","EURSEK","EURSGD","EURTRY","EURUSD","GBPAUD","GBPCAD","GBPCHF","GBPCNH","GBPCNY",
				"GBPDKK","GBPHKD","GBPJPY","GBPNOK","GBPNZD","GBPSEK","GBPUSD","HKDCNY","HKDJPY","KRWAUD",
				"KRWCAD","KRWCHF","KRWEUR","KRWGBP","KRWHKD","KRWJPY","KRWUSD","MXNJPY","NOKJPY","NOKSEK",
				"NZDCAD","NZDCHF","NZDCNH","NZDCNY","NZDJPY","NZDUSD","SEKCNY","SEKJPY","SGDCNH","SGDCNY",
				"SGDJPY","USDAED","USDAFN","USDALL","USDAMD","USDANG","USDAOA","USDARS","USDAWG","USDAZN",
				"USDBAM","USDBBD","USDBDT","USDBGN","USDBHD","USDBIF","USDBMD","USDBND","USDBOB","USDBRL",
				"USDBSD","USDBTN","USDBWP","USDBYR","USDBZD","USDCAD","USDCDF","USDCHF","USDCLF","USDCLP",
				"USDCNH","USDCNY","USDCOP","USDCRC","USDCUC","USDCUP","USDCVE","USDCZK","USDDJF","USDDKK",
				"USDDOP","USDDZD","USDEGP","USDERN","USDETB","USDFJD","USDFKP","USDGEL","USDGHS","USDGIP",
				"USDGMD","USDGNF","USDGTQ","USDGYD","USDHKD","USDHNL","USDHRK","USDHTG","USDHUF","USDIDR",
				"USDILA","USDILS","USDINR","USDIQD","USDIRR","USDISK","USDJMD","USDJOD","USDJPY","USDKES",
				"USDKGS","USDKHR","USDKMF","USDKPW","USDKRW","USDKWD","USDKYD","USDKZT","USDLAK","USDLBP",
				"USDLKR","USDLRD","USDLSL","USDLTL","USDLVL","USDLYD","USDMAD","USDMDL","USDMGA","USDMKD",
				"USDMMK","USDMNT","USDMOP","USDMRO","USDMUR","USDMVR","USDMWK","USDMXN","USDMYR","USDMZN",
				"USDNAD","USDNGN","USDNIO","USDNOK","USDNPR","USDOMR","USDPAB","USDPEN","USDPGK","USDPHP",
				"USDPKR","USDPLN","USDPYG","USDQAR","USDRON","USDRSD","USDRUB","USDRWF","USDSAR","USDSBD",
				"USDSCR","USDSDG","USDSEK","USDSGD","USDSHP","USDSLL","USDSOS","USDSRD","USDSTD","USDSVC",
				"USDSYP","USDSZL","USDTHB","USDTJS","USDTMT","USDTND","USDTOP","USDTRY","USDTTD","USDTWD",
				"USDTZS","USDUAH","USDUGX","USDUYU","USDUZS","USDVEF","USDVND","USDVUV","USDWST","USDXAF",
				"USDXCD","USDXCU","USDXDR","USDXOF","USDXPF","USDYER","USDZAC","USDZAR","USDZMW","USDZWL",
				"ZARJPY","XAUUSD","XAGUSD"};
			if (_client != null) {
				for (String symbol : arrSymbol) {
					adapter.unsubscribeMarketData(symbol, _client);
				}	
			}
		_client = null;
		btnNewClient.setEnabled(true);
	}

	@Override
	public void onTimer(TimerThread objSender) {
		System.gc();		
	}
}
