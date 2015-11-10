package com.cyanspring.server.validation;

import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class FcRefDataChecker implements IRefDataChecker {

	@Override
	public boolean check(RefData refData) {
		if(IType.isFuture(refData.getIType()))
			return true;
		
		return false;
	}

}
