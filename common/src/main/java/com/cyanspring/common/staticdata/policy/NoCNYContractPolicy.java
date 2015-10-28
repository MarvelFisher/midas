package com.cyanspring.common.staticdata.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class NoCNYContractPolicy extends ContractPolicy {

	int cnyMonth = 1;
	// 1 -12月（春节月份除外）
	@Override
	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);

		// 連續 12 個合約月份
		for (int i = 0; i < 12; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			int month = contractMonth.get(Calendar.MONTH);
			if (month != cnyMonth) {
				lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			}
		}

		return lstContractMonth;
	}
	
	public int getCnyMonth() {
		return cnyMonth;
	}
	
	public void setCnyMonth(int cnyMonth) {
		this.cnyMonth = cnyMonth;
	}

}
