package com.cyanspring.common.staticdata.policy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.staticdata.RefData;

public class DefaultContractPolicy {

	public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat ymSdf = new SimpleDateFormat("yyyyMM");
	Calendar cal;
	public void init(Calendar cal) {
		this.cal = cal;
	}

	private List<Integer> contractMonths;

	public List<Integer> getContractMonths() {
		return contractMonths;
	}

	public void setContractMonths(final List<Integer> contractMonths) {
		this.contractMonths = contractMonths;
	}

	public List<String> getContractMonths(RefData refData) {
		List<String> lstContractMonth = new ArrayList<>();
		Calendar firstContractMonth = getFirstContractMonth(refData);
		for (int i = 0; i < 12; i++) {
			Calendar contractMonth = (Calendar)firstContractMonth.clone();
			contractMonth.add(Calendar.MONTH, i);
			int month = contractMonth.get(Calendar.MONTH);
			if (contractMonths != null && contractMonths.contains(month)) {
				lstContractMonth.add(ymSdf.format(contractMonth.getTime()));
			}
		}

		return lstContractMonth;
	}

	public Calendar getFirstContractMonth(RefData refData) {
		String settlement = refData.getSettlementDate();
		Date settlementDate = Calendar.getInstance().getTime();
		try {
			settlementDate = sdf.parse(settlement);
		} catch (ParseException e) {
		}

		Calendar cal = (Calendar) this.cal.clone();
		Date today = cal.getTime();
		if (today.after(settlementDate)) {
			cal.add(Calendar.MONTH, 1);
		}

		return cal;
	}

}
