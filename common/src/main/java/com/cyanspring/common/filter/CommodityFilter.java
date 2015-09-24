package com.cyanspring.common.filter;

import java.util.ArrayList;
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
			ArrayList<RefData> fLstRefData = new ArrayList<RefData>();

			if (lstExcludedSymbols != null && lstExcludedSymbols.size() > 0) {
				for (RefData refData : lstRefData) {
					if (!lstExcludedSymbols.contains(refData.getSymbol())) {
						fLstRefData.add(refData);
					}
				}
			}

			if (lstExcludedCategories != null && lstExcludedCategories.size() > 0) {
				if (fLstRefData.size() > 0) {
					for (RefData refData : fLstRefData) {
						if (lstExcludedCategories.contains(refData.getCategory())) {
							fLstRefData.remove(refData);
						}
					}
				} else {
					for (RefData refData : lstRefData) {
						if (!lstExcludedCategories.contains(refData.getCategory())) {
							fLstRefData.add(refData);
						}
					}
				}
			}

			return fLstRefData;
		}

		return lstRefData;
	}

}
