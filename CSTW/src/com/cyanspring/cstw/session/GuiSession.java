package com.cyanspring.cstw.session;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.cyanspring.common.account.AccountSetting;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.09.21
 *
 */
public final class GuiSession {

	private static GuiSession instance;

	private PropertyChangeSupport pcs;

	private String symbol;
	private AccountSetting accountSetting = null;
	
	public enum Property{
		SYMBOL,ACCOUNT_SETTING
	}

	private GuiSession() {
		pcs = new PropertyChangeSupport(this);
		symbol = "";
	}

	public static GuiSession getInstance() {
		if (instance == null) {
			instance = new GuiSession();
		}
		return instance;
	}

	public String getSymbol() {
		return symbol;
	}
	

	public void setSymbol(String symbol) {
		String oldSymbol = this.symbol;
		this.symbol = symbol;
		pcs.firePropertyChange("symbol", oldSymbol, symbol);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}

	public void setAccountSetting(AccountSetting accountSetting) {
		AccountSetting oldSetting = this.accountSetting;
		this.accountSetting = accountSetting;
		pcs.firePropertyChange(Property.ACCOUNT_SETTING.toString(), oldSetting, accountSetting);
	}
}
