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
		if (lstRefData == null || lstRefData.size() == 0 ||
				 ((lstExcludedSymbols == null || lstExcludedSymbols.size() == 0) &&
						 lstExcludedCategories == null || lstExcludedCategories.size() == 0)) {
			return lstRefData;
		}

		if (lstExcludedSymbols == null) {
			lstExcludedSymbols = new ArrayList<>();
		}
		if (lstExcludedCategories == null) {
			lstExcludedCategories = new ArrayList<>();
		}

		ArrayList<RefData> fLstRefData = new ArrayList<>();
		for (RefData refData : lstRefData) {
			if (!lstExcludedSymbols.contains(refData.getSymbol()) &&
					!lstExcludedCategories.contains(refData.getCategory())) {
				fLstRefData.add(refData);
			}
		}

		return fLstRefData;
	}

}
