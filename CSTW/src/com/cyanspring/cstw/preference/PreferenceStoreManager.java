package com.cyanspring.cstw.preference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.service.common.BasicEventModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/06/11
 *
 */
public final class PreferenceStoreManager extends BasicEventModel{

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
	
	public void setTableColumnWidth(String columnName, int width) {
		store.setValue(columnName + "/width", width);
	}

	public int getTableColumnWidth(String columnName) {
		return store.getInt(columnName + "/width");
	}

	public int getDefaultQuantity(String symbol) {
		return store.getInt(symbol + "DEFAULT_QUANTITY");
	}

	public void putDefaultQuantity(String symbol, int quantity) {
		store.setValue(symbol + "DEFAULT_QUANTITY", quantity);
	}

	public void setTableColumnVisible(String columnName, boolean isVisible) {
		store.setValue(columnName + "/visible", isVisible);
	}

	public boolean istTableColumnVisible(String columnName) {
		return store.getBoolean(columnName + "/visible");
	}

	public void setLastUserName(String userName) {
		store.setValue("LastUserName", userName);
	}

	public String getLastUserName() {
		return store.getString("LastUserName");
	}

	public boolean isSaved(String name) {
		return store.getBoolean(name);
	}

	public void clearSaved(String name) {
		store.setValue(name, false);
	}

	public RGB getRGB(String name) {
		int red = store.getInt(name + "/red");
		int green = store.getInt(name + "/green");
		int blue = store.getInt(name + "/blue");
		RGB rgb = new RGB(red, green, blue);
		return rgb;
	}

	public void setRGB(String name, RGB rgb) {
		store.setValue(name + "/red", rgb.red);
		store.setValue(name + "/green", rgb.green);
		store.setValue(name + "/blue", rgb.blue);
		store.setValue(name, true);
	}

	public void setFont(String name, FontData fontdata) {
		store.setValue(name + "/name", fontdata.getName());
		store.setValue(name + "/locale", fontdata.getLocale());
		store.setValue(name + "/style", fontdata.getStyle());
		store.setValue(name + "/height", fontdata.getHeight());
		store.setValue(name, true);
	}

	public Font getFont(String storeName) {
		FontData fontdata = new FontData();
		String name = store.getString(storeName + "/name");
		String locale = store.getString(storeName + "/locale");
		int style = store.getInt(storeName + "/style");
		int height = store.getInt(storeName + "/height");

		fontdata.setName(name);
		fontdata.setLocale(locale);
		fontdata.setHeight(height);
		fontdata.setStyle(style);

		Font font = new Font(null, fontdata);
		return font;
	}

	public void saveShortcutKeyMap(Map<String, int[]> shortcutKeyCodeMap) {
		for (String key : shortcutKeyCodeMap.keySet()) {
			int[] shortcutKeys = shortcutKeyCodeMap.get(key);
			setShortcutKeyByCode(key, shortcutKeys[0], shortcutKeys[1]);
		}
	}

	private void setShortcutKeyByCode(String name, int normalKey, int keyCode) {
		store.setValue("shortcutKey/normalKey/" + name, normalKey);
		store.setValue("shortcutKey/keyCode/" + name, keyCode);
	}

	public int[] getShortCutKeyByName(String name) {
		int[] commonkeys = new int[2];
		commonkeys[0] = store.getInt("shortcutKey/normalKey/" + name);
		commonkeys[1] = store.getInt("shortcutKey/keyCode/" + name);
		return commonkeys;
	}

	public int getDefaultQtyBase() {
		int base = store.getInt("DEFAULT_QUANTITY_BASE");
		if (0 == base) {
			base = 100;
		}
		return base;
	}

	public void setDefaultQtyBase(int base) {
		store.setValue("DEFAULT_QUANTITY_BASE", base);
		firePropertyChange("DEFAULT_QUANTITY_BASE", null, base);
	}
	
}

class MockStore extends PreferenceStore {

	@Override
	public void save() throws IOException {

	}

}
