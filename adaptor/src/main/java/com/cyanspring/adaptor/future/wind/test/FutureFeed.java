package com.cyanspring.adaptor.future.wind.test;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.adaptor.future.wind.WindFutureDataAdaptor;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Frame.InfoString;
import com.cyanspring.id.Library.Util.Logger;


public class FutureFeed implements IFrameClose {

	List<FutureClient> clients = new ArrayList<FutureClient>();
	FutureClient client = null;
	Mainframe mainframe = null;
	public boolean isSelectAll = false;
	public String watchSymbol = "IF1501";
	public boolean isWatchSymbol(String symbol) {
			return symbol.compareToIgnoreCase(watchSymbol) == 0;
	}
	
	public static FutureFeed instance = new FutureFeed();
	public WindFutureDataAdaptor adaptor = null;

	public void showQuote(String quote) {
		if (mainframe != null) {
			mainframe.setQuoteText(quote);
		}
	}

	public static void info(String f,  Object... args) {
		if (FutureFeed.instance.mainframe != null) {
			FutureFeed.instance.mainframe.addLog(InfoString.Info, f, args);
		}
	}
	
	public static void error(String f, Object... args) {
		if (FutureFeed.instance.mainframe != null) {
			FutureFeed.instance.mainframe.addLog(InfoString.Error, f, args);
		}
	}
	
	public static void debug(String f, Object... args) {
		if (FutureFeed.instance.mainframe != null) {
			FutureFeed.instance.mainframe.addLog(InfoString.Info, f, args);
		}
	}
	
	public FutureFeed() {
	}

	public void addComponents() {
		
		if (mainframe == null)  {
			return;
		}
		JButton buttonRecon = new JButton("Reconnect");
		buttonRecon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				adaptor.reconnect();
			}
		});
		mainframe.addButton(buttonRecon);
		
		JButton buttonRefreshSymbol = new JButton("refreh Symbol");
		buttonRefreshSymbol.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				adaptor.refreshSymbolInfo("SHF");
			}
		});
		
		mainframe.addButton(buttonRefreshSymbol);
		mainframe.addLabel(new JLabel("Watch"));
		final JTextField textField = new JTextField();
		textField.setText(this.watchSymbol);
		textField.setPreferredSize(new Dimension(100, 20));
		textField.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {				
				watchSymbol = textField.getText();
			}
		});
		mainframe.addTextField(textField);
		
		final JCheckBox check = new JCheckBox();
		check.setSelected(isSelectAll);
		check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isSelectAll = check.isSelected();
			}
		});

		mainframe.addLabel(new JLabel("All"));
		mainframe.addCheckBox(check);
		
		final JButton newClient = new JButton("New Client");
		newClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (client == null) {
					client = new FutureClient(FutureFeed.instance);
					FutureFeed.instance.addClient(client);
					newClient.setEnabled(false);
				}
			}
		});
		mainframe.addButton(newClient);
	}
	
	@Override
	public void onCloseAction() {

	}
	
	public void addState(FutureClient client) {
		adaptor.subscirbeSymbolData(client);
		adaptor.subscribeMarketDataState(client);
	}

	public void removeState(FutureClient client) {
		adaptor.unsubscribeSymbolData(client);
		adaptor.unsubscribeMarketDataState(client);
	}

	public void onAddSymbol(String Symbol, FutureClient client)
			throws MarketDataException {
		adaptor.subscribeMarketData(Symbol, client);
	}

	public void onRemoveSymbol(String Symbol, FutureClient client)
			throws MarketDataException {
		adaptor.unsubscribeMarketData(Symbol, client);
	}
	
	public void addClient(FutureClient client) {
		clients.add(client);
	}

	public void onClientClose(FutureClient client) throws Exception {
		if (clients.contains(client) == true) {
			clients.remove(client);
			client.close();
		}
	}	
	public void onClientclose( FutureClient client) {
		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//LogUtil.log4J = false;
		final FutureFeed future = FutureFeed.instance;
		//future.mainframe = Mainframe.instance(future, "Future Feed");
		//future.addComponents();
		
		DOMConfigurator.configure("conf/windlog4j.xml");

		
		String configFile = "conf/windtest.xml";
		try {
			ApplicationContext context = new FileSystemXmlApplicationContext(
					configFile);

			// start server
			WindFutureDataAdaptor bean = (WindFutureDataAdaptor) context
					.getBean("windFutureDataAdaptor");

			future.adaptor = bean;

			future.adaptor.init();

			if (bean.isShowGui()) {
				future.mainframe = Mainframe.instance(future, "Future Feed");
				future.addComponents();
			}
				
			info("Program Start");
		} catch (Exception e) {
			Logger.logException(e);
		}
	}	
}
