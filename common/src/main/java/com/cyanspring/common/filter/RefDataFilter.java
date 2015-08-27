package com.cyanspring.common.filter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.staticdata.RefData;

public class RefDataFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefDataFilter.class);
	
	private List<IRefDataFilter> lstRefDataFilter;
	
	public RefDataFilter(IRefDataFilter... refDataFilters) {
		lstRefDataFilter = new ArrayList<IRefDataFilter>();
		
		for (IRefDataFilter filter : refDataFilters) {
			lstRefDataFilter.add(filter);
		}
	}

	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		for (IRefDataFilter filter : lstRefDataFilter) {
			lstRefData = filter.filter(lstRefData);
		}
		
		return lstRefData;
	}
	
}
