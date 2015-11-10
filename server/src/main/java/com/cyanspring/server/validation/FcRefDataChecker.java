package com.cyanspring.server.validation;

import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class FcRefDataChecker implements IRefDataChecker {

	@Override
	public boolean check(RefData refData) {
		String iType = refData.getIType();
		if (!StringUtils.hasLength(iType))
			return false;
		if (IType.isFuture(iType))
			return true;
		
		return false;
	}

}
