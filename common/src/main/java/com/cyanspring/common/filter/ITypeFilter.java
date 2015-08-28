package com.cyanspring.common.filter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class ITypeFilter implements IRefDataFilter {

	/**
	 * Change the value of property "types" of bean "futuresIndexRefDataFilter" to filter.
	 * 
	 * DataObjects with matching "iType" will be returned as a new DataObject list. 
	 * 
	 * @param lstDataObj
	 *            The DataObject list to be filtered
	 * @return The filtered DataObject list
	 */
private static final Logger LOGGER = LoggerFactory.getLogger(ITypeFilter.class);
	
	private IType[] types;

	public IType[] getTypes() {
		return types;
	}

	public void setTypes(IType[] types) {
		this.types = types;
	}

	/**
	 * Change the value of property "types" of bean "itypeFilter" to filter.
	 * 
	 * Compare IType of RefData between property "types" and lstRefData
	 * RefData with matching IType will be returned as a RefData list. 
	 * 
	 * @param lstRefData
	 *            The DataObject list to be filtered
	 * @return The filtered RefData list
	 */
	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		ArrayList<RefData> fLstRefData = new ArrayList<RefData>();
		if (lstRefData != null) {
			ArrayList<String> lstITypes = new ArrayList<String>();
			for (IType iType : getTypes()) {
				lstITypes.add(iType.getValue());
			}
	
			for (RefData refData : lstRefData) {
				String type = refData.getIType();
				if (!StringUtils.hasText(type)) {
					continue;
				}
				
				if (lstITypes.contains(type)) {
					fLstRefData.add(refData);
				}
			}
		}

		return fLstRefData;
	}

}
