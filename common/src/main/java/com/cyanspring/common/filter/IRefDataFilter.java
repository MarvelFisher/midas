package com.cyanspring.common.filter;

import com.cyanspring.common.staticdata.RefData;

import java.util.List;

public interface IRefDataFilter {
	List<RefData> filter(List<RefData> lstRefData) throws Exception;
}
