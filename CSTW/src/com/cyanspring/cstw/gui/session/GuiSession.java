package com.cyanspring.cstw.gui.session;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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

}
