package com.cyanspring.id.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;

public class ForexClient implements IMarketDataListener,
		IMarketDataStateListener, IFrameClose, AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(ForexClient.class);
	
	public static ForexClient instance = null;
	public static Hashtable<String, ForexClient> clientsMap = new Hashtable<String, ForexClient>();

	Program parent = null;
	boolean isConnected = false;
	IDForexClientDialog dialog = null;

	/**
	 * 
	 * @param srcParent 
	 */
	public ForexClient(Program srcParent) {
		parent = srcParent;
		dialog = IDForexClientDialog.Instance(this, "Client");
		ForexClient.instance = this;
		final JTextField text = new JTextField();
		dialog.addTextField(text);

		final JCheckBox checkBox = new JCheckBox("remove");
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		dialog.addCheckBox(checkBox);
		JButton button = new JButton("add Symbol");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean isRemove = checkBox.isSelected();
				if (isRemove) {
					ForexClient.instance.onRemoveSymbol(text.getText()
							.toUpperCase());
				} else {
					ForexClient.instance.onAddSymbol(text.getText()
							.toUpperCase());
				}
			}
		});
		dialog.addButton(button);
		srcParent.addState(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.cyanspring.common.marketdata.IMarketDataStateListener#onState(boolean)
	 */
	@Override
	public void onState(boolean on) {
		isConnected = on;
		dialog.addLog("Status is %s", on ? "true" : "false");
		// update frame
		dialog.setStatus(String
				.format("Connection  is %s", on ? "on" : "off"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.cyanspring.common.marketdata.IMarketDataListener#onQuote(com.cyanspring.common.marketdata.Quote)
	 */
	@Override
	public void onQuote(Quote quote, int sourceId) {
		//dialog.addLog(quote.toString());
		dialog.addLog("[%s][%s] bid:%.5f ask:%.5f", quote.getSymbol(),
				DateUtil.formatDate(DateUtil.toGmt(quote.getTimeStamp()), "HH:mm:ss.SSS"),
				quote.getBid(), quote.getAsk());

	}

	/*
	 * (non-Javadoc)
	 * @see com.cyanspring.common.marketdata.IMarketDataListener#onTrade(com.cyanspring.common.marketdata.Trade)
	 */
	@Override
	public void onTrade(Trade trade) {
		try {
			throw new Exception("NotImplement");
		} catch (Exception e) {
			LogUtil.logException(log, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.cyanspring.id.Library.Frame.IFrameClose#onCloseAction()
	 */
	@Override
	public void onCloseAction() {

		parent.onClientclose(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		dialog = null;
		FinalizeHelper.suppressFinalize(this);
	}

	/**
	 * subscribe symbol
	 * @param Symbol
	 */
	public void onAddSymbol(String Symbol) {
		try {
			parent.onAddSymbol(Symbol, this);
		} catch (MarketDataException e) {
			LogUtil.logException(log, e);
		}
	}

	/**
	 * unsubscribe symbol
	 * @param Symbol
	 */
	public void onRemoveSymbol(String Symbol) {
		try {
			parent.onRemoveSymbol(Symbol, this);
		} catch (MarketDataException e) {
			LogUtil.logException(log, e);
		}
	}

	@Override
	public void onQuoteExt(DataObject quoteExt, int sourceId) {
		
	}
}