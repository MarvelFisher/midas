package com.cyanspring.common.filter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;

public class RefDataFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefDataFilter.class);
	
	private IRefDataFilter[] refDataFilters;
	
	public RefDataFilter(IRefDataFilter... refDataFilters) {
		this.refDataFilters = refDataFilters;
	}

	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		for (IRefDataFilter filter : refDataFilters) {
			lstRefData = filter.filter(lstRefData);
		}
		
		return lstRefData;
	}
	
}
