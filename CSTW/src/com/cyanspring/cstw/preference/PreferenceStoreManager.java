package com.cyanspring.cstw.preference;

import java.io.IOException;

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

	public String getDefaultQty() {
		return store.getString("ORDER_DEFAULT_QTY");
	}

	public void setDefayltQty(String qty) {
		store.setValue("ORDER_DEFAULT_QTY", qty);
	}

}

class MockStore extends PreferenceStore {

	@Override
	public void save() throws IOException {

	}

}
