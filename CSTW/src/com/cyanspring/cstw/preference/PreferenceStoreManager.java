package com.cyanspring.cstw.preference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import com.cyanspring.cstw.gui.Activator;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/06/11
 *
 */
public final class PreferenceStoreManager {

	private static PreferenceStoreManager instance;

	private IPreferenceStore store;

	private PreferenceStoreManager() {
		if (Activator.getDefault() == null) {
			store = new MockStore();
		} else {
			store = Activator.getDefault().getPreferenceStore();
		}

	}

	public static PreferenceStoreManager getInstance() {
		if (instance == null) {
			instance = new PreferenceStoreManager();
		}
		return instance;
	}

	public String getDefaultQty(String symbol) {
		return store.getString(symbol + "ORDER_DEFAULT_QTY");
	}

	public void setDefayltQty(String symbol, String qty) {
		store.setValue(symbol + "ORDER_DEFAULT_QTY", qty);
	}

	public void saveQuoteList(String quoteList) {
		store.setValue("QUOTE_LIST", quoteList);
	}

	public List<String> getQuoteList() {
		List<String> quoteList = new ArrayList<String>();
		if (store.getString("QUOTE_LIST") != null
				&& store.getString("QUOTE_LIST").split(";").length > 0) {
			String[] quotes = store.getString("QUOTE_LIST").split(";");
			for (String quote : quotes) {
				quoteList.add(quote);
			}
		}
		return quoteList;
	}
}

class MockStore extends PreferenceStore {

	@Override
	public void save() throws IOException {

	}

}
