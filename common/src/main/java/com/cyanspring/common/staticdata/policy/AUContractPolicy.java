package com.cyanspring.common.staticdata.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class AUContractPolicy extends DefaultContractPolicy {

	// 最近三个连续月份的合约以及最近11个月以内的双月合约
	@Override
	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);

		// 最近三个连续月份
		for (int i = 0; i < 3; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
		}

		// 最近13个月以内的双月合约
		for (int i = 3; i < 13; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			if (contractMonth.get(Calendar.MONTH) % 2 == 1) {
				lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			}
		}

		return lstContractMonth;
	}

}
