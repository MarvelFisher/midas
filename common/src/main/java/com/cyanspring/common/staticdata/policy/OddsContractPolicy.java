package com.cyanspring.common.staticdata.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class OddsContractPolicy extends ContractPolicy {

	// 1、3、5、7、9、11月
	@Override
	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);
		for (int i = 0; i < 12; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			int month = contractMonth.get(Calendar.MONTH);
			if (month % 2 == 0) {
				lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			}
		}

		return lstContractMonth;
	}

}
