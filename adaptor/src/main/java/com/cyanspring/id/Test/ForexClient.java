package com.cyanspring.id.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.cyanspring.common.marketdata.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;

public class ForexClient implements IMarketDataListener,
		IMarketDataStateListener, IFrameClose, AutoCloseable {
	
	public static final String _ClientName = "QuotesFeed 1.20150423.1";
	private static final Logger log = LoggerFactory.getLogger(ForexClient.class);
	
	public static ForexClient instance = null;
	public static Hashtable<String, ForexClient> clientsMap = new Hashtable<String, ForexClient>();
	
	public static ArrayList<String> contribArray = new ArrayList<String>();

	Program parent = null;
	boolean isConnected = false;
	IDForexClientDialog dialog = null;

	/**
	 * 
	 * @param srcParent 
	 */
	public ForexClient(Program srcParent) {
		parent = srcParent;
		dialog = IDForexClientDialog.Instance(this, _ClientName);
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
				if (text.getText().isEmpty()){
					return;
				}
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
		final JTextField textCon = new JTextField();
		dialog.addTextField(textCon);

		final JCheckBox checkBoxCon = new JCheckBox("remove");
		checkBoxCon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		dialog.addCheckBox(checkBoxCon);
		JButton buttonT = new JButton("add Contribute");
		buttonT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (textCon.getText().isEmpty()){
					return;
				}
				boolean isRemove = checkBoxCon.isSelected();
				if (isRemove) {
					ForexClient.instance.onRemoveContributor(textCon.getText()
							.toUpperCase());
				} else {
					ForexClient.instance.onAddContributor(textCon.getText()
							.toUpperCase());
				}
			}
		});
		dialog.addButton(buttonT);
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
	public void onQuote(InnerQuote innerQuote) {
		//dialog.addLog(quote.toString());
		if (contribArray.isEmpty() == false){
			if (contribArray.contains(innerQuote.getContributor()) == false)
				return;
		}
		dialog.addLog("[%s][%s] bid:%.5f ask:%.5f %s", innerQuote.getSymbol(),
				DateUtil.formatDate(DateUtil.toGmt(innerQuote.getQuote().getTimeStamp()), "HH:mm:ss.SSS"),
				innerQuote.getQuote().getBid(), innerQuote.getQuote().getAsk(), innerQuote.getContributor());
		dialog.addFollow(innerQuote.getSymbol(), "[%s][%s] bid:%.5f ask:%.5f %s", innerQuote.getSymbol(),
				DateUtil.formatDate(DateUtil.toGmt(innerQuote.getQuote().getTimeStamp()), "HH:mm:ss.SSS"),
				innerQuote.getQuote().getBid(), innerQuote.getQuote().getAsk(), innerQuote.getContributor());

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
			dialog.removeFollow(Symbol);
		} catch (MarketDataException e) {
			LogUtil.logException(log, e);
		}
	}
	
	public void onAddContributor(String contribute){
		contribArray.add(contribute);
	}
	public void onRemoveContributor(String contribute){
		if (contribArray.contains(contribute)){
			contribArray.remove(contribute);
		}
	}

	@Override
	public void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource) {
		
	}
}