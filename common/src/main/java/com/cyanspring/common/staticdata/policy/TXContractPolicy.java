package com.cyanspring.common.staticdata.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class TXContractPolicy extends DefaultContractPolicy {

	// 近二遠三, 當月 下月及隨後三個季月
	@Override
	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);
		Calendar firstQtrMonth = firstContractMonth;

		// 当月、下月
		for (int i = 0; i < 2; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			firstQtrMonth = contractMonth;
		}

		int month = firstQtrMonth.get(Calendar.MONTH);
		switch (month) {
		case 0:
		case 1:
			firstQtrMonth.set(Calendar.MONTH, 2);
			break;
		case 2:
		case 3:
		case 4:
			firstQtrMonth.set(Calendar.MONTH, 5);
			break;
		case 5:
		case 6:
		case 7:
			firstQtrMonth.set(Calendar.MONTH, 8);
			break;
		case 8:
		case 9:
		case 10:
			firstQtrMonth.set(Calendar.MONTH, 11);
			break;
		case 11:
			firstQtrMonth.add(Calendar.YEAR, 1);
			firstQtrMonth.set(Calendar.MONTH, 2);
			break;
		default:
			break;
		}

		// 随后三个季月
		lstContractMonth.add(ymSdf.format(firstQtrMonth.getTime()));
		firstQtrMonth.add(Calendar.MONTH, 3);
		lstContractMonth.add(ymSdf.format(firstQtrMonth.getTime()));
		firstQtrMonth.add(Calendar.MONTH, 3);
		lstContractMonth.add(ymSdf.format(firstQtrMonth.getTime()));

		return lstContractMonth;
	}

}
