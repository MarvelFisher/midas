package com.cyanspring.server.validation;

import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataBitUtil;

public class FcRefDataChecker implements IRefDataChecker {

	@Override
	public boolean check(RefData refData) {
		long iType = refData.getInstrumentType();
		if (iType == 0)
			return false;
		if (RefDataBitUtil.isFutures(iType))
			return true;
		
		return false;
	}

}
