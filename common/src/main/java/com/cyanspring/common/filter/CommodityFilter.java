package com.cyanspring.common.filter;

import java.util.Iterator;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class CommodityFilter implements IRefDataFilter {

	List<String> lstExcludedSymbols;
	List<String> lstExcludedCategories;

	public List<String> getLstExcludedSymbols() {
		return lstExcludedSymbols;
	}

	public void setLstExcludedSymbols(List<String> lstExcludedSymbols) {
		this.lstExcludedSymbols = lstExcludedSymbols;
	}

	public List<String> getLstExcludedCategories() {
		return lstExcludedCategories;
	}

	public void setLstExcludedCategories(List<String> lstExcludedCategories) {
		this.lstExcludedCategories = lstExcludedCategories;
	}

	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		if (lstRefData != null && lstRefData.size() > 0) {
			Iterator<RefData> iterator = lstRefData.iterator();
			if (lstExcludedSymbols != null && lstExcludedSymbols.size() > 0) {
				while (iterator.hasNext()) {
					RefData refData = iterator.next();
					if (lstExcludedSymbols.contains(refData.getSymbol())) {
						iterator.remove();
					}
				}
			}

			if (lstExcludedCategories != null && lstExcludedCategories.size() > 0) {
				while (iterator.hasNext()) {
					RefData refData = iterator.next();
					if (lstExcludedCategories.contains(refData.getCategory())) {
						iterator.remove();
					}
				}
			}
		}

		return lstRefData;
	}

}
