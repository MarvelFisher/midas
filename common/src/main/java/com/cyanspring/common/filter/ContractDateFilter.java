package com.cyanspring.common.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.staticdata.RefData;

public class ContractDateFilter implements IRefDataFilter {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ContractDateFilter.class);
	private boolean checkValidContractDate;

	public boolean isCheckValidContractDate() {
		return checkValidContractDate;
	}

	public void setCheckValidContractDate(boolean checkValidContractDate) {
		this.checkValidContractDate = checkValidContractDate;
	}

	/**
	 * Check if the configured SettlementDate is valid
	 * If SettlementDate is past time or more than 5 year to the future, filter it
	 * 
	 * Reference path:
	 * server/conf/fc/fc.xml
	 * 
	 * @param lstRefData
	 *            The RefData list to be filtered
	 * @return The filtered RefData list
	 */
	@Override
	public List<RefData> filter(List<RefData> lstRefData) throws Exception {
		if (lstRefData != null && lstRefData.size() > 0) {
			Iterator<RefData> itRefData = lstRefData.iterator();
			while (itRefData.hasNext()) {
				RefData data = itRefData.next();
				if (!isValidContractDate(data))
					itRefData.remove();
			}
		} else {
			LOGGER.error("The given RefData list cannot be null or empty");
			throw new Exception(
					"The given RefData list cannot be null or empty");
		}

		return lstRefData;
	}

	private boolean isValidContractDate(RefData refData) {
		if (null == refData)
			return false;

		String settlementDate = null;
		try {
			settlementDate = refData.getSettlementDate();
			if (!StringUtils.hasText(settlementDate))
				return false;

			SimpleDateFormat contractFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar now = Calendar.getInstance();
			int thisYear = now.get(Calendar.YEAR);
			Calendar contractCal = Calendar.getInstance();
			contractCal.setTime(contractFormat.parse(settlementDate));
			int contractYear = contractCal.get(Calendar.YEAR);

			if ((contractYear - thisYear) >= 5 || (contractYear - thisYear) < 0) {
				return false;
			} else {
				return true;
			}

		} catch (ParseException e) {
			LOGGER.warn("not valid contract date:{},{}", settlementDate,
					e.getMessage());
		} catch (Exception e) {
			LOGGER.warn("can't find settlementDate :{},{}",
					refData.getSymbol(), e.getMessage());
		}

		return false;
	}

}
