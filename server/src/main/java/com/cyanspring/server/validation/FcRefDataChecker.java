package com.cyanspring.server.validation;

import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataBitUtil;

public class FcRefDataChecker implements IRefDataChecker {

	@Override
	public boolean check(RefData refData) {
		if(RefDataBitUtil.isFutures(refData.getInstrumentType()))
			return true;
		
		return false;
	}

}
