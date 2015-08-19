package com.cyanspring.common.filter;

import com.cyanspring.common.data.DataObject;

import java.util.List;

public interface IDataObjectFilter {
	List<? extends DataObject> filter(List<? extends DataObject> lstDataObj) throws DataObjectFilterException;
}
