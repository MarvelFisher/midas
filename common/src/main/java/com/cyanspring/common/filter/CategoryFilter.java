package com.cyanspring.common.filter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataTplLoader;

public class CategoryFilter implements IRefDataFilter {
	
	RefDataTplLoader refDataTplLoader;
	
	public RefDataTplLoader getRefDataTplLoader() {
		return refDataTplLoader;
	}

	public void setRefDataTplLoader(RefDataTplLoader refDataTplLoader) {
		this.refDataTplLoader = refDataTplLoader;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryFilter.class);

	/**
	 * Compare Category of RefData between template_FC and lstRefData
	 * RefData with Category in FC template will be returned as a RefData list. 
	 * 
	 * @param lstRefData
	 *            The RefData list to be filtered
	 * @return The filtered RefData list
	 */
	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		ArrayList<RefData> fLstRefData = new ArrayList<RefData>();
		if (lstRefData != null) {
			// Compare RefData list from template with the input lstRefData
			// If Category of RefData in the input lstRefData doesn't exist in template, exclude it
			// After filtering, only Category in template will be kept in the returned lstRefData
			List<RefData> lstRefDataTpl = refDataTplLoader.getRefDataList();
			if (lstRefDataTpl != null && lstRefDataTpl.size() > 0) {
				ArrayList<String> lstCategory = new ArrayList<String>();
				for (RefData data : lstRefDataTpl) {
					lstCategory.add(data.getCategory());
				}
				
				for (RefData refData : lstRefData) {
					if (refData.getCategory() == null || refData.getCategory().isEmpty()) {
						LOGGER.error("Category cannot be null or empty.");
						throw new Exception("Category cannot be null or empty.");
					}
					
					if (lstCategory.contains(refData.getCategory())) {
						fLstRefData.add(refData);
					}
				}
			}
		} else {
			LOGGER.error("The given RefData list cannot be null");
			throw new Exception("The given RefData list cannot be null");
		}
		
		return fLstRefData;
	}

}
