package com.cyanspring.common.staticdata.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class BUContractPolicy extends ContractPolicy {

	// 24个月以内，其中最近1-6个月为连续月份合约，6个月以后为季月合约。
	@Override
	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);

		// 最近1-6个月为连续月份合约
		for (int i = 0; i < 6; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			firstContractMonth.add(Calendar.MONTH, i);
			lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
		}

		// 6个月以后为季月合约
		for (int i = 6; i < 24; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			if (contractMonth.get(Calendar.MONTH) % 3 == 2) {
				lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			}
		}

		return lstContractMonth;
	}

}
